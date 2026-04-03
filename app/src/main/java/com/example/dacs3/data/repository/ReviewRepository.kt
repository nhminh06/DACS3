package com.example.dacs3.data.repository

import android.util.Log
import com.example.dacs3.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val toursCollection = firestore.collection("tours")

    suspend fun submitReview(review: Review): Boolean {
        return try {
            val docRef = reviewsCollection.document()
            val finalReview = review.copy(id = docRef.id)
            docRef.set(finalReview).await()
            
            // Update tour overall rating
            updateTourRating(review.tourId)
            true
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error submitting review: ${e.message}")
            false
        }
    }

    private suspend fun updateTourRating(tourId: String) {
        try {
            val reviews = reviewsCollection.whereEqualTo("tourId", tourId).get().await()
            if (reviews.isEmpty) return

            val totalRating = reviews.documents.sumOf { it.getLong("rating")?.toDouble() ?: 0.0 }
            val count = reviews.size()
            val averageRating = totalRating / count

            toursCollection.document(tourId).update(
                mapOf(
                    "rating" to averageRating,
                    "reviewCount" to count
                )
            ).await()
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error updating tour rating: ${e.message}")
        }
    }

    suspend fun getReviewsByTour(tourId: String): List<Review> {
        return try {
            reviewsCollection.whereEqualTo("tourId", tourId)
                .get().await()
                .toObjects(Review::class.java)
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
