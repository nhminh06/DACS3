package com.example.dacs3.data.model

import com.example.dacs3.R
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Tour(
    val id: String = "",
    val title: String = "",
    val imageRes: Int = R.drawable.a5,
    val imageUrl: String = "",
    val startDate: String = "",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val price: Long? = null,
    val duration: String = "",
    val location: String = "",
    val type: TourType = TourType.DAY_TOUR,
    val banners: List<String> = emptyList(),
    val dichVu: String = "",
    val loTrinh: String = "",
    val traiNghiem: String = "",
    val trang_thai: String = "active",
    val maTour: String = "",
    val diemKhoiHanh: String = "",
    val giaTreEm: Long? = null,
    val giaTreNho: Long? = null,
    val minGuests: Int = 1,
    val maxGuests: Int = 50,
    
    // Deal/Offer fields
    @get:PropertyName("isOffer")
    @set:PropertyName("isOffer")
    @field:PropertyName("isOffer")
    var isOffer: Boolean = false,
    
    @get:PropertyName("originalPrice")
    @set:PropertyName("originalPrice")
    var originalPrice: Long = 0,

    @get:PropertyName("originalPriceChild")
    @set:PropertyName("originalPriceChild")
    var originalPriceChild: Long = 0,

    @get:PropertyName("originalPriceInfant")
    @set:PropertyName("originalPriceInfant")
    var originalPriceInfant: Long = 0,

    val discountTag: String = "",
    val timeLeft: String = "00:00:00",
    val offerImageUrl: String = ""
) {
    fun getTourScaleInfo(): TourScale? {
        return when {
            minGuests >= 5 && maxGuests <= 8 -> TourScale.SMALL
            minGuests >= 10 && maxGuests <= 16 -> TourScale.MEDIUM
            minGuests >= 20 && maxGuests <= 35 -> TourScale.LARGE
            else -> null
        }
    }

    @Exclude
    fun getPrice(): Long = price ?: 0L
    
    @Exclude
    fun getGiaTreEm(): Long = giaTreEm ?: 0L
    
    @Exclude
    fun getGiaTreNho(): Long = giaTreNho ?: 0L
}

enum class TourScale(val label: String, val transport: String) {
    SMALL("Tour nhỏ", "Xe 4-7 chỗ"),
    MEDIUM("Tour vừa", "Xe 16 chỗ"),
    LARGE("Tour lớn", "Xe 29-45 chỗ")
}

enum class TourType {
    ALL, DAY_TOUR, MULTI_DAY
}
