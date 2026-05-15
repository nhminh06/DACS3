package com.example.dacs3.ui.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3.data.repository.SupportRepository
import com.example.dacs3.ui.viewmodel.SupportViewModel

class SupportViewModelFactory(private val repository: SupportRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SupportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
