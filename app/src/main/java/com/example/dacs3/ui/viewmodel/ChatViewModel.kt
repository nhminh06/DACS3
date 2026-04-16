package com.example.dacs3.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.ChatRepository
import com.example.dacs3.ui.screens.chatbot.ChatMessage  // UI ChatMessage, không phải remote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _messages = mutableStateListOf(
        ChatMessage(
            text = "Xin chào! Tôi là trợ lý ảo WIND 🌊\nTôi có thể giúp bạn tìm tour, thông tin văn hóa hoặc giải đáp thắc mắc về du lịch. Bạn muốn đi đâu?",
            isUser = false
        )
    )
    val messages: List<ChatMessage> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _suggestedTours = MutableStateFlow<List<Tour>>(emptyList())
    val suggestedTours: StateFlow<List<Tour>> = _suggestedTours.asStateFlow()

    private val _suggestedArticles = MutableStateFlow<List<ArticleEntity>>(emptyList())
    val suggestedArticles: StateFlow<List<ArticleEntity>> = _suggestedArticles.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.add(ChatMessage(text = text, isUser = true))
        _isLoading.value = true
        _suggestedTours.value = emptyList()
        _suggestedArticles.value = emptyList()

        viewModelScope.launch {
            try {
                val result = repository.getAiResponse(text)
                val answer = result["answer"] as? String
                    ?: "Xin lỗi, tôi không nhận được câu trả lời."
                val tours = (result["tours"] as? List<*>)?.filterIsInstance<Tour>() ?: emptyList()
                val articles = (result["articles"] as? List<*>)?.filterIsInstance<ArticleEntity>() ?: emptyList()

                _messages.add(ChatMessage(text = answer, isUser = false))
                _suggestedTours.value = tours
                _suggestedArticles.value = articles

            } catch (e: Exception) {
                _messages.add(
                    ChatMessage(
                        text = "Xin lỗi, tôi gặp sự cố kĩ thuật: ${e.message}",
                        isUser = false
                    )
                )
            } finally {
                _isLoading.value = false
            }
        }
    }
}