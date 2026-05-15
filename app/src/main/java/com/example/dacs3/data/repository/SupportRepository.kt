package com.example.dacs3.data.repository

import com.example.dacs3.data.model.SupportMessage
import com.example.dacs3.data.remote.FirebaseService
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SupportRepository(private val firebaseService: FirebaseService) {
    private val firestore = firebaseService.getFirestore()
    private val messagesCollection = firestore.collection("support_messages")

    suspend fun sendMessage(message: SupportMessage): Result<Unit> {
        return try {
            val messageMap = hashMapOf(
                "userId" to message.userId,
                "text" to message.text,
                "senderRole" to message.senderRole,
                "timestamp" to message.timestamp
            )
            messagesCollection.add(messageMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToMessages(userId: String): Flow<List<SupportMessage>> = callbackFlow {
        // Bỏ orderBy trong query để tránh lỗi thiếu Index Firestore (FAILED_PRECONDITION)
        // Chúng ta sẽ sắp xếp dữ liệu ở phía client.
        val subscription = messagesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SupportMessage::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.timestamp } ?: emptyList() // Sắp xếp theo thời gian tăng dần
                
                trySend(messages)
            }
        
        awaitClose { subscription.remove() }
    }
}
