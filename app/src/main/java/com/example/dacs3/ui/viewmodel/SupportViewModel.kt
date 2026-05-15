package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.SupportMessage
import com.example.dacs3.data.repository.SupportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SupportViewModel(private val repository: SupportRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<SupportMessage>>(emptyList())
    val messages: StateFlow<List<SupportMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun listenToMessages(userId: String) {
        viewModelScope.launch {
            repository.listenToMessages(userId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(userId: String, text: String) {
        if (text.isBlank()) return
        
        val message = SupportMessage(
            userId = userId,
            text = text,
            senderRole = "user"
        )

        viewModelScope.launch {
            repository.sendMessage(message)
        }
    }
}
