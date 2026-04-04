package com.example.dacs3.data.repository

import android.net.Uri
import android.util.Log
import com.example.dacs3.data.model.User
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.remote.RetrofitClient
import com.example.dacs3.data.repository.storage.StorageRepository
import com.google.firebase.Timestamp
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
                "dia_chi" to user.dia_chi,
                "gioi_tinh" to user.gioi_tinh,
                "ngay_sinh" to user.ngay_sinh,
                "rank" to "Bronze",
                "role" to "user",
                "sdt" to user.sdt,
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

    suspend fun changePassword(userId: String, currentPass: String, newPass: String): Result<Unit> {
        return try {
            val doc = usersCollection.document(userId).get().await()
            if (!doc.exists()) return Result.failure(Exception("Người dùng không tồn tại"))
            
            val dbPassword = doc.getString("password")
            if (dbPassword != currentPass) {
                return Result.failure(Exception("Mật khẩu hiện tại không chính xác"))
            }
            
            usersCollection.document(userId).update("password", newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendOtp(email: String): Result<Unit> {
        return try {
            val response = RetrofitClient.authService.sendOtp(email)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "Email không tồn tại hoặc lỗi hệ thống"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Lỗi sendOtp: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithOtp(email: String, otp: String, newPass: String): Result<Unit> {
        return try {
            val response = RetrofitClient.authService.resetPasswordWithOtp(email, otp, newPass)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "Mã OTP không đúng hoặc lỗi hệ thống"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Lỗi resetPasswordWithOtp: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(userId: String, uri: Uri): String? {
        return try {
            Log.d("UserRepository", "Đang upload avatar lên Cloudinary...")
            val imageUrl = storageRepository.uploadFile(uri)
            
            if (imageUrl != null) {
                Log.d("UserRepository", "Upload avatar thành công: $imageUrl")
                
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
