package com.example.dacs3.data.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val articleId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList()
)
