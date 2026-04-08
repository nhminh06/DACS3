package com.example.dacs3.data.repository

import android.net.Uri
import android.util.Log
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.repository.storage.StorageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class TourRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRepository = StorageRepository()
    private val toursCollection = firestore.collection("tours")

    /**
     * Lấy danh sách tour đang hoạt động
     */
    suspend fun getActiveTours(): List<Tour> {
        return try {
            val querySnapshot = toursCollection
                .get()
                .await()
            
            val allTours = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject(Tour::class.java)?.copy(id = doc.id)
            }
            
            // Log for debugging
            Log.d("TourRepository", "Total tours fetched: ${allTours.size}")
            allTours.forEach { tour ->
                Log.d("TourRepository", "Tour: ${tour.title}, Status: ${tour.trang_thai}")
            }

            // Filter manually to be safe with field names or missing fields
            allTours.filter { it.trang_thai == "active" || it.trang_thai.isEmpty() }

        } catch (e: Exception) {
            Log.e("TourRepository", "Lỗi lấy danh sách tour: ${e.message}")
            emptyList()
        }
    }

    /**
     * Lấy thông tin chi tiết một tour theo ID
     */
    suspend fun getTourById(tourId: String): Tour? {
        return try {
            val doc = toursCollection.document(tourId).get().await()
            if (doc.exists()) {
                doc.toObject(Tour::class.java)?.copy(id = doc.id)
            } else null
        } catch (e: Exception) {
            Log.e("TourRepository", "Lỗi lấy chi tiết tour: ${e.message}")
            null
        }
    }

    /**
     * Test hàm upload ảnh lên Cloudinary và lưu link vào Firestore
     */
    suspend fun uploadTourImageAndSave(uri: Uri, tourId: String): Boolean {
        return try {
            // 1. Upload ảnh lên Cloudinary
            Log.d("TourRepository", "Bắt đầu upload ảnh lên Cloudinary...")
            val imageUrl = storageRepository.uploadFile(uri)
            
            if (imageUrl != null) {
                Log.d("TourRepository", "Upload Cloudinary thành công: $imageUrl")
                
                // 2. Lưu URL vào Firestore
                val data = hashMapOf("imageUrl" to imageUrl)
                
                toursCollection.document(tourId)
                    .set(data, SetOptions.merge())
                    .await()
                
                Log.d("TourRepository", "Đã lưu URL vào Firestore cho tour: $tourId")
                true
            } else {
                Log.e("TourRepository", "Upload Cloudinary trả về null")
                false
            }
        } catch (e: Exception) {
            Log.e("TourRepository", "Lỗi trong quá trình upload và lưu: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
