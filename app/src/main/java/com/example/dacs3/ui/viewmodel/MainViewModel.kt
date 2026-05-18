package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Guide
import com.example.dacs3.data.model.Review
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.GuideRepository
import com.example.dacs3.data.repository.ReviewRepository
import com.example.dacs3.data.repository.TourRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    private val guideRepository = GuideRepository(FirebaseService())
    private val reviewRepository = ReviewRepository()
    private val tourRepository = TourRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _banners = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val banners: StateFlow<List<Map<String, Any>>> = _banners.asStateFlow()

    private val _guides = MutableStateFlow<List<Guide>>(emptyList())
    val guides: StateFlow<List<Guide>> = _guides.asStateFlow()

    private val _allReviews = MutableStateFlow<List<Review>>(emptyList())
    val allReviews: StateFlow<List<Review>> = _allReviews.asStateFlow()

    private val _guideReviews = MutableStateFlow<List<Pair<Review, String>>>(emptyList())
    val guideReviews: StateFlow<List<Pair<Review, String>>> = _guideReviews.asStateFlow()

    init {
        fetchBanners()
        loadGuides()
        loadAllReviews()
    }

    private fun fetchBanners() {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("banners")
                    .orderBy("order", Query.Direction.ASCENDING).get().await()
                _banners.value = snapshot.documents.mapNotNull { doc -> doc.data?.also { it["id"] = doc.id } }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadGuides() { viewModelScope.launch { _guides.value = guideRepository.getAllGuides() } }
    
    fun loadAllReviews() { viewModelScope.launch { _allReviews.value = reviewRepository.getAllReviews(10) } }

    fun loadReviewsForGuide(guideUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val direct = reviewRepository.getReviewsByGuide(guideUserId)
            val guideTours = guideRepository.getToursForGuide(guideUserId)
            val bookingIds = guideTours.mapNotNull { it["bookingId"] as? String }
            val bookingReviews = if (bookingIds.isNotEmpty()) reviewRepository.getReviewsByBookingIds(bookingIds) else emptyList()
            
            val all = (direct + bookingReviews).distinctBy { it.id }
            
            // Lấy thêm tên tour để hiển thị trong review
            _guideReviews.value = withContext(Dispatchers.Default) {
                all.map { r -> 
                    val tour = tourRepository.getTourById(r.tourId)
                    Pair(r, tour?.title ?: "Tour không xác định") 
                }.sortedByDescending { it.first.createdAt }
            }
            _isLoading.value = false
        }
    }
}
