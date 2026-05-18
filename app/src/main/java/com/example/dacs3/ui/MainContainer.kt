package com.example.dacs3.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dacs3.data.local.SessionManager
import com.example.dacs3.data.model.ArticleCategory
import com.example.dacs3.data.model.Guide
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.remote.FirebaseService
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.data.repository.ContactRepository
import com.example.dacs3.data.repository.GuideRepository
import com.example.dacs3.data.repository.SupportRepository
import com.example.dacs3.data.repository.UserRepository
import com.example.dacs3.ui.components.guides.GuideDetailScreen
import com.example.dacs3.ui.components.guides.GuidesListScreen
import com.example.dacs3.ui.screens.articles.*
import com.example.dacs3.ui.screens.chatbot.ChatBotScreen
import com.example.dacs3.ui.screens.contact.*
import com.example.dacs3.ui.screens.home.AppHomeScreen
import com.example.dacs3.ui.screens.staff.*
import com.example.dacs3.ui.screens.tours.*
import com.example.dacs3.ui.screens.user.*
import com.example.dacs3.ui.viewmodel.*
import com.example.dacs3.ui.viewmodel.factory.*

@Composable
fun MainContainer(themeViewModel: ThemeViewModel = viewModel()) {
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

    val supportRepository = SupportRepository(firebaseService)
    val supportViewModel: SupportViewModel = viewModel(
        factory = SupportViewModelFactory(supportRepository)
    )

    val mainViewModel: MainViewModel = viewModel()
    val tourViewModel: TourViewModel = viewModel()
    val articleViewModel: ArticleViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val notificationViewModel: NotificationViewModel = viewModel()

    val user by userViewModel.currentUser
    
    // Lắng nghe thay đổi trạng thái booking để gửi notification
    LaunchedEffect(user?.id) {
        user?.id?.let { uid ->
            bookingViewModel.listenAndNotifyBookingStatus(uid)
            notificationViewModel.startListening(uid)
            if (user?.role == "guide") {
                staffViewModel.loadGuideProfile(uid)
            }
        }
    }

    // Navigation State
    var currentScreen by remember { mutableStateOf("home") }
    var previousScreenForDetail by remember { mutableStateOf("explore") }
    var previousScreenForGuideDetail by remember { mutableStateOf("home") }
    var showLoginSuggestion by remember { mutableStateOf(false) }
    
    // Danh sách màn hình cần đăng nhập
    val protectedScreens = listOf(
        "booking_form", "my_bookings", "booking_detail", "profile", "edit_profile",
        "favorites_tours", "favorites_articles", "my_articles", "create_article", 
        "edit_article", "support_chat", "notifications", "change_password",
        "staff_personal", "staff_schedule", "staff_trip_detail", "staff_notes", "staff_skills"
    )

    // Hàm xử lý khi cần đăng nhập
    val onRequireLoginAction = {
        showLoginSuggestion = true
        Toast.makeText(context, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show()
    }

    val navigateTo: (String) -> Unit = { screen ->
        if (protectedScreens.contains(screen) && !userViewModel.isLoggedIn()) {
            onRequireLoginAction()
        } else {
            currentScreen = screen
        }
    }

    // Nếu là guide, chuyển thẳng sang màn hình staff personal profile
    LaunchedEffect(user?.role) {
        if (user?.role == "guide" && (currentScreen == "home" || currentScreen == "login")) {
            currentScreen = "staff_personal"
        }
    }

    var selectedArticle by remember { mutableStateOf<ArticleEntity?>(null) }
    var selectedTour by remember { mutableStateOf<Tour?>(null) }
    var selectedGuideForDetail by remember { mutableStateOf<Guide?>(null) }
    var selectedBookingId by remember { mutableStateOf<String?>(null) }
    var initialArticleCategory by remember { mutableStateOf(ArticleCategory.CULTURE) }
    
    // Passenger State for Navigation
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    var infantCount by remember { mutableIntStateOf(0) }

    // Dialog gợi ý đăng nhập
    if (showLoginSuggestion) {
        AlertDialog(
            onDismissRequest = { showLoginSuggestion = false },
            title = { Text("Yêu cầu đăng nhập", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn cần đăng nhập để sử dụng chức năng này. Bạn có muốn đăng nhập ngay không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLoginSuggestion = false
                        currentScreen = "login"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginSuggestion = false }) {
                    Text("Để sau")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

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
                onNavigate = navigateTo,
                viewModel = mainViewModel,
                tourViewModel = tourViewModel,
                articleViewModel = articleViewModel,
                userViewModel = userViewModel,
                notificationViewModel = notificationViewModel,
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
                        "Du lịch" -> navigateTo("tours")
                        "Văn hóa" -> {
                            initialArticleCategory = ArticleCategory.CULTURE
                            navigateTo("explore")
                        }
                        "Ẩm thực" -> {
                            initialArticleCategory = ArticleCategory.CUISINE
                            navigateTo("explore")
                        }
                        "Làng nghề" -> {
                            initialArticleCategory = ArticleCategory.CRAFT_VILLAGE
                            navigateTo("explore")
                        }
                    }
                }
            )
        }
        "guides_list" -> {
            GuidesListScreen(
                viewModel = mainViewModel,
                onBack = { currentScreen = "home" },
                onGuideClick = { guide: Guide ->
                    selectedGuideForDetail = guide
                    previousScreenForGuideDetail = "guides_list"
                    currentScreen = "guide_detail"
                }
            )
        }
        "guide_detail" -> {
            selectedGuideForDetail?.let { guide ->
                GuideDetailScreen(
                    guide = guide,
                    viewModel = mainViewModel,
                    onBack = { currentScreen = previousScreenForGuideDetail }
                )
            }
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
                onNavigate = navigateTo,
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                },
                viewModel = tourViewModel
            )
        }
        "tour_detail" -> {
            selectedTour?.let { tour ->
                TourDetailScreen(
                    tour = tour,
                    onBack = { currentScreen = "tours" },
                    onNavigateToBooking = { a, c, i ->
                        if (userViewModel.isLoggedIn()) {
                            adultCount = a
                            childCount = c
                            infantCount = i
                            currentScreen = "booking_form"
                        } else {
                            onRequireLoginAction()
                        }
                    },
                    onRequireLogin = onRequireLoginAction,
                    tourViewModel = tourViewModel,
                    userViewModel = userViewModel
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
                onNavigate = navigateTo,
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
                    onRequireLogin = onRequireLoginAction,
                    userViewModel = userViewModel,
                    articleViewModel = articleViewModel
                )
            }
        }
        "create_article" -> {
            CreateArticleScreen(
                userViewModel = userViewModel,
                articleViewModel = articleViewModel,
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                }
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
                onNavigate = navigateTo
            )
        }
        "support_chat" -> {
            SupportChatScreen(
                onBack = { currentScreen = "home" },
                userViewModel = userViewModel,
                supportViewModel = supportViewModel
            )
        }
        "profile" -> {
            ProfileScreen(
                userViewModel = userViewModel,
                articleViewModel = articleViewModel,
                themeViewModel = themeViewModel,
                onNavigate = { screen ->
                    if (screen == "login") {
                        userViewModel.logout {
                            currentScreen = "login"
                        }
                    } else {
                        navigateTo(screen)
                    }
                }
            )
        }
        "favorites_tours" -> {
            FavoritesToursScreen(
                userViewModel = userViewModel,
                tourViewModel = tourViewModel,
                onBack = { currentScreen = "profile" },
                onTourClick = { tour ->
                    selectedTour = tour
                    currentScreen = "tour_detail"
                }
            )
        }
        "favorites_articles" -> {
            FavoritesArticlesScreen(
                userViewModel = userViewModel,
                articleViewModel = articleViewModel,
                onBack = { currentScreen = "profile" },
                onArticleClick = { article ->
                    selectedArticle = article
                    previousScreenForDetail = "favorites_articles"
                    currentScreen = "article_detail"
                }
            )
        }
        "my_articles" -> {
            MyArticlesScreen(
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                },
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
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "profile"
                    }
                },
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
                notificationViewModel = notificationViewModel,
                onBack = { 
                    if (user?.role == "guide") {
                        currentScreen = "staff_personal"
                    } else {
                        currentScreen = "home"
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
                themeViewModel = themeViewModel,
                onNavigate = { screen -> 
                    if (screen == "login") {
                        userViewModel.logout { currentScreen = "login" }
                    } else {
                        navigateTo(screen)
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
