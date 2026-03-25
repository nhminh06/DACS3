package com.example.dacs3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Notification
import com.example.dacs3.data.model.NotificationType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    val notifications = remember { getSampleNotifications() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Thông báo", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có thông báo nào từ Admin", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(notification, primaryColor)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, primaryColor: Color) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    if (!notification.isRead) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = primaryColor
                        ) {}
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    notification.message,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    notification.timestamp.format(formatter),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getSampleNotifications(): List<Notification> {
    return listOf(
        Notification(
            "1",
            "Phản hồi về tour Đà Nẵng",
            "Chào bạn, Admin đã nhận được yêu cầu của bạn. Chúng tôi sẽ xử lý trong vòng 24h tới. Cảm ơn bạn!",
            LocalDateTime.now().minusHours(2)
        ),
        Notification(
            "2",
            "Xác nhận thay đổi lịch trình",
            "Yêu cầu đổi ngày khởi hành của bạn đã được Admin chấp nhận. Vui lòng kiểm tra lại trong phần Đặt chỗ.",
            LocalDateTime.now().minusDays(1),
            isRead = true
        ),
        Notification(
            "3",
            "Chào mừng thành viên mới",
            "Cảm ơn bạn đã đăng ký tài khoản tại Wind Travel. Chúc bạn có những chuyến đi tuyệt vời!",
            LocalDateTime.now().minusDays(3),
            isRead = true
        )
    )
}
