package com.example.dacs3.data.model

import java.time.LocalDate

data class Booking(
    val id: String = "",
    val tour: Tour = Tour(),
    val status: BookingStatus = BookingStatus.PENDING,
    val startDate: LocalDate = LocalDate.now(),
    val adults: Int = 1,
    val children: Int = 0,
    val infants: Int = 0,
    val totalPrice: Long = 0,
    val note: String? = null,
    val customerName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val paymentMethod: String = "CASH",
    val receiptUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val tripStatus: String = "preparing" // preparing, started, completed, cancelled
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
