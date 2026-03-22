package com.example.dacs3.ui

import androidx.compose.runtime.*
import com.example.dacs3.ui.screens.AppHomeScreen
import com.example.dacs3.ui.screens.ArticleExplorerScreen

@Composable
fun MainContainer() {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> {
            AppHomeScreen(onNavigate = { screen -> 
                currentScreen = screen 
            })
        }
        "explore" -> {
            ArticleExplorerScreen(onNavigate = { screen -> 
                currentScreen = screen 
            })
        }
    }
}
