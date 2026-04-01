package com.example.dacs3.data.repository

import android.net.Uri
import android.util.Log
import com.example.dacs3.data.repository.storage.StorageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class TourRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRepository = StorageRepository()

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
                // Sử dụng set với merge để tạo mới nếu chưa có document
                val data = hashMapOf("imageUrl" to imageUrl)
                
                firestore.collection("tours")
                    .document(tourId)
                    .set(data, SetOptions.merge()) // Thay update bằng set(merge) để tránh lỗi NOT_FOUND
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
