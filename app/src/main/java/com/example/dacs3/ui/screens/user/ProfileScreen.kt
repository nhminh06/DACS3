package com.example.dacs3.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.profile.*
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.ThemeViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    articleViewModel: ArticleViewModel,
    themeViewModel: ThemeViewModel,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val user by userViewModel.currentUser
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    // Lưu vị trí của phần Tài khoản & Bảo mật
    var securitySectionY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(user?.id) {
        user?.id?.let { articleViewModel.fetchUserArticles(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Hồ sơ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    // Chuyển icon bánh răng sang bên trái và thêm chức năng cuộn
                    IconButton(onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(securitySectionY.toInt())
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Scroll to Security Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Chuyển nút đổi giao diện sang bên phải
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AppBottomBar(currentScreen = "profile", onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {

            // ── HERO: Avatar + Thông tin cá nhân ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 36.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ProfileHeader(
                        name = user?.name ?: "Người dùng",
                        email = user?.email ?: "Chưa có email",
                        avatarUrl = user?.avatar,
                        imageRes = R.drawable.a8,
                        onAvatarClick = { uri ->
                            userViewModel.updateAvatar(uri, onError = {})
                        }
                    )
                }
            }

            // Card thông tin
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    InfoRow(
                        icon = Icons.Default.Person,
                        label = "Giới tính",
                        value = user?.gioi_tinh ?: "Chưa cập nhật",
                        onClick = { onNavigate("edit_profile") }
                    )
                    Divider16()
                    InfoRow(
                        icon = Icons.Default.Cake,
                        label = "Ngày sinh",
                        value = user?.ngay_sinh ?: "Chưa cập nhật",
                        onClick = { onNavigate("edit_profile") }
                    )
                    Divider16()
                    InfoRow(
                        icon = Icons.Default.Phone,
                        label = "Số điện thoại",
                        value = user?.sdt ?: "Chưa cập nhật",
                        onClick = { onNavigate("edit_profile") }
                    )
                    Divider16()
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Địa chỉ",
                        value = user?.dia_chi ?: "Chưa cập nhật",
                        onClick = { onNavigate("edit_profile") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── NHÓM 1: Du lịch & Đặt chỗ ──────────────────────────────────
            GroupLabel("DU LỊCH & ĐẶT CHỖ")
            ActionCard {
                ActionRow(
                    icon = Icons.Default.BookOnline,
                    iconBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                    iconTint = Color(0xFF3B82F6),
                    title = "Đặt chỗ của tôi",
                    subtitle = "Xem lịch sử & trạng thái booking",
                    isDarkMode = isDarkMode,
                    onClick = { onNavigate("my_bookings") }
                )
                RowDivider()
                ActionRow(
                    icon = Icons.Default.Favorite,
                    iconBg = if (isDarkMode) Color(0xFF451A1C) else Color(0xFFFFF1F2),
                    iconTint = Color(0xFFEF4444),
                    title = "Tour yêu thích",
                    subtitle = "Danh sách tour đã lưu",
                    isDarkMode = isDarkMode,
                    onClick = { onNavigate("favorites_tours") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── NHÓM 2: Nội dung & Cộng đồng ────────────────────────────────
            GroupLabel("NỘI DUNG & CỘNG ĐỒNG")
            ActionCard {
                ActionRow(
                    icon = Icons.Default.Article,
                    iconBg = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFF0FDF4),
                    iconTint = Color(0xFF10B981),
                    title = "Bài viết của tôi",
                    subtitle = "Quản lý bài viết đã đăng",
                    isDarkMode = isDarkMode,
                    onClick = { onNavigate("my_articles") }
                )
                RowDivider()
                ActionRow(
                    icon = Icons.Default.FavoriteBorder,
                    iconBg = if (isDarkMode) Color(0xFF2E1065) else Color(0xFFF5F3FF),
                    iconTint = Color(0xFF8B5CF6),
                    title = "Bài viết yêu thích",
                    subtitle = "Xem lại các bài viết đã lưu",
                    isDarkMode = isDarkMode,
                    onClick = { onNavigate("favorites_articles") }
                )
                RowDivider()
                ActionRow(
                    icon = Icons.Default.PostAdd,
                    iconBg = if (isDarkMode) Color(0xFF451A03) else Color(0xFFFFFBEB),
                    iconTint = Color(0xFFF59E0B),
                    title = "Đóng góp bài viết",
                    subtitle = "Chia sẻ trải nghiệm của bạn",
                    isDarkMode = isDarkMode,
                    onClick = { onNavigate("create_article") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── NHÓM 3: Tài khoản & Bảo mật ────────────────────────────────
            Column(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    securitySectionY = coordinates.positionInParent().y
                }
            ) {
                GroupLabel("TÀI KHOẢN & BẢO MẬT")
                ActionCard {
                    ActionRow(
                        icon = Icons.Default.Notifications,
                        iconBg = if (isDarkMode) Color(0xFF2E1065) else Color(0xFFF5F3FF),
                        iconTint = Color(0xFF7C3AED),
                        title = "Thông báo",
                        subtitle = "Cài đặt thông báo đẩy",
                        isDarkMode = isDarkMode,
                        onClick = { onNavigate("notifications") }
                    )
                    RowDivider()
                    ActionRow(
                        icon = Icons.Default.Lock,
                        iconBg = if (isDarkMode) Color(0xFF0C4A6E) else Color(0xFFEFF6FF),
                        iconTint = Color(0xFF0284C7),
                        title = "Mật khẩu & Bảo mật",
                        subtitle = "Đổi mật khẩu, xác thực 2 bước",
                        isDarkMode = isDarkMode,
                        onClick = { onNavigate("change_password") }
                    )
                    RowDivider()
                    ActionRow(
                        icon = Icons.Default.Edit,
                        iconBg = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFF0FDF4),
                        iconTint = Color(0xFF059669),
                        title = "Chỉnh sửa hồ sơ",
                        subtitle = "Cập nhật thông tin cá nhân",
                        isDarkMode = isDarkMode,
                        onClick = { onNavigate("edit_profile") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Đăng xuất ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onNavigate("login") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) 
                                    else Color(0xFFFFF1F2)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Đăng xuất",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 28.dp, bottom = 6.dp, top = 4.dp)
    )
}

@Composable
private fun ActionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDarkMode: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (value == "Chưa cập nhật") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun Divider16() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 74.dp, end = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}
