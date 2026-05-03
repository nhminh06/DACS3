package com.example.dacs3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StaffBottomBar(
    currentScreen: String,
    unreadCount: Int = 0,
    onNavigate: (String) -> Unit
) {
    val commonColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF2563EB),
        selectedTextColor = Color(0xFF2563EB),
        unselectedIconColor = Color.Gray.copy(alpha = 0.5f),
        unselectedTextColor = Color.Gray.copy(alpha = 0.5f),
        indicatorColor = Color(0xFF2563EB).copy(alpha = 0.1f)
    )

    Surface(
        modifier = Modifier.shadow(12.dp),
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding().height(65.dp)
        ) {
            // Lịch trình
            NavigationBarItem(
                icon = { Icon(if (currentScreen == "staff_schedule") Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth, null, Modifier.size(24.dp)) },
                label = { Text("Lịch trình", fontSize = 11.sp) },
                selected = currentScreen == "staff_schedule",
                onClick = { onNavigate("staff_schedule") },
                colors = commonColors
            )

            // Thông báo
            NavigationBarItem(
                icon = { 
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentScreen == "notifications") Icons.Filled.Notifications else Icons.Outlined.Notifications, 
                            contentDescription = null, 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text("Thông báo", fontSize = 11.sp) },
                selected = currentScreen == "notifications",
                onClick = { onNavigate("notifications") },
                colors = commonColors
            )

            // Ghi chú
            NavigationBarItem(
                icon = { Icon(if (currentScreen == "staff_notes") Icons.Filled.EditNote else Icons.Outlined.EditNote, null, Modifier.size(24.dp)) },
                label = { Text("Ghi chú", fontSize = 11.sp) },
                selected = currentScreen == "staff_notes",
                onClick = { onNavigate("staff_notes") },
                colors = commonColors
            )

            // Cá nhân
            NavigationBarItem(
                icon = { Icon(if (currentScreen == "staff_personal") Icons.Filled.Person else Icons.Outlined.Person, null, Modifier.size(24.dp)) },
                label = { Text("Cá nhân", fontSize = 11.sp) },
                selected = currentScreen == "staff_personal",
                onClick = { onNavigate("staff_personal") },
                colors = commonColors
            )
        }
    }
}
