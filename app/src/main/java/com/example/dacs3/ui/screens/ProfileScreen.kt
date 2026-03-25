package com.example.dacs3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.profile.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cá nhân",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF64748B))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            AppBottomBar(currentScreen = "profile", onNavigate = onNavigate)
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Profile Header
            ProfileHeader(
                name = "Nguyễn Văn An",
                email = "an.nguyen@windtravel.com",
                imageRes = R.drawable.a8
            )

            // Activity Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SectionHeader("HOẠT ĐỘNG")
                    ProfileOptionItem(
                        title = "Đặt chỗ của tôi", 
                        icon = Icons.Default.BookOnline, 
                        onClick = { onNavigate("my_bookings") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                    ProfileOptionItem(
                        title = "Thông báo", 
                        icon = Icons.Default.Notifications, 
                        onClick = { onNavigate("notifications") }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                    ProfileOptionItem(
                        title = "Mật khẩu & Bảo mật", 
                        icon = Icons.Default.Lock, 
                        onClick = { onNavigate("change_password") }
                    )
                }
            }

            // Personal Information Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    SectionHeader("THÔNG TIN CÁ NHÂN")
                    ProfileInfoItem(label = "Giới tính", value = "Nam")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                    ProfileInfoItem(label = "Ngày sinh", value = "12/05/1995")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                    ProfileInfoItem(label = "Số điện thoại", value = "0987 654 321")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFF1F5F9))
                    ProfileInfoItem(label = "Địa chỉ", value = "Liên Chiểu, Đà Nẵng")
                }
            }

            // Logout Button
            TextButton(
                onClick = { onNavigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
