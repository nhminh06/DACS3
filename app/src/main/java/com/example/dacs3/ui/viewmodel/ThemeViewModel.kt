package com.example.dacs3.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.local.datastore.ThemeSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val themeSettings = ThemeSettings(application)

    val isDarkMode: StateFlow<Boolean> = themeSettings.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleTheme() {
        viewModelScope.launch {
            themeSettings.saveDarkMode(!isDarkMode.value)
        }
    }
}
