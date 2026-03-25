package com.example.dacs3.ui

import androidx.compose.runtime.*
import com.example.dacs3.data.model.Article
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.screens.*

@Composable
fun MainContainer() {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var selectedTour by remember { mutableStateOf<Tour?>(null) }

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                onNavigateToRegister = { currentScreen = "register" },
                onNavigateToForgotPassword = { currentScreen = "forgot_password" },
                onLoginSuccess = { currentScreen = "home" }
            )
        }
        "register" -> {
            RegisterScreen(
                onNavigateToLogin = { currentScreen = "login" },
                onRegisterSuccess = { currentScreen = "login" }
            )
        }
        "forgot_password" -> {
            ForgotPasswordScreen(
                onBackToLogin = { currentScreen = "login" }
            )
        }
        "home" -> {
            AppHomeScreen(onNavigate = { screen -> 
                currentScreen = screen 
            })
        }
        "tours" -> {
            TourScreen(
                onNavigate = { screen -> currentScreen = screen },
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                }
            )
        }
        "tour_detail" -> {
            selectedTour?.let { tour ->
                TourDetailScreen(
                    tour = tour,
                    onBack = { currentScreen = "tours" },
                    onNavigateToBooking = { /* Handle booking navigation if needed */ }
                )
            }
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
                    isLoggedIn = true
                )
            }
        }
        "profile" -> {
            ProfileScreen(onNavigate = { screen ->
                currentScreen = screen
            })
        }
        "my_bookings" -> {
            MyBookingsScreen(
                onBack = { currentScreen = "profile" }
            )
        }
        "notifications" -> {
            NotificationsScreen(
                onBack = { currentScreen = "profile" }
            )
        }
        "change_password" -> {
            ChangePasswordScreen(
                onBack = { currentScreen = "profile" }
            )
        }
    }
}
