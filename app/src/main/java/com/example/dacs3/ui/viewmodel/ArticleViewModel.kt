package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Comment
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(private val repository: ArticleRepository = ArticleRepository()) : ViewModel() {
    private val _homeArticles = MutableStateFlow<List<ArticleEntity>>(emptyList())
    val homeArticles: StateFlow<List<ArticleEntity>> = _homeArticles

    private val _explorerArticles = MutableStateFlow<List<ArticleEntity>>(emptyList())
    val explorerArticles: StateFlow<List<ArticleEntity>> = _explorerArticles

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isCommenting = MutableStateFlow(false)
    val isCommenting: StateFlow<Boolean> = _isCommenting

    init {
        fetchHomeArticles()
        fetchAllArticles()
    }

    fun fetchHomeArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getHomeArticles(3)
            _homeArticles.value = result
            _isLoading.value = false
        }
    }

    fun fetchAllArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getAllArticles()
            _explorerArticles.value = result
            _isLoading.value = false
        }
    }

    fun fetchComments(articleId: String) {
        viewModelScope.launch {
            val result = repository.getComments(articleId)
            _comments.value = result
        }
    }

    fun postComment(comment: Comment, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isCommenting.value = true
            val success = repository.addComment(comment)
            if (success) {
                fetchComments(comment.articleId)
            }
            _isCommenting.value = false
            onComplete(success)
        }
    }

    fun deleteComment(articleId: String, commentId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.deleteComment(articleId, commentId)
            if (success) {
                fetchComments(articleId)
            }
            onComplete(success)
        }
    }

    fun toggleLikeComment(articleId: String, commentId: String, userId: String, isLiked: Boolean) {
        viewModelScope.launch {
            val success = repository.toggleLikeComment(articleId, commentId, userId, isLiked)
            if (success) {
                fetchComments(articleId)
            }
        }
    }
}
