package com.example.dacs3.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.repository.BookingRepository
import com.example.dacs3.data.repository.storage.StorageRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {
    private val bookingRepository = BookingRepository()
    private val storageRepository = StorageRepository()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _currentBooking = MutableStateFlow<Booking?>(null)
    val currentBooking: StateFlow<Booking?> = _currentBooking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Boolean?>(null)
    val bookingSuccess: StateFlow<Boolean?> = _bookingSuccess.asStateFlow()

    // Map to store guest counts for dates: "tourId_date" -> count
    private val _dateGuestCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dateGuestCounts: StateFlow<Map<String, Int>> = _dateGuestCounts.asStateFlow()

    fun createBooking(booking: Booking, receiptUri: Uri? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            var finalBooking = booking
            if (receiptUri != null) {
                val imageUrl = storageRepository.uploadFile(receiptUri)
                if (imageUrl != null) {
                    finalBooking = booking.copy(receiptUrl = imageUrl)
                }
            }
            val success = bookingRepository.createBooking(finalBooking)
            _bookingSuccess.value = success
            _isLoading.value = false
        }
    }

    fun loadUserBookings(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = bookingRepository.getBookingsByUser(email)
            _bookings.value = result
            _isLoading.value = false
        }
    }

    fun loadBookingById(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = bookingRepository.getBookingById(bookingId)
            _currentBooking.value = result
            _isLoading.value = false
        }
    }

    fun loadGuestCountsForDates(tourId: String, dates: List<String>) {
        viewModelScope.launch {
            val counts = mutableMapOf<String, Int>()
            dates.forEach { dateStr ->
                // Format date to ISO for DB query if needed, but repository expects what's in Firestore
                // startDate in Firestore is yyyy-MM-dd
                val dbDate = try {
                    val parts = dateStr.split("/")
                    if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
                } catch (e: Exception) { dateStr }
                
                val count = bookingRepository.getGuestCountByDate(tourId, dbDate)
                counts["${tourId}_${dateStr}"] = count
            }
            _dateGuestCounts.value = _dateGuestCounts.value + counts
        }
    }

    fun cancelBooking(bookingId: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = bookingRepository.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
            if (success) {
                // Refresh both the list and the current detail view to update UI
                loadUserBookings(email)
                loadBookingById(bookingId)
            }
            _isLoading.value = false
        }
    }

    fun resetBookingStatus() {
        _bookingSuccess.value = null
    }

    fun listenAndNotifyBookingStatus(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (change in snapshots.documentChanges) {
                    if (change.type == DocumentChange.Type.MODIFIED) {
                        val doc = change.document
                        val status = doc.getString("status") ?: continue
                        val bookingId = doc.getString("id") ?: doc.id
                        val customerName = doc.getString("customerName") ?: "Bạn"

                        if (status == "CONFIRMED" || status == "CANCELLED") {
                            sendBookingNotification(
                                userId = userId,
                                bookingId = bookingId,
                                status = status,
                                customerName = customerName
                            )
                        }
                    }
                }
            }
    }

    private fun sendBookingNotification(
        userId: String,
        bookingId: String,
        status: String,
        customerName: String
    ) {
        val db = FirebaseFirestore.getInstance()
        val notifId = "${bookingId}_${status}"
        val notifRef = db.collection("notifications").document(notifId)

        notifRef.get().addOnSuccessListener { existing ->
            if (existing.exists()) return@addOnSuccessListener

            val (title, message) = when (status) {
                "CONFIRMED" -> Pair(
                    "🎉 Tour đã được xác nhận!",
                    "Booking #${bookingId.take(10)} của $customerName đã được xác nhận. Chúc bạn có chuyến đi vui vẻ!"
                )
                "CANCELLED" -> Pair(
                    "❌ Tour đã bị hủy",
                    "Booking #${bookingId.take(10)} của $customerName đã bị hủy. Vui lòng liên hệ hỗ trợ nếu cần."
                )
                else -> return@addOnSuccessListener
            }

            val notification = hashMapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to "BOOKING",
                "bookingId" to bookingId,
                "status" to status,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "isRead" to false
            )

            notifRef.set(notification)
                .addOnSuccessListener { Log.d("NOTIF", "Notification sent: $notifId") }
                .addOnFailureListener { Log.e("NOTIF", "Failed: ${it.message}") }
        }
    }
}
