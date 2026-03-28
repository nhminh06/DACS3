package com.example.dacs3.data.repository

import com.example.dacs3.data.model.User
import com.example.dacs3.data.remote.FirebaseService
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class UserRepository(private val firebaseService: FirebaseService) {
    private val firestore = firebaseService.getFirestore()
    private val usersCollection = firestore.collection("users")

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
}
