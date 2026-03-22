package com.example.dacs3.data.model

data class Tour(
    val id: String,
    val title: String,
    val imageRes: Int,
    val startDate: String,
    val rating: Double,
    val reviewCount: Int,
    val price: Long,
    val duration: String,
    val location: String,
    val type: TourType
)

enum class TourType {
    ALL, DAY_TOUR, MULTI_DAY
}
