package com.example.dacs3.data.repository

import android.util.Log
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
    private val usersCollection = firestore.collection("users")

    suspend fun getAllGuides(): List<Guide> {
        return try {
            // 1. Lấy tất cả user có role là 'guide'
            val userSnapshot = usersCollection.whereEqualTo("role", "guide").get().await()
            // 2. Lấy tất cả thông tin bổ sung từ collection 'guides'
            val guideSnapshot = guidesCollection.get().await()
            
            val guideDocs = guideSnapshot.documents
            
            userSnapshot.documents.mapNotNull { userDoc ->
                val userId = userDoc.id
                // Tìm guide document tương ứng với userId này
                val guideDetailDoc = guideDocs.find { it.getString("userId") == userId }
                
                val guide = if (guideDetailDoc != null) {
                    guideDetailDoc.toObject(Guide::class.java)?.copy(id = guideDetailDoc.id)
                } else {
                    Guide(userId = userId)
                }
                
                guide?.copy(
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    sdt = userDoc.getString("sdt") ?: "",
                    imageUrl = userDoc.getString("avatar") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("GuideRepository", "Lỗi lấy danh sách HDV: ${e.message}")
            emptyList()
        }
    }

    suspend fun getGuideByUserId(userId: String): Guide? {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) return null
            
            val snapshot = guidesCollection.whereEqualTo("userId", userId).get().await()
            val guide = if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                doc.toObject(Guide::class.java)?.copy(id = doc.id)
            } else {
                val newGuide = Guide(userId = userId)
                val docRef = guidesCollection.add(newGuide).await()
                newGuide.copy(id = docRef.id)
            }
            
            guide?.copy(
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                sdt = userDoc.getString("sdt") ?: "",
                imageUrl = userDoc.getString("avatar") ?: ""
            )
        } catch (e: Exception) {
            null
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
        return try {
            val snapshot = bookingsCollection.whereEqualTo("guideId", guideId).get().await()
            val toursWithDetails = mutableListOf<Map<String, Any>>()
            
            for (doc in snapshot.documents) {
                val tourId = doc.getString("tourId") ?: continue
                val tourDoc = toursCollection.document(tourId).get().await()
                if (tourDoc.exists()) {
                    val tourData = tourDoc.data ?: continue
                    val bookingData = doc.data ?: emptyMap()
                    
                    val combinedData = tourData.toMutableMap()
                    combinedData["id"] = doc.id 
                    combinedData["bookingId"] = doc.id
                    combinedData["tourId"] = tourId
                    combinedData["startDate"] = bookingData["startDate"] ?: "N/A"
                    combinedData["status"] = bookingData["tripStatus"] ?: "preparing"
                    combinedData["tripNote"] = bookingData["tripNote"] ?: ""
                    
                    toursWithDetails.add(combinedData)
                }
            }
            toursWithDetails.sortByDescending { it["startDate"] as? String }
            toursWithDetails
        } catch (e: Exception) {
            emptyList()
        }
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
