package com.example.dacs3.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs3.data.local.SessionManager
import com.example.dacs3.data.model.Article
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.UserRepository
import com.example.dacs3.ui.screens.*
import com.example.dacs3.ui.viewmodel.MainViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.example.dacs3.ui.viewmodel.factory.UserViewModelFactory

@Composable
fun MainContainer() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // Khởi tạo ViewModels
    val firebaseService = FirebaseService()
    val userRepository = UserRepository(firebaseService)
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository, sessionManager)
    )
    val mainViewModel: MainViewModel = viewModel()

    // Mặc định vào App là màn hình Home
    var currentScreen by remember { mutableStateOf("home") }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    var selectedTour by remember { mutableStateOf<Tour?>(null) }

    when (currentScreen) {
        "login" -> {
            LoginScreen(
                userViewModel = userViewModel,
                onNavigateToRegister = { currentScreen = "register" },
                onNavigateToForgotPassword = { currentScreen = "forgot_password" },
                onLoginSuccess = { currentScreen = "home" }
            )
        }
        "register" -> {
            RegisterScreen(
                userViewModel = userViewModel,
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
            AppHomeScreen(
                onNavigate = { screen -> currentScreen = screen },
                viewModel = mainViewModel,
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                }
            )
        }
        "tours" -> {
            TourScreen(
                onNavigate = { screen -> currentScreen = screen },
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                },
                viewModel = mainViewModel
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
            if (userViewModel.isLoggedIn()) {
                ProfileScreen(
                    userViewModel = userViewModel,
                    onNavigate = { screen ->
                        if (screen == "login") {
                            userViewModel.logout {
                                currentScreen = "login"
                            }
                        } else {
                            currentScreen = screen
                        }
                    }
                )
            } else {
                // Nếu chưa đăng nhập mà bấm vào Profile, chuyển sang màn hình Login
                LaunchedEffect(Unit) {
                    currentScreen = "login"
                }
            }
        }
        "edit_profile" -> {
            EditProfileScreen(
                userViewModel = userViewModel,
                onBack = { currentScreen = "profile" }
            )
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
