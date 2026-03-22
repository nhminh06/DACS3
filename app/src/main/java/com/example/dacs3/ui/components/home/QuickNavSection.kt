package com.example.dacs3.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(val title: String, val index: Int, val icon: ImageVector)

@Composable
fun QuickNavSection(onItemClick: (Int) -> Unit) {
    val navItems = listOf(
        NavItem("Địa điểm", 4, Icons.Default.Map),
        NavItem("Văn hóa", 5, Icons.AutoMirrored.Filled.MenuBook),
        NavItem("Hướng dẫn", 6, Icons.Default.Groups),
        NavItem("Đánh giá", 7, Icons.Default.Star),
        NavItem("Liên hệ", 8, Icons.AutoMirrored.Filled.Chat)
    )

    // Tính toán độ rộng để hiển thị khoảng 3.2 mục trên màn hình
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val itemWidth = (screenWidth - 40.dp) / 3.2f // 40dp là tổng padding 2 bên của HomeScreen

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Khám phá nhanh",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(bottom = 14.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 20.dp) // Thêm khoảng trống ở cuối để vuốt thoải mái
        ) {
            items(navItems) { item ->
                QuickNavCard(
                    item = item, 
                    modifier = Modifier.width(itemWidth),
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
fun QuickNavCard(item: NavItem, modifier: Modifier = Modifier, onItemClick: (Int) -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onItemClick(item.index) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Blue button with white icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF3B82F6).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
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
