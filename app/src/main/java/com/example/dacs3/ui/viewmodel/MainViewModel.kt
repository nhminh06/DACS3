package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Guide
import com.example.dacs3.data.model.Review
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.model.TourType
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.GuideRepository
import com.example.dacs3.data.repository.ReviewRepository
import com.example.dacs3.data.repository.TourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val tourRepository = TourRepository()
    private val guideRepository = GuideRepository(FirebaseService())
    private val reviewRepository = ReviewRepository()

    private val _allTours = MutableStateFlow<List<Tour>>(emptyList())
    val allTours: StateFlow<List<Tour>> = _allTours.asStateFlow()

    private val _tours = MutableStateFlow<List<Tour>>(emptyList())
    val tours: StateFlow<List<Tour>> = _tours.asStateFlow()

    private val _guides = MutableStateFlow<List<Guide>>(emptyList())
    val guides: StateFlow<List<Guide>> = _guides.asStateFlow()

    // Danh sách đánh giá hdv kèm theo thông tin tên tour
    private val _guideReviews = MutableStateFlow<List<Pair<Review, String>>>(emptyList())
    val guideReviews: StateFlow<List<Pair<Review, String>>> = _guideReviews.asStateFlow()

    private val _allReviews = MutableStateFlow<List<Review>>(emptyList())
    val allReviews: StateFlow<List<Review>> = _allReviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTourType = MutableStateFlow("Tất cả")
    val selectedTourType = _selectedTourType.asStateFlow()

    private val _selectedScale = MutableStateFlow("Tất cả")
    val selectedScale = _selectedScale.asStateFlow()

    private val _selectedLocations = MutableStateFlow<Set<String>>(emptySet())
    val selectedLocations = _selectedLocations.asStateFlow()

    private val DEFAULT_MAX_PRICE = 1000000000f 
    private val _priceRange = MutableStateFlow(0f..DEFAULT_MAX_PRICE)
    val priceRange = _priceRange.asStateFlow()

    private val _selectedDuration = MutableStateFlow("Tất cả")
    val selectedDuration = _selectedDuration.asStateFlow()

    private val _selectedRating = MutableStateFlow(0f)
    val selectedRating = _selectedRating.asStateFlow()

    private val _availableProvinces = MutableStateFlow<List<String>>(emptyList())
    val availableProvinces = _availableProvinces.asStateFlow()

    init {
        loadTours()
        loadGuides()
        loadAllReviews()
    }

    fun loadTours() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tourRepository.getActiveTours()
            _allTours.value = result
            
            _availableProvinces.value = result.map { tour ->
                val loc = tour.location.split(",").last().trim()
                loc
            }.distinct().filter { it.isNotBlank() && !it.contains("Đà Lạt", ignoreCase = true) }.sorted()
            
            applyFilters()
            _isLoading.value = false
        }
    }

    fun loadGuides() {
        viewModelScope.launch {
            _guides.value = guideRepository.getAllGuides()
        }
    }

    fun loadAllReviews() {
        viewModelScope.launch {
            _allReviews.value = reviewRepository.getAllReviews(10)
        }
    }

    fun loadReviewsForGuide(guideUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // 1. Lấy đánh giá trực tiếp theo guideId
            val directReviews = reviewRepository.getReviewsByGuide(guideUserId)
            
            // 2. Lấy đánh giá thông qua các booking mà HDV này tham gia
            val guideTours = guideRepository.getToursForGuide(guideUserId)
            val bookingIds = guideTours.mapNotNull { it["bookingId"] as? String }
            val bookingReviews = if (bookingIds.isNotEmpty()) {
                reviewRepository.getReviewsByBookingIds(bookingIds)
            } else {
                emptyList()
            }
            
            // Hợp nhất và loại bỏ trùng lặp
            val allGuideReviews = (directReviews + bookingReviews).distinctBy { it.id }
            
            val reviewsWithTours = allGuideReviews.map { review ->
                val tourTitle = _allTours.value.find { it.id == review.tourId }?.title ?: "Tour không xác định"
                Pair(review, tourTitle)
            }.sortedByDescending { it.first.createdAt }
            
            _guideReviews.value = reviewsWithTours
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setTourType(type: String) {
        _selectedTourType.value = type
        applyFilters()
    }

    fun setTourScale(scale: String) {
        _selectedScale.value = scale
        applyFilters()
    }

    fun toggleLocation(location: String) {
        val current = _selectedLocations.value.toMutableSet()
        if (current.contains(location)) {
            current.remove(location)
        } else {
            current.add(location)
        }
        _selectedLocations.value = current
        applyFilters()
    }

    fun setMinPrice(price: Float?) {
        val currentRange = _priceRange.value
        _priceRange.value = (price ?: 0f)..currentRange.endInclusive
        applyFilters()
    }

    fun setMaxPrice(price: Float?) {
        val currentRange = _priceRange.value
        _priceRange.value = currentRange.start..(price ?: DEFAULT_MAX_PRICE)
        applyFilters()
    }

    fun setDuration(duration: String) {
        _selectedDuration.value = duration
        applyFilters()
    }

    fun setRating(rating: Float) {
        _selectedRating.value = rating
        applyFilters()
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedTourType.value = "Tất cả"
        _selectedScale.value = "Tất cả"
        _selectedLocations.value = emptySet()
        _priceRange.value = 0f..DEFAULT_MAX_PRICE
        _selectedDuration.value = "Tất cả"
        _selectedRating.value = 0f
        applyFilters()
    }

    fun applyFilters() {
        var filteredList = _allTours.value

        // Search Filter
        if (_searchQuery.value.isNotBlank()) {
            val query = _searchQuery.value.trim().lowercase()
            filteredList = filteredList.filter { tour ->
                tour.title.lowercase().contains(query) || 
                tour.location.lowercase().contains(query) ||
                tour.traiNghiem.lowercase().contains(query)
            }
        }

        fun isSingleDay(tour: Tour): Boolean {
            if (tour.type == TourType.DAY_TOUR) return true
            if (tour.type == TourType.MULTI_DAY) return false
            val d = tour.duration.lowercase()
            return (d.contains("1 ngày") || d.contains("trong ngày")) && !d.contains("đêm")
        }

        if (_selectedTourType.value != "Tất cả") {
            filteredList = filteredList.filter { tour ->
                val singleDay = isSingleDay(tour)
                if (_selectedTourType.value == "Trong ngày") singleDay
                else !singleDay
            }
        }

        if (_selectedScale.value != "Tất cả") {
            filteredList = filteredList.filter { tour ->
                val scale = tour.getTourScaleInfo()?.label ?: "Tùy chỉnh"
                scale == _selectedScale.value
            }
        }

        if (_selectedDuration.value != "Tất cả") {
            filteredList = filteredList.filter { tour ->
                val d = tour.duration.lowercase()
                val dayMatch = Regex("(\\d+)\\s*ngày").find(d)
                val days = dayMatch?.groupValues?.get(1)?.toIntOrNull() ?: (if (d.contains("trong ngày") || d.contains("1 ngày")) 1 else 0)
                
                when (_selectedDuration.value) {
                    "1 ngày" -> days == 1 && !d.contains("đêm")
                    "2-3 ngày" -> days in 2..3
                    "4-5 ngày" -> days in 4..5
                    "6+ ngày" -> days >= 6
                    else -> true
                }
            }
        }

        if (_selectedLocations.value.isNotEmpty()) {
            filteredList = filteredList.filter { tour ->
                _selectedLocations.value.any { loc -> tour.location.contains(loc, ignoreCase = true) }
            }
        }

        filteredList = filteredList.filter { it.price.toFloat() in _priceRange.value }

        if (_selectedRating.value > 0) {
            filteredList = filteredList.filter { it.rating >= _selectedRating.value }
        }

        _tours.value = filteredList
    }

    suspend fun getTourById(tourId: String): Tour? {
        return tourRepository.getTourById(tourId)
    }
}
