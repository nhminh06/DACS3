package com.example.dacs3.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.home.*
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.MainViewModel
import com.example.dacs3.ui.viewmodel.NotificationViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel,
    articleViewModel: ArticleViewModel,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel,
    onTourClick: (Tour) -> Unit,
    onArticleClick: (ArticleEntity) -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    val backgroundColor = Color(0xFFF1F5F9)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val allTours by viewModel.allTours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val systemMeta by notificationViewModel.systemMeta.collectAsState()
    val reportMeta by notificationViewModel.reportMeta.collectAsState()
    val contactMeta by notificationViewModel.contactMeta.collectAsState()

    // Chỉ lấy những tour đang có ưu đãi
    val offerTours = remember(allTours) {
        allTours.filter { it.isOffer }
    }
    
    // Chỉ lấy những tour thường (không phải ưu đãi) để hiện ở mục Nổi bật
    val featuredTours = remember(allTours) {
        allTours.filter { !it.isOffer }
    }

    var showNotifDialog by remember { mutableStateOf(false) }

    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thông báo", fontWeight = FontWeight.ExtraBold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val sortedCategories = remember(systemMeta, reportMeta, contactMeta) {
                        listOf(
                            Triple("system", systemMeta.first, systemMeta.second),
                            Triple("report", reportMeta.first, reportMeta.second),
                            Triple("contact", contactMeta.first, contactMeta.second)
                        ).sortedWith(
                            compareByDescending<Triple<String, Int, Long>> { it.second > 0 }
                                .thenByDescending { it.third }
                        )
                    }

                    sortedCategories.forEach { (type, count, _) ->
                        when(type) {
                            "system" -> NotificationCategoryItem(
                                icon = Icons.Default.CardTravel,
                                title = "Cập nhật Tour",
                                color = Color(0xFFEAB308),
                                count = count,
                                onClick = {
                                    showNotifDialog = false
                                    onNavigate("notifications")
                                }
                            )
                            "report" -> NotificationCategoryItem(
                                icon = Icons.Default.Report,
                                title = "Phản hồi báo cáo",
                                color = Color.Red,
                                count = count,
                                onClick = {
                                    showNotifDialog = false
                                    onNavigate("notifications")
                                }
                            )
                            "contact" -> NotificationCategoryItem(
                                icon = Icons.Default.Chat,
                                title = "Tin nhắn hỗ trợ",
                                color = Color(0xFF2563EB),
                                count = count,
                                onClick = {
                                    showNotifDialog = false
                                    onNavigate("notifications")
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showNotifDialog = false
                        onNavigate("notifications")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xem tất cả", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Đóng", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { 
            AppBottomBar(
                currentScreen = "home",
                onNavigate = onNavigate
            ) 
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { 
                    TopHeader(
                        userViewModel = userViewModel,
                        unreadCount = unreadCount,
                        onProfileClick = { onNavigate("profile") },
                        onNotificationClick = { showNotifDialog = true }
                    ) 
                }
                item { 
                    HomePaddingWrapper { 
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                viewModel.setSearchQuery(it)
                            },
                            onSearchClick = {
                                val query = searchQuery.trim().lowercase()
                                if (query.isNotEmpty()) {
                                    val articles = articleViewModel.explorerArticles.value
                                    val hasMatchingArticle = articles.any {
                                        it.tieu_de.lowercase().contains(query)
                                    }

                                    if (hasMatchingArticle) {
                                        articleViewModel.setSearchQuery(searchQuery)
                                        onNavigate("explore")
                                    } else {
                                        onNavigate("tours")
                                    }
                                }
                            }
                        ) 
                    } 
                }
                
                item { 
                    HomePaddingWrapper { 
                        QuickNavSection(
                            onScrollTo = { index ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                            onNavigate = onNavigate
                        )
                    } 
                }
                item { 
                    HomePaddingWrapper { 
                        CategorySection(onCategoryClick = onCategoryClick)
                    } 
                }

                // Promo Banners
                item {
                    HomePaddingWrapper {
                        PromoBannersSection(banners = banners)
                    }
                }

                // Special Offers (Flash Sale)
                item {
                    HomePaddingWrapper {
                        SpecialOffersSection(
                            tours = offerTours,
                            onTourClick = onTourClick,
                            onSeeAllClick = { onNavigate("tours") }
                        )
                    }
                }

                // Featured Tours (Tour nổi bật)
                if (featuredTours.isNotEmpty()) {
                    item {
                        HomePaddingWrapper {
                            FeaturedToursSection(
                                tours = featuredTours,
                                onTourClick = onTourClick,
                                onSeeAllClick = { onNavigate("tours") }
                            )
                        }
                    }
                }

                item {
                    HomePaddingWrapper {
                        CulturalArticlesSection(
                            articleViewModel = articleViewModel,
                            onSeeAllClick = { onNavigate("explore") },
                            onArticleClick = onArticleClick
                        )
                    }
                }

                item { 
                    HomePaddingWrapper { 
                        GuidesSection(
                            viewModel = viewModel,
                            onSeeAllClick = { onNavigate("guides_list") }
                        ) 
                    } 
                }
                item { HomePaddingWrapper { ReviewsSection(viewModel) } }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            if (isLoading && allTours.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun NotificationCategoryItem(
    icon: ImageVector,
    title: String,
    color: Color,
    count: Int = 0,
    onClick: () -> Unit
) {
    val isNew = count > 0
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isNew) color.copy(alpha = 0.12f) else color.copy(alpha = 0.05f),
        border = if (isNew) BorderStroke(1.5.dp, color.copy(alpha = 0.4f)) else BorderStroke(1.dp, Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = if (isNew) 0.25f else 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontWeight = if (isNew) FontWeight.ExtraBold else FontWeight.SemiBold, 
                    fontSize = 15.sp, 
                    color = if (isNew) Color.Black else Color(0xFF475569)
                )
                if (isNew) {
                    Text(
                        text = "Bạn có $count thông báo mới",
                        fontSize = 12.sp,
                        color = color.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (isNew) {
                Surface(
                    color = Color.Red,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        "MỚI", 
                        color = Color.White, 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomePaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
