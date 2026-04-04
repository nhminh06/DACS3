package com.example.dacs3.data.model

import com.google.firebase.Timestamp

data class Contact(
    val id: String = "",
    val userId: String? = null,
    val name: String = "",
    val email: String = "",
    val type: String = "", // "Góp ý" hoặc "Khiếu nại"
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "pending", // pending, processed
    val reply: String? = null,
    val replyAt: Timestamp? = null
)
