package com.example.dacs3.data.repository

import com.example.dacs3.data.model.Experience
import com.example.dacs3.data.model.Guide
import com.example.dacs3.data.remote.FirebaseService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GuideRepository(private val firebaseService: FirebaseService) {
    private val firestore = firebaseService.getFirestore()
    private val guidesCollection = firestore.collection("guides")
    private val toursCollection = firestore.collection("tours")
    private val bookingsCollection = firestore.collection("bookings")

    suspend fun getGuideByUserId(userId: String): Guide? {
        val snapshot = guidesCollection.whereEqualTo("userId", userId).get().await()
        return if (!snapshot.isEmpty) {
            val doc = snapshot.documents[0]
            doc.toObject(Guide::class.java)?.copy(id = doc.id)
        } else {
            // Create a new guide profile if not exists
            val newGuide = Guide(userId = userId)
            val docRef = guidesCollection.add(newGuide).await()
            newGuide.copy(id = docRef.id)
        }
    }

    suspend fun updateGuide(guide: Guide): Result<Unit> {
        return try {
            guidesCollection.document(guide.id).set(guide).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getToursForGuide(guideId: String): List<Map<String, Any>> {
        // In a real scenario, tours would have a guideId field or a mapping table
        // For now, let's assume tours are assigned to guides
        val snapshot = toursCollection.whereEqualTo("guideId", guideId).get().await()
        return snapshot.documents.map { (it.data ?: emptyMap()) + ("id" to it.id) }
    }
    
    suspend fun getBookingsForTour(tourId: String): List<Map<String, Any>> {
        val snapshot = bookingsCollection.whereEqualTo("tourId", tourId).get().await()
        return snapshot.documents.map { (it.data ?: emptyMap()) + ("id" to it.id) }
    }

    suspend fun addExperience(guideId: String, experience: Experience): Result<Unit> {
        return try {
            val guide = guidesCollection.document(guideId).get().await().toObject(Guide::class.java)
            val updatedExps = (guide?.experiences ?: emptyList()) + experience.copy(id = java.util.UUID.randomUUID().toString())
            guidesCollection.document(guideId).update("experiences", updatedExps).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
