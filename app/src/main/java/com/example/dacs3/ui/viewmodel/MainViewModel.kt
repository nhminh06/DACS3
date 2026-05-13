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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.Locale

class MainViewModel : ViewModel() {
    private val tourRepository = TourRepository()
    private val guideRepository = GuideRepository(FirebaseService())
    private val reviewRepository = ReviewRepository()

    private val _allTours = MutableStateFlow<List<Tour>>(emptyList())
    val allTours: StateFlow<List<Tour>> = _allTours.asStateFlow()

    private val _tours = MutableStateFlow<List<Tour>>(emptyList())
    val tours: StateFlow<List<Tour>> = _tours.asStateFlow()

    // PHÂN TRANG: UI sẽ lấy dữ liệu từ đây
    private val _pagedTours = MutableStateFlow<List<Tour>>(emptyList())
    val pagedTours: StateFlow<List<Tour>> = _pagedTours.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val itemsPerPage = 6

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
        loadTours()
        loadGuides()
        loadAllReviews()
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000)
                val currentTours = _allTours.value
                if (currentTours.any { it.isOffer }) {
                    val updatedTours = currentTours.map { tour ->
                        if (tour.isOffer) {
                            val newTime = decrementTime(tour.timeLeft)
                            if (newTime == "00:00:00") {
                                tour.copy(
                                    isOffer = false,
                                    price = if (tour.originalPrice > 0) tour.originalPrice else tour.price,
                                    giaTreEm = if (tour.originalPriceChild > 0) tour.originalPriceChild else tour.giaTreEm,
                                    giaTreNho = if (tour.originalPriceInfant > 0) tour.originalPriceInfant else tour.giaTreNho,
                                    timeLeft = "00:00:00"
                                )
                            } else tour.copy(timeLeft = newTime)
                        } else tour
                    }
                    _allTours.value = updatedTours
                    applyFilters(resetPage = false)
                }
            }
        }
    }

    private fun decrementTime(timeStr: String): String {
        if (timeStr.isBlank() || timeStr == "00:00:00") return "00:00:00"
        return try {
            val cleanStr = timeStr.trim().lowercase()
            var totalSecs: Long = 0
            if (cleanStr.endsWith("h")) {
                totalSecs = (cleanStr.removeSuffix("h").toLongOrNull() ?: 0) * 3600
            } else {
                val parts = cleanStr.split(":").mapNotNull { it.toLongOrNull() }
                totalSecs = when (parts.size) {
                    3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
                    2 -> parts[0] * 3600 + parts[1] * 60
                    1 -> parts[0]
                    else -> 0
                }
            }
            if (totalSecs <= 0) return "00:00:00"
            totalSecs--
            String.format(Locale.US, "%02d:%02d:%02d", totalSecs / 3600, (totalSecs % 3600) / 60, totalSecs % 60)
        } catch (e: Exception) { "00:00:00" }
    }

    fun loadTours() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tourRepository.getActiveTours()
            val unique = withContext(Dispatchers.Default) {
                result.groupBy { tour -> tour.title.lowercase().filter { it.isLetterOrDigit() } }
                    .map { (_, group) -> group.find { it.isOffer } ?: group.first() }
            }
            _allTours.value = unique
            _availableProvinces.value = withContext(Dispatchers.Default) {
                unique.map { it.location.split(",").last().trim() }
                    .distinct().filter { it.isNotBlank() && !it.contains("Đà Lạt", true) }.sorted()
            }
            applyFilters(resetPage = true)
            _isLoading.value = false
        }
    }

    fun applyFilters(resetPage: Boolean = true) {
        if (resetPage) _currentPage.value = 1
        viewModelScope.launch(Dispatchers.Default) {
            var list = _allTours.value
            val q = _searchQuery.value.trim().lowercase()
            if (q.isNotEmpty()) {
                list = list.filter { it.title.lowercase().contains(q) || it.location.lowercase().contains(q) || it.traiNghiem.lowercase().contains(q) }
            }
            if (_selectedTourType.value != "Tất cả") {
                list = list.filter { tour ->
                    val isSingle = tour.duration.lowercase().let { (it.contains("1 ngày") || it.contains("trong ngày")) && !it.contains("đêm") }
                    if (_selectedTourType.value == "Trong ngày") isSingle else !isSingle
                }
            }
            if (_selectedScale.value != "Tất cả") {
                list = list.filter { (it.getTourScaleInfo()?.label ?: "Tùy chỉnh") == _selectedScale.value }
            }
            if (_selectedLocations.value.isNotEmpty()) {
                list = list.filter { tour -> _selectedLocations.value.any { loc -> tour.location.contains(loc, true) } }
            }
            list = list.filter { it.getPrice().toFloat() in _priceRange.value }
            if (_selectedRating.value > 0) list = list.filter { it.rating >= _selectedRating.value }
            
            _tours.value = list
            updatePagedList()
        }
    }

    fun setPage(page: Int) {
        _currentPage.value = page
        updatePagedList()
    }

    private fun updatePagedList() {
        val filtered = _tours.value
        _totalPages.value = maxOf(1, (filtered.size + itemsPerPage - 1) / itemsPerPage)
        val start = (_currentPage.value - 1) * itemsPerPage
        _pagedTours.value = filtered.drop(start).take(itemsPerPage)
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q; applyFilters(true) }
    fun setTourType(t: String) { _selectedTourType.value = t; applyFilters(true) }
    fun setTourScale(s: String) { _selectedScale.value = s; applyFilters(true) }
    fun toggleLocation(l: String) {
        val current = _selectedLocations.value.toMutableSet()
        if (current.contains(l)) current.remove(l) else current.add(l)
        _selectedLocations.value = current
        applyFilters(true)
    }
    fun setMinPrice(p: Float?) { _priceRange.value = (p ?: 0f).._priceRange.value.endInclusive; applyFilters(true) }
    fun setMaxPrice(p: Float?) { _priceRange.value = _priceRange.value.start..(p ?: DEFAULT_MAX_PRICE); applyFilters(true) }
    fun setDuration(d: String) { _selectedDuration.value = d; applyFilters(true) }
    fun setRating(r: Float) { _selectedRating.value = r; applyFilters(true) }
    fun resetFilters() {
        _searchQuery.value = ""; _selectedTourType.value = "Tất cả"; _selectedScale.value = "Tất cả"
        _selectedLocations.value = emptySet(); _priceRange.value = 0f..DEFAULT_MAX_PRICE
        _selectedDuration.value = "Tất cả"; _selectedRating.value = 0f
        applyFilters(true)
    }

    private fun fetchBanners() {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("banners").orderBy("order", Query.Direction.ASCENDING).get().await()
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
            _guideReviews.value = withContext(Dispatchers.Default) {
                all.map { r -> Pair(r, _allTours.value.find { it.id == r.tourId }?.title ?: "Tour không xác định") }.sortedByDescending { it.first.createdAt }
            }
            _isLoading.value = false
        }
    }
    suspend fun getTourById(id: String) = tourRepository.getTourById(id)
}
