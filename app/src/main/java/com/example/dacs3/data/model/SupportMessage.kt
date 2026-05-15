package com.example.dacs3.data.model

import com.google.firebase.Timestamp

data class SupportMessage(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val senderRole: String = "user", // "user" or "admin"
    val timestamp: Timestamp = Timestamp.now()
)
