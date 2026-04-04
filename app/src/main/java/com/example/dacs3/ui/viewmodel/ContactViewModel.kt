package com.example.dacs3.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.model.Contact
import com.example.dacs3.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isSuccess = mutableStateOf(false)
    val isSuccess: State<Boolean> = _isSuccess

    private val _userContacts = MutableStateFlow<List<Contact>>(emptyList())
    val userContacts: StateFlow<List<Contact>> = _userContacts

    fun submitContact(contact: Contact, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.submitContact(contact)
            _isLoading.value = false
            
            result.onSuccess {
                _isSuccess.value = true
                onComplete(true, null)
                // Refresh list if user is logged in
                contact.userId?.let { fetchUserContacts(it) }
            }.onFailure {
                onComplete(false, it.message)
            }
        }
    }

    fun fetchUserContacts(userId: String) {
        viewModelScope.launch {
            val result = repository.getUserContacts(userId)
            result.onSuccess {
                _userContacts.value = it
            }
        }
    }

    fun resetSuccess() {
        _isSuccess.value = false
    }
}
