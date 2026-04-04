package com.example.dacs3.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Experience
import com.example.dacs3.data.model.Guide
import com.example.dacs3.data.repository.GuideRepository
import kotlinx.coroutines.launch

class StaffViewModel(private val repository: GuideRepository) : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _guideProfile = mutableStateOf<Guide?>(null)
    val guideProfile: State<Guide?> = _guideProfile

    private val _tours = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val tours: State<List<Map<String, Any>>> = _tours
    
    private val _selectedTourBookings = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val selectedTourBookings: State<List<Map<String, Any>>> = _selectedTourBookings

    fun loadGuideProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _guideProfile.value = repository.getGuideByUserId(userId)
            _guideProfile.value?.id?.let { loadTours(it) }
            _isLoading.value = false
        }
    }

    fun loadTours(guideId: String) {
        viewModelScope.launch {
            _tours.value = repository.getToursForGuide(guideId)
        }
    }
    
    fun loadBookingsForTour(tourId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedTourBookings.value = repository.getBookingsForTour(tourId)
            _isLoading.value = false
        }
    }

    fun updateBio(newBio: String, onSuccess: () -> Unit) {
        val currentGuide = _guideProfile.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(bio = newBio)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }
            _isLoading.value = false
        }
    }

    fun updateSkills(newSkills: List<String>, onSuccess: () -> Unit) {
        val currentGuide = _guideProfile.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(skills = newSkills)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }
            _isLoading.value = false
        }
    }

    fun addExperience(experience: Experience, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val guideId = _guideProfile.value?.id ?: return
        if (experience.title.isEmpty() || experience.startTime.isEmpty() || experience.description.isEmpty()) {
            onError("Vui lòng điền đầy đủ thông tin bắt buộc (Chức danh, Thời gian, Mô tả)")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.addExperience(guideId, experience).onSuccess {
                loadGuideProfile(_guideProfile.value?.userId ?: "")
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Lỗi khi thêm kinh nghiệm")
            }
            _isLoading.value = false
        }
    }
    
    fun deleteExperience(expId: String, onSuccess: () -> Unit) {
        val currentGuide = _guideProfile.value ?: return
        val updatedExps = currentGuide.experiences.filter { it.id != expId }
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(experiences = updatedExps)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }
            _isLoading.value = false
        }
    }
}
