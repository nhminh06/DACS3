package com.example.dacs3.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBottomBar(
    currentScreen: String = "home",
    onNavigate: (String) -> Unit = {}
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
            // Trang chủ
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (currentScreen == "home") Icons.Filled.Home else Icons.Outlined.Home, 
                        contentDescription = "Trang chủ", 
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { Text("Trang chủ", fontSize = 11.sp) },
                selected = currentScreen == "home",
                onClick = { onNavigate("home") },
                colors = commonColors
            )

            // Tours
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (currentScreen == "tours") Icons.Filled.LocalActivity else Icons.Outlined.LocalActivity, 
                        contentDescription = "Tour", 
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { Text("Tours", fontSize = 11.sp) },
                selected = currentScreen == "tours",
                onClick = { onNavigate("tours") },
                colors = commonColors
            )

            // Bài viết
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (currentScreen == "explore") Icons.Filled.Explore else Icons.Outlined.Explore, 
                        contentDescription = "Bài viết", 
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { Text("Bài viết", fontSize = 11.sp) },
                selected = currentScreen == "explore",
                onClick = { onNavigate("explore") },
                colors = commonColors
            )

            // Cá nhân
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (currentScreen == "profile") Icons.Filled.Person else Icons.Outlined.Person, 
                        contentDescription = "Cá nhân", 
                        modifier = Modifier.size(24.dp)
                    ) 
                },
                label = { Text("Cá nhân", fontSize = 11.sp) },
                selected = currentScreen == "profile",
                onClick = { onNavigate("profile") },
                colors = commonColors
            )
        }
    }
}
