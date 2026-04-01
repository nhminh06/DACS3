package com.example.dacs3.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.local.SessionManager
import com.example.dacs3.data.model.User
import com.example.dacs3.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _currentUser = mutableStateOf<User?>(sessionManager.getUser())
    val currentUser: State<User?> = _currentUser

    fun login(name: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.login(name, pass)
            _isLoading.value = false
            if (user != null) {
                _currentUser.value = user
                sessionManager.saveUser(user)
                onSuccess()
            } else {
                onError("Sai tên đăng nhập hoặc mật khẩu")
            }
        }
    }

    fun register(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(user)
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Đăng ký thất bại")
            }
        }
    }

    fun updateUserInfo(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateUser(user)
            _isLoading.value = false
            result.onSuccess {
                _currentUser.value = user
                sessionManager.saveUser(user)
                onSuccess()
            }.onFailure {
                onError(it.message ?: "Cập nhật thông tin thất bại")
            }
        }
    }

    fun updateAvatar(uri: Uri, onError: (String) -> Unit) {
        val userId = _currentUser.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val newUrl = repository.uploadAvatar(userId, uri)
            _isLoading.value = false
            if (newUrl != null) {
                val updatedUser = _currentUser.value?.copy(avatar = newUrl)
                _currentUser.value = updatedUser
                updatedUser?.let { sessionManager.saveUser(it) }
            } else {
                onError("Cập nhật ảnh đại diện thất bại")
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        sessionManager.clearSession()
        _currentUser.value = null
        onSuccess()
    }
    
    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
