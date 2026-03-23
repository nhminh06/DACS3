package com.example.dacs3.ui

import androidx.compose.runtime.*
import com.example.dacs3.data.model.Article
import com.example.dacs3.ui.screens.*

@Composable
fun MainContainer() {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

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
            ArticleExplorerScreen(
                onNavigate = { screen -> currentScreen = screen },
                onArticleClick = { article ->
                    selectedArticle = article
                    currentScreen = "article_detail"
                }
            )
        }
        "article_detail" -> {
            selectedArticle?.let { article ->
                ArticleDetailScreen(
                    article = article,
                    onBack = { currentScreen = "explore" },
                    onNavigateToTour = { currentScreen = "tours" },
                    isLoggedIn = true // Giả định đã đăng nhập để hiện form comment
                )
            }
        }
        "profile" -> {
            ProfileScreen(onNavigate = { screen ->
                currentScreen = screen
            })
        }
    }
}
