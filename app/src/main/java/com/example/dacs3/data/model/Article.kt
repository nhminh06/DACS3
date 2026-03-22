package com.example.dacs3.data.model

enum class ArticleCategory {
    CULTURE,      // Văn hóa
    CRAFT_VILLAGE, // Làng nghề
    CUISINE       // Ẩm thực
}

data class Article(
    val title: String,
    val desc: String,
    val imageRes: Int,
    val category: ArticleCategory,
    val date: String = "20/05/2024"
)
