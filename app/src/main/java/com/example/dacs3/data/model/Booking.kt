package com.example.dacs3.data.model

import java.time.LocalDate

data class Booking(
    val id: String,
    val tour: Tour,
    val status: BookingStatus,
    val startDate: LocalDate,
    val adults: Int,
    val children: Int = 0, // Trẻ em
    val infants: Int = 0,  // Trẻ sơ sinh
    val totalPrice: Long,
    val note: String? = null,
    val customerName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = ""
) {
    val totalPeople: Int get() = adults + children + infants
    
    val durationDays: Int get() {
        val regex = "(\\d+)\\s*ngày".toRegex()
        val match = regex.find(tour.duration)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }

    val endDate: LocalDate get() = startDate.plusDays((durationDays - 1).toLong())
    
    val canCancel: Boolean get() {
        val today = LocalDate.now()
        val daysUntilStart = java.time.temporal.ChronoUnit.DAYS.between(today, startDate)
        return status != BookingStatus.CANCELLED && daysUntilStart >= 3
    }
}

enum class BookingStatus {
    PENDING, CONFIRMED, CANCELLED
}
