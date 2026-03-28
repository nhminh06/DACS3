package com.example.dacs3.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.User
import com.example.dacs3.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _currentUser = mutableStateOf<User?>(null)
    val currentUser: State<User?> = _currentUser

    fun login(name: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.login(name, pass)
            _isLoading.value = false
            if (user != null) {
                _currentUser.value = user
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
}
