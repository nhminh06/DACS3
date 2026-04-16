package com.example.dacs3.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs3.data.local.SessionManager
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.UserRepository
import com.example.dacs3.data.repository.ContactRepository
import com.example.dacs3.data.repository.GuideRepository
import com.example.dacs3.ui.screens.*
import com.example.dacs3.ui.screens.user.*
import com.example.dacs3.ui.screens.articles.*
import com.example.dacs3.ui.screens.home.*
import com.example.dacs3.ui.screens.tours.*
import com.example.dacs3.ui.screens.contact.*
import com.example.dacs3.ui.screens.staff.*
import com.example.dacs3.ui.screens.chatbot.ChatBotScreen
import com.example.dacs3.ui.viewmodel.*
import com.example.dacs3.ui.viewmodel.factory.*
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
    
    val contactRepository = ContactRepository(firebaseService)
    val contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(contactRepository)
    )

    val guideRepository = GuideRepository(firebaseService)
    val staffViewModel: StaffViewModel = viewModel(
        factory = StaffViewModelFactory(guideRepository)
    )

    val mainViewModel: MainViewModel = viewModel()
    val articleViewModel: ArticleViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    val user by userViewModel.currentUser
    
    // Lắng nghe thay đổi trạng thái booking để gửi notification
    LaunchedEffect(user?.id) {
        user?.id?.let { uid ->
            bookingViewModel.listenAndNotifyBookingStatus(uid)
            if (user?.role == "guide") {
                staffViewModel.loadGuideProfile(uid)
            }
        }
    }

    // Mặc định vào App
    var currentScreen by remember { mutableStateOf("home") }
    var previousScreenForDetail by remember { mutableStateOf("explore") }
    
    // Nếu là guide, chuyển thẳng sang màn hình staff personal profile
    LaunchedEffect(user?.role) {
        if (user?.role == "guide" && (currentScreen == "home" || currentScreen == "login")) {
            currentScreen = "staff_personal"
        }
    }

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
                onLoginSuccess = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "home"
                    }
                }
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
                userViewModel = userViewModel,
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
                    previousScreenForDetail = "home"
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
        "chatbot" -> {
            ChatBotScreen(
                onBack = { currentScreen = "home" },
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                },
                onArticleClick = { article ->
                    selectedArticle = article
                    previousScreenForDetail = "home"
                    currentScreen = "article_detail"
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
                    tour = tour,
                    initialAdults = adultCount,
                    initialChildren = childCount,
                    initialInfants = infantCount,
                    onNavigateBack = { currentScreen = "tour_detail" },
                    onBookingSuccess = {
                        currentScreen = "my_bookings"
                    },
                    userViewModel = userViewModel,
                    bookingViewModel = bookingViewModel
                )
            }
        }
        "explore" -> {
            ArticleExplorerScreen(
                onNavigate = { screen -> currentScreen = screen },
                onArticleClick = { article ->
                    selectedArticle = article
                    previousScreenForDetail = "explore"
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
                    onBack = { currentScreen = previousScreenForDetail },
                    onNavigateToTour = { currentScreen = "tours" },
                    userViewModel = userViewModel,
                    articleViewModel = articleViewModel
                )
            }
        }
        "create_article" -> {
            CreateArticleScreen(
                userViewModel = userViewModel,
                articleViewModel = articleViewModel,
                onBack = { currentScreen = "profile" }
            )
        }
        "edit_article" -> {
            selectedArticle?.let { article ->
                EditArticleScreen(
                    article = article,
                    userViewModel = userViewModel,
                    articleViewModel = articleViewModel,
                    onBack = { currentScreen = "my_articles" }
                )
            }
        }
        "contact" -> {
            ContactScreen(
                userViewModel = userViewModel,
                contactViewModel = contactViewModel,
                onNavigate = { screen -> currentScreen = screen }
            )
        }
        "profile" -> {
            if (userViewModel.isLoggedIn()) {
                ProfileScreen(
                    userViewModel = userViewModel,
                    articleViewModel = articleViewModel,
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
        "my_articles" -> {
            MyArticlesScreen(
                onBack = { currentScreen = "profile" },
                onNavigateToDetail = { article ->
                    selectedArticle = article
                    previousScreenForDetail = "my_articles"
                    currentScreen = "article_detail"
                },
                onNavigateToEdit = { article ->
                    selectedArticle = article
                    currentScreen = "edit_article"
                },
                userViewModel = userViewModel,
                articleViewModel = articleViewModel
            )
        }
        "edit_profile" -> {
            EditProfileScreen(
                userViewModel = userViewModel,
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                }
            )
        }
        "my_bookings" -> {
            MyBookingsScreen(
                onBack = { currentScreen = "profile" },
                onBookingClick = { bookingId ->
                    selectedBookingId = bookingId
                    currentScreen = "booking_detail"
                },
                userViewModel = userViewModel,
                bookingViewModel = bookingViewModel
            )
        }
        "booking_detail" -> {
            selectedBookingId?.let { id ->
                BookingDetailScreen(
                    bookingId = id,
                    onNavigateBack = { currentScreen = "my_bookings" },
                    bookingViewModel = bookingViewModel
                )
            }
        }
        "notifications" -> {
            NotificationsScreen(
                userViewModel = userViewModel,
                contactViewModel = contactViewModel,
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                }
            )
        }
        "change_password" -> {
            ChangePasswordScreen(
                userViewModel = userViewModel,
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                }
            )
        }
        
        // Staff Screens
        "staff_personal" -> {
            StaffPersonalScreen(
                userViewModel = userViewModel,
                staffViewModel = staffViewModel,
                onNavigate = { screen -> 
                    if (screen == "login") {
                        userViewModel.logout { currentScreen = "login" }
                    } else {
                        currentScreen = screen
                    }
                },
                onBack = { 
                    userViewModel.logout { currentScreen = "login" }
                }
            )
        }
        "staff_schedule" -> {
            StaffScheduleScreen(
                staffViewModel = staffViewModel,
                onBack = { currentScreen = "staff_personal" },
                onTourClick = { id -> 
                    selectedBookingId = id
                    currentScreen = "staff_trip_detail"
                }
            )
        }
        "staff_trip_detail" -> {
            selectedBookingId?.let { id ->
                StaffTripDetailScreen(
                    bookingId = id,
                    staffViewModel = staffViewModel,
                    onBack = { currentScreen = "staff_schedule" }
                )
            }
        }
        "staff_notes" -> {
            StaffNotesScreen(
                staffViewModel = staffViewModel,
                onBack = { currentScreen = "staff_personal" }
            )
        }
        "staff_skills" -> {
            StaffSkillsScreen(
                staffViewModel = staffViewModel,
                onBack = { currentScreen = "staff_personal" }
            )
        }
    }
}
