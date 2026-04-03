package com.example.dacs3.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs3.data.local.SessionManager
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.UserRepository
import com.example.dacs3.ui.screens.*
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.MainViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.example.dacs3.ui.viewmodel.factory.UserViewModelFactory
import com.example.dacs3.data.model.ArticleCategory

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
    val articleViewModel: ArticleViewModel = viewModel()

    // Mặc định vào App là màn hình Home
    var currentScreen by remember { mutableStateOf("home") }
    var selectedArticle by remember { mutableStateOf<ArticleEntity?>(null) }
    var selectedTour by remember { mutableStateOf<Tour?>(null) }
    var selectedBookingId by remember { mutableStateOf<String?>(null) }
    var initialArticleCategory by remember { mutableStateOf(ArticleCategory.CULTURE) }
    
    // Passenger State for Navigation
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    var infantCount by remember { mutableIntStateOf(0) }

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
                articleViewModel = articleViewModel,
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                },
                onArticleClick = { article ->
                    selectedArticle = article
                    currentScreen = "article_detail"
                },
                onCategoryClick = { category ->
                    when (category) {
                        "Du lịch" -> currentScreen = "tours"
                        "Văn hóa" -> {
                            initialArticleCategory = ArticleCategory.CULTURE
                            currentScreen = "explore"
                        }
                        "Ẩm thực" -> {
                            initialArticleCategory = ArticleCategory.CUISINE
                            currentScreen = "explore"
                        }
                        "Làng nghề" -> {
                            initialArticleCategory = ArticleCategory.CRAFT_VILLAGE
                            currentScreen = "explore"
                        }
                    }
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
                    onNavigateToBooking = { a, c, i ->
                        adultCount = a
                        childCount = c
                        infantCount = i
                        currentScreen = "booking_form" 
                    }
                )
            }
        }
        "booking_form" -> {
            selectedTour?.let { tour ->
                BookingFormScreen(
                    tourId = tour.id,
                    initialAdults = adultCount,
                    initialChildren = childCount,
                    initialInfants = infantCount,
                    onNavigateBack = { currentScreen = "tour_detail" },
                    onBookingSuccess = {
                        // Sau khi đặt thành công, chuyển đến danh sách đơn hàng
                        currentScreen = "my_bookings"
                    },
                    viewModel = mainViewModel
                )
            }
        }
        "explore" -> {
            ArticleExplorerScreen(
                onNavigate = { screen -> currentScreen = screen },
                onArticleClick = { article ->
                    selectedArticle = article
                    currentScreen = "article_detail"
                },
                articleViewModel = articleViewModel,
                initialCategory = initialArticleCategory
            )
        }
        "article_detail" -> {
            selectedArticle?.let { article ->
                ArticleDetailScreen(
                    article = article,
                    onBack = { currentScreen = "explore" },
                    onNavigateToTour = { currentScreen = "tours" },
                    userViewModel = userViewModel,
                    articleViewModel = articleViewModel
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
                onBack = { currentScreen = "profile" },
                onBookingClick = { bookingId ->
                    selectedBookingId = bookingId
                    currentScreen = "booking_detail"
                }
            )
        }
        "booking_detail" -> {
            selectedBookingId?.let { id ->
                BookingDetailScreen(
                    bookingId = id,
                    onNavigateBack = { currentScreen = "my_bookings" },
                    viewModel = mainViewModel
                )
            }
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
