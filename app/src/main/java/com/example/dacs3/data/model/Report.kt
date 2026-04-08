package com.example.dacs3.data.model

import com.google.firebase.Timestamp

enum class ReportType {
    COMMENT,
    ARTICLE
}

data class Report(
    val id: String = "",
    val type: ReportType = ReportType.COMMENT,
    
    // Thông tin người báo cáo
    val reporterId: String = "",
    val reporterName: String = "",
    
    // Trường hợp báo cáo Bình luận
    val commentId: String? = null,
    val reportedUserId: String? = null, // Người bị báo cáo (chủ comment)
    val reportedUserName: String? = null,
    val commentContent: String? = null,
    
    // Trường hợp báo cáo Bài viết
    val articleId: String? = null,
    val articleTitle: String? = null,
    
    val reason: String = "",
    val createdAt: Timestamp? = null,
    val status: String = "pending" // pending, resolved, dismissed
)
