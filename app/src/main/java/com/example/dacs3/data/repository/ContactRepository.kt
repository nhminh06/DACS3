package com.example.dacs3.data.repository

import com.example.dacs3.data.model.Contact
import com.example.dacs3.data.remote.FirebaseService
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ContactRepository(private val firebaseService: FirebaseService) {
    private val firestore = firebaseService.getFirestore()
    private val contactsCollection = firestore.collection("contacts")

    suspend fun submitContact(contact: Contact): Result<Unit> {
        return try {
            val contactMap = hashMapOf(
                "userId" to contact.userId,
                "name" to contact.name,
                "email" to contact.email,
                "type" to contact.type,
                "content" to contact.content,
                "timestamp" to contact.timestamp,
                "status" to contact.status
            )
            contactsCollection.add(contactMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserContacts(userId: String): Result<List<Contact>> {
        return try {

            val snapshot = contactsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val contacts = snapshot.documents.mapNotNull { doc ->
                try {
                    val contact = doc.toObject(Contact::class.java)?.copy(id = doc.id)
                    contact
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }.sortedByDescending { it.timestamp }

            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
