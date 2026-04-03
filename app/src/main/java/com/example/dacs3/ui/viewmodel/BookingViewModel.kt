package com.example.dacs3.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.repository.BookingRepository
import com.example.dacs3.data.repository.storage.StorageRepository
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
}
