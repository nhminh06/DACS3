package com.example.dacs3.data.model

import com.example.dacs3.R

data class Tour(
    val id: String = "",
    val title: String = "",
    val imageRes: Int = R.drawable.a5, // Added for local resources
    val imageUrl: String = "",
    val startDate: String = "",
    val rating: Double = 5.0,
    val reviewCount: Int = 0,
    val price: Long = 0,
    val duration: String = "",
    val location: String = "",
    val type: TourType = TourType.DAY_TOUR, // Changed to Enum
    val banners: List<String> = emptyList(),
    val dichVu: String = "",
    val loTrinh: String = "",
    val traiNghiem: String = "",
    val trang_thai: String = "active",
    val maTour: String = "",
    val diemKhoiHanh: String = "",
    val giaTreEm: Long = 0,
    val giaTreNho: Long = 0,
    val minGuests: Int = 1,
    val maxGuests: Int = 50
) {
    fun getTourScaleInfo(): TourScale? {
        return when {
            minGuests >= 5 && maxGuests <= 8 -> TourScale.SMALL
            minGuests >= 10 && maxGuests <= 16 -> TourScale.MEDIUM
            minGuests >= 20 && maxGuests <= 35 -> TourScale.LARGE
            else -> null
        }
    }
}

enum class TourScale(val label: String, val transport: String) {
    SMALL("Tour nhỏ", "Xe 4-7 chỗ"),
    MEDIUM("Tour vừa", "Xe 16 chỗ"),
    LARGE("Tour lớn", "Xe 29-45 chỗ")
}

enum class TourType {
    ALL, DAY_TOUR, MULTI_DAY
}
