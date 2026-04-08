package com.example.dacs3.data.repository

import android.util.Log
import com.example.dacs3.data.model.Report
import com.example.dacs3.data.model.ReportType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection("reports")

    suspend fun sendReport(report: Report): Boolean {
        return try {
            val reportData = hashMapOf(
                "type" to report.type.name,
                "reporterId" to report.reporterId,
                "reporterName" to report.reporterName,
                "reason" to report.reason,
                "createdAt" to Timestamp.now(),
                "status" to "pending"
            )

            if (report.type == ReportType.COMMENT) {
                reportData["commentId"] = report.commentId ?: ""
                reportData["reportedUserId"] = report.reportedUserId ?: ""
                reportData["reportedUserName"] = report.reportedUserName ?: ""
                reportData["commentContent"] = report.commentContent ?: ""
            } else {
                reportData["articleId"] = report.articleId ?: ""
                reportData["articleTitle"] = report.articleTitle ?: ""
            }

            reportsCollection.add(reportData).await()
            true
        } catch (e: Exception) {
            Log.e("ReportRepository", "Lỗi gửi báo cáo: ${e.message}")
            false
        }
    }

    suspend fun getUserReports(userId: String): List<Map<String, Any>> {
        return try {
            Log.d("ReportRepository", "Fetching reports for userId: $userId")
            // Bỏ orderBy để tránh lỗi thiếu Index trên Firestore
            val snapshot = reportsCollection
                .whereEqualTo("reporterId", userId)
                .get()
                .await()
            
            val reports = snapshot.documents.map { doc ->
                val data = doc.data?.toMutableMap() ?: mutableMapOf()
                data["id"] = doc.id
                data
            }.sortedByDescending { (it["createdAt"] as? Timestamp)?.seconds ?: 0L }
            
            Log.d("ReportRepository", "Found ${reports.size} reports")
            reports
        } catch (e: Exception) {
            Log.e("ReportRepository", "Lỗi lấy danh sách báo cáo: ${e.message}")
            emptyList()
        }
    }
}
