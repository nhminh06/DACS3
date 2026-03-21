package com.example.dacs3.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar() {
    Surface(
        modifier = Modifier.shadow(12.dp),
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding().height(65.dp)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Trang chủ", modifier = Modifier.size(24.dp)) },
                label = { Text("Trang chủ", fontSize = 11.sp) },
                selected = true,
                onClick = { },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2563EB),
                    selectedTextColor = Color(0xFF2563EB),
                    unselectedIconColor = Color.Gray.copy(alpha = 0.5f),
                    unselectedTextColor = Color.Gray.copy(alpha = 0.5f),
                    indicatorColor = Color(0xFF2563EB).copy(alpha = 0.1f)
                )
            )
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Explore, contentDescription = "Khám phá", modifier = Modifier.size(24.dp)) },
                label = { Text("Khám phá", fontSize = 11.sp) },
                selected = false,
                onClick = { }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Yêu thích", modifier = Modifier.size(24.dp)) },
                label = { Text("Yêu thích", fontSize = 11.sp) },
                selected = false,
                onClick = { }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Person, contentDescription = "Cá nhân", modifier = Modifier.size(24.dp)) },
                label = { Text("Cá nhân", fontSize = 11.sp) },
                selected = false,
                onClick = { }
            )
        }
    }
}
