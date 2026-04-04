package com.example.dacs3.data.model

data class Guide(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val bio: String = "",
    val imageRes: Int = 0,
    val skills: List<String> = emptyList(),
    val experiences: List<Experience> = emptyList()
)

data class Experience(
    val id: String = "",
    val title: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = ""
)
