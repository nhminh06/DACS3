package com.example.dacs3.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Comment
import com.example.dacs3.data.model.Report
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.ArticleRepository
import com.example.dacs3.data.repository.ReportRepository
import com.example.dacs3.data.repository.storage.StorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(
    private val repository: ArticleRepository = ArticleRepository(),
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {
    private val storageRepository = StorageRepository()
    
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

    // Search Query for Articles
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        fetchHomeArticles()
        fetchAllArticles()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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

    fun createArticle(article: ArticleEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.createArticle(article)
            if (success) {
                fetchAllArticles()
            }
            onComplete(success)
        }
    }

    suspend fun uploadImage(uri: Uri): String? {
        return storageRepository.uploadFile(uri)
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

    fun toggleLikeComment(articleId: String, commentId: String, userId: String) {
        viewModelScope.launch {
            val success = repository.toggleLikeComment(articleId, commentId, userId)
            if (success) {
                fetchComments(articleId)
            }
        }
    }

    fun sendReport(report: Report, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = reportRepository.sendReport(report)
            onComplete(success)
        }
    }
}
