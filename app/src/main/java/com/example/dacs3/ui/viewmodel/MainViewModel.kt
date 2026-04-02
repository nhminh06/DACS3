package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.model.TourType
import com.example.dacs3.data.repository.TourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val tourRepository = TourRepository()

    private val _allTours = MutableStateFlow<List<Tour>>(emptyList())
    private val _tours = MutableStateFlow<List<Tour>>(emptyList())
    val tours: StateFlow<List<Tour>> = _tours.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filter States
    private val _selectedTourType = MutableStateFlow("Tất cả")
    val selectedTourType = _selectedTourType.asStateFlow()

    private val _selectedLocations = MutableStateFlow<Set<String>>(emptySet())
    val selectedLocations = _selectedLocations.asStateFlow()

    // Default price range: 0 to a very large number (effectively no limit)
    private val DEFAULT_MAX_PRICE = 1000000000f 
    private val _priceRange = MutableStateFlow(0f..DEFAULT_MAX_PRICE)
    val priceRange = _priceRange.asStateFlow()

    private val _selectedDuration = MutableStateFlow("Tất cả")
    val selectedDuration = _selectedDuration.asStateFlow()

    private val _selectedRating = MutableStateFlow(0f)
    val selectedRating = _selectedRating.asStateFlow()

    // Dynamic locations
    private val _availableProvinces = MutableStateFlow<List<String>>(emptyList())
    val availableProvinces = _availableProvinces.asStateFlow()

    init {
        loadTours()
    }

    fun loadTours() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tourRepository.getActiveTours()
            _allTours.value = result
            
            // Extract unique provinces/cities from tour locations
            _availableProvinces.value = result.map { tour ->
                val loc = tour.location.split(",").last().trim()
                loc
            }.distinct().filter { it.isNotBlank() && !it.contains("Đà Lạt", ignoreCase = true) }.sorted()
            
            applyFilters()
            _isLoading.value = false
        }
    }

    fun setTourType(type: String) {
        _selectedTourType.value = type
    }

    fun toggleLocation(location: String) {
        val current = _selectedLocations.value.toMutableSet()
        if (current.contains(location)) current.remove(location)
        else current.add(location)
        _selectedLocations.value = current
    }

    fun setPriceRange(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }
    
    fun setMinPrice(min: Float?) {
        val currentRange = _priceRange.value
        val newMin = min ?: 0f
        _priceRange.value = newMin..currentRange.endInclusive
    }

    fun setMaxPrice(max: Float?) {
        val currentRange = _priceRange.value
        val newMax = max ?: DEFAULT_MAX_PRICE
        _priceRange.value = currentRange.start..newMax
    }

    fun setDuration(duration: String) {
        _selectedDuration.value = duration
    }

    fun setRating(rating: Float) {
        _selectedRating.value = rating
    }

    fun resetFilters() {
        _selectedTourType.value = "Tất cả"
        _selectedLocations.value = emptySet()
        _priceRange.value = 0f..DEFAULT_MAX_PRICE
        _selectedDuration.value = "Tất cả"
        _selectedRating.value = 0f
        applyFilters()
    }

    fun applyFilters() {
        var filteredList = _allTours.value

        // Helper to determine if it's a single day tour (no overnight)
        fun isSingleDay(tour: Tour): Boolean {
            if (tour.type == TourType.DAY_TOUR) return true
            if (tour.type == TourType.MULTI_DAY) return false
            
            val d = tour.duration.lowercase()
            return (d.contains("1 ngày") || d.contains("trong ngày")) && !d.contains("đêm")
        }

        // 1. Filter by Tour Type
        if (_selectedTourType.value != "Tất cả") {
            filteredList = filteredList.filter { tour ->
                val singleDay = isSingleDay(tour)
                if (_selectedTourType.value == "Trong ngày") singleDay
                else !singleDay
            }
        }

        // 2. Filter by Specific Duration
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

        // 3. Filter by Location
        if (_selectedLocations.value.isNotEmpty()) {
            filteredList = filteredList.filter { tour ->
                _selectedLocations.value.any { loc -> tour.location.contains(loc, ignoreCase = true) }
            }
        }

        // 4. Filter by Price
        filteredList = filteredList.filter { it.price.toFloat() in _priceRange.value }

        // 5. Filter by Rating
        if (_selectedRating.value > 0) {
            filteredList = filteredList.filter { it.rating >= _selectedRating.value }
        }

        _tours.value = filteredList
    }

    suspend fun getTourById(tourId: String): Tour? {
        return tourRepository.getTourById(tourId)
    }
}
