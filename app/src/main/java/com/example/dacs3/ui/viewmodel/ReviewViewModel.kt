package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Review
import com.example.dacs3.data.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {
    private val reviewRepository = ReviewRepository()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submitSuccess = MutableStateFlow<Boolean?>(null)
    val submitSuccess: StateFlow<Boolean?> = _submitSuccess.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun submitReview(review: Review) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val success = reviewRepository.submitReview(review)
            _submitSuccess.value = success
            _isSubmitting.value = false
        }
    }

    fun loadReviewsForTour(tourId: String) {
        viewModelScope.launch {
            val result = reviewRepository.getReviewsByTour(tourId)
            _reviews.value = result
        }
    }

    fun resetSubmitStatus() {
        _submitSuccess.value = null
    }
}
