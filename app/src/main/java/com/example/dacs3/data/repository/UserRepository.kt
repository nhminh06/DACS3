package com.example.dacs3.data.repository

import android.net.Uri
import android.util.Log
import com.example.dacs3.data.model.User
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.storage.StorageRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository(private val firebaseService: FirebaseService) {
    private val firestore = firebaseService.getFirestore()
    private val usersCollection = firestore.collection("users")
    private val storageRepository = StorageRepository()

    suspend fun login(name: String, password: String): User? {
        val querySnapshot = usersCollection
            .whereEqualTo("name", name)
            .get()
            .await()

        if (querySnapshot.isEmpty) return null

        val document = querySnapshot.documents[0]
        val dbPassword = document.getString("password")
        
        return if (dbPassword == password) {
            document.toObject(User::class.java)?.copy(id = document.id)
        } else {
            null
        }
    }

    suspend fun register(user: User): Result<Unit> {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("name", user.name)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                return Result.failure(Exception("Tên đăng nhập đã tồn tại"))
            }

            val newUserMap = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "password" to user.password,
                "avatar" to "",
                "created_at" to Timestamp.now(),
                "dia_chi" to "",
                "gioi_tinh" to "",
                "ngay_sinh" to "",
                "rank" to "Bronze",
                "role" to "user",
                "sdt" to "",
                "trang_thai" to "active"
            )

            usersCollection.add(newUserMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userMap = hashMapOf(
                "email" to user.email,
                "sdt" to user.sdt,
                "dia_chi" to user.dia_chi,
                "gioi_tinh" to user.gioi_tinh,
                "ngay_sinh" to user.ngay_sinh
            )
            usersCollection.document(user.id).update(userMap as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload ảnh đại diện lên Cloudinary và cập nhật trường 'avatar' trong Firestore
     * @param userId ID của người dùng trong Firestore
     * @param uri URI của ảnh chọn từ máy
     */
    suspend fun uploadAvatar(userId: String, uri: Uri): String? {
        return try {
            Log.d("UserRepository", "Đang upload avatar lên Cloudinary...")
            val imageUrl = storageRepository.uploadFile(uri)
            
            if (imageUrl != null) {
                Log.d("UserRepository", "Upload avatar thành công: $imageUrl")
                
                // Cập nhật trường 'avatar' trong collection 'users'
                firestore.collection("users")
                    .document(userId)
                    .update("avatar", imageUrl)
                    .await()
                
                Log.d("UserRepository", "Đã cập nhật avatar cho user: $userId")
                imageUrl
            } else {
                Log.e("UserRepository", "Không nhận được URL từ Cloudinary")
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Lỗi upload avatar: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
