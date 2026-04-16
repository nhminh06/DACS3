package com.example.dacs3.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickNavSection(
    onScrollTo: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val row1 = listOf(
        NavItem("Địa điểm", Icons.Default.LocationOn, Color(0xFFEF4444)) { onScrollTo(4) },
        NavItem("Văn hóa", Icons.Default.AutoStories, Color(0xFF3B82F6)) { onScrollTo(5) },
        NavItem("Hướng dẫn", Icons.Default.Explore, Color(0xFF10B981)) { onScrollTo(6) },
        NavItem("Đánh giá", Icons.Default.Stars, Color(0xFFF59E0B)) { onScrollTo(7) },
    )

    val row2 = listOf(
        NavItem("Liên hệ", Icons.Default.SupportAgent, Color(0xFF0EA5E9)) { onNavigate("contact") },
        NavItem("Viết bài", Icons.Default.EditNote, Color(0xFF8B5CF6)) { onNavigate("create_article") },
        NavItem("Thông báo", Icons.Default.NotificationsActive, Color(0xFFF1D517)) { onNavigate("notifications") },
        NavItem("Chat Bot", Icons.Default.SmartToy, Color(0xFF2563EB)) { onNavigate("chatbot") },
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Khám phá nhanh",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color(0xFF1E293B)
        )
        
        // Hàng 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row1.forEach { item ->
                QuickNavCard(item = item, modifier = Modifier.weight(1f))
            }
        }

        // Hàng 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row2.forEach { item ->
                QuickNavCard(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickNavCard(item: NavItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { item.onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            color = Color(0xFF475569),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
