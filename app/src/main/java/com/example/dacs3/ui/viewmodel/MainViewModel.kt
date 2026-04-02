package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.repository.TourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val tourRepository = TourRepository()

    private val _tours = MutableStateFlow<List<Tour>>(emptyList())
    val tours: StateFlow<List<Tour>> = _tours

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTours()
    }

    fun loadTours() {
        viewModelScope.launch {
            _isLoading.value = true
            _tours.value = tourRepository.getActiveTours()
            _isLoading.value = false
        }
    }

    suspend fun getTourById(tourId: String): Tour? {
        return tourRepository.getTourById(tourId)
    }
}
