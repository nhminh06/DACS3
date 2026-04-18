package com.example.dacs3.data.repository

import android.util.Log
import com.example.dacs3.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
            
            // Cập nhật lại điểm trung bình cho tour
            updateTourRating(review.tourId)
            true
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error submitting review: ${e.message}")
            false
        }
    }

    private suspend fun updateTourRating(tourId: String) {
        if (tourId.isBlank()) return
        
        try {
            // Lấy tất cả đánh giá của tour này từ Firebase
            val snapshot = reviewsCollection.whereEqualTo("tourId", tourId).get().await()
            val reviews = snapshot.toObjects(Review::class.java)
            
            if (reviews.isEmpty()) return

            // Tính tổng điểm và trung bình cộng
            val totalRating = reviews.sumOf { it.rating.toDouble() }
            val count = reviews.size
            val averageRating = totalRating / count

            // Làm tròn đến 1 chữ số thập phân (ví dụ: 3.0)
            val roundedRating = Math.round(averageRating * 10.0) / 10.0

            // Cập nhật thông tin vào document của tour
            toursCollection.document(tourId).update(
                mapOf(
                    "rating" to roundedRating,
                    "reviewCount" to count
                )
            ).await()
            
            Log.d("ReviewRepository", "Updated tour $tourId: rating=$roundedRating, count=$count")
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

    suspend fun getReviewsByGuide(guideId: String): List<Review> {
        return try {
            reviewsCollection.whereEqualTo("guideId", guideId)
                .get().await()
                .toObjects(Review::class.java)
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error getting reviews by guide: ${e.message}")
            emptyList()
        }
    }

    suspend fun getReviewsByBookingIds(bookingIds: List<String>): List<Review> {
        if (bookingIds.isEmpty()) return emptyList()
        return try {
            val result = mutableListOf<Review>()
            // Firestore giới hạn 10-30 items trong whereIn
            bookingIds.chunked(10).forEach { chunk ->
                val snapshot = reviewsCollection.whereIn("bookingId", chunk).get().await()
                result.addAll(snapshot.toObjects(Review::class.java))
            }
            result.distinctBy { it.id }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error getting reviews by bookingIds: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllReviews(limit: Int = 10): List<Review> {
        return try {
            reviewsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get().await()
                .toObjects(Review::class.java)
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Error getting all reviews: ${e.message}")
            emptyList()
        }
    }
}
