package com.example.dacs3.data.model

import java.time.LocalDateTime

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.ADMIN_FEEDBACK
)

enum class NotificationType {
    ADMIN_FEEDBACK,
    SYSTEM_UPDATE,
    BOOKING_STATUS
}
