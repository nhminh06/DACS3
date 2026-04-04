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
            try {
                val profile = repository.getGuideByUserId(userId)
                _guideProfile.value = profile
                loadTours(userId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
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

    fun updateBio(newBio: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentGuide = _guideProfile.value
        if (currentGuide == null) {
            onError("Chưa tải được hồ sơ, vui lòng đợi giây lát.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(bio = newBio)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }.onFailure {
                onError("Lỗi khi cập nhật giới thiệu: ${it.message}")
            }
            _isLoading.value = false
        }
    }

    fun updateSkills(newSkills: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentGuide = _guideProfile.value
        if (currentGuide == null) {
            onError("Chưa tải được hồ sơ, vui lòng đợi giây lát.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(skills = newSkills)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }.onFailure {
                onError("Lỗi khi cập nhật kỹ năng: ${it.message}")
            }
            _isLoading.value = false
        }
    }

    fun addExperience(experience: Experience, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentProfile = _guideProfile.value
        if (currentProfile == null) {
            onError("Chưa tải được hồ sơ, vui lòng đợi giây lát.")
            return
        }
        if (experience.title.isEmpty() || experience.startTime.isEmpty() || experience.description.isEmpty()) {
            onError("Vui lòng điền đầy đủ thông tin bắt buộc (Chức danh, Thời gian, Mô tả)")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val newExp = experience.copy(id = java.util.UUID.randomUUID().toString())
            repository.addExperience(currentProfile.id, newExp).onSuccess {
                // Update local state directly to ensure UI reflects changes immediately
                val updatedExps = (currentProfile.experiences) + newExp
                _guideProfile.value = currentProfile.copy(experiences = updatedExps)
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Lỗi khi thêm kinh nghiệm")
            }
            _isLoading.value = false
        }
    }
    
    fun deleteExperience(expId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentGuide = _guideProfile.value
        if (currentGuide == null) {
            onError("Chưa tải được hồ sơ.")
            return
        }
        val updatedExps = currentGuide.experiences.filter { it.id != expId }
        viewModelScope.launch {
            _isLoading.value = true
            val updated = currentGuide.copy(experiences = updatedExps)
            repository.updateGuide(updated).onSuccess {
                _guideProfile.value = updated
                onSuccess()
            }.onFailure {
                onError("Lỗi khi xóa kinh nghiệm: ${it.message}")
            }
            _isLoading.value = false
        }
    }
}
