package com.example.dacs3.data.repository.storage

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class StorageRepository {

    suspend fun uploadFile(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            Log.d("CloudinaryTest", "Bắt đầu upload URI: $uri")
            
            // Đối với Unsigned Upload, nên sử dụng phương thức .unsigned() 
            // thay vì truyền preset vào .option()
            MediaManager.get().upload(uri)
                .unsigned("images") // Tên preset Unsigned của bạn
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d("CloudinaryTest", "onStart: $requestId")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        if (totalBytes > 0) {
                            val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                            Log.d("CloudinaryTest", "Đang tải lên: $progress%")
                        }
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                        Log.d("CloudinaryTest", "Thành công! URL: $url")
                        continuation.resume(url)
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e("CloudinaryTest", "Lỗi upload: ${error.description} (Mã: ${error.code})")
                        continuation.resume(null)
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w("CloudinaryTest", "Tải lại: ${error.description}")
                        continuation.resume(null)
                    }
                })
                .dispatch()
        }
    }
}
