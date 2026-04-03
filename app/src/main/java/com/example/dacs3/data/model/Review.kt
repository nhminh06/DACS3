package com.example.dacs3.data.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val tourId: String = "",
    val bookingId: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
