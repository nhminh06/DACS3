package com.example.dacs3.ui

import androidx.compose.runtime.*
import com.example.dacs3.ui.screens.AppHomeScreen
import com.example.dacs3.ui.screens.ArticleExplorerScreen
import com.example.dacs3.ui.screens.ProfileScreen
import com.example.dacs3.ui.screens.TourScreen

@Composable
fun MainContainer() {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> {
            AppHomeScreen(onNavigate = { screen -> 
                currentScreen = screen 
            })
        }
        "tours" -> {
            TourScreen(onNavigate = { screen ->
                currentScreen = screen
            })
        }
        "explore" -> {
            ArticleExplorerScreen(onNavigate = { screen -> 
                currentScreen = screen 
            })
        }
        "profile" -> {
            ProfileScreen(onNavigate = { screen ->
                currentScreen = screen
            })
        }
    }
}
