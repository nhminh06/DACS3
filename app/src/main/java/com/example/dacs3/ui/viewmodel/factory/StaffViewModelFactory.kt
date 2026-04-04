package com.example.dacs3.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3.data.repository.GuideRepository
import com.example.dacs3.ui.viewmodel.StaffViewModel

class StaffViewModelFactory(private val repository: GuideRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StaffViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
