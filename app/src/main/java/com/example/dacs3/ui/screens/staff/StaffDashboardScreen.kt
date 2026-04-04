package com.example.dacs3.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.ui.viewmodel.UserViewModel

@Composable
fun StaffDashboardScreen(
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val user by userViewModel.currentUser
    val primaryColor = Color(0xFF2563EB)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Header: Avatar + Tên + Rank
        AsyncImage(
            model = user?.avatar?.ifEmpty { "https://via.placeholder.com/150" } ?: "https://via.placeholder.com/150",
            contentDescription = "Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = user?.name ?: "Staff Name",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Text(
            text = "Rank: ${user?.rank ?: "N/A"}",
            fontSize = 14.sp,
            color = primaryColor,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 4 Nút chức năng chính
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardButton(
                    modifier = Modifier.weight(1f),
                    title = "Lịch Tour",
                    icon = Icons.Default.DateRange,
                    color = Color(0xFF3B82F6),
                    onClick = { onNavigate("staff_schedule") }
                )
                DashboardButton(
                    modifier = Modifier.weight(1f),
                    title = "Cá nhân",
                    icon = Icons.Default.Person,
                    color = Color(0xFF10B981),
                    onClick = { onNavigate("staff_profile") }
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardButton(
                    modifier = Modifier.weight(1f),
                    title = "Kỹ năng",
                    icon = Icons.Default.Psychology,
                    color = Color(0xFFF59E0B),
                    onClick = { onNavigate("staff_skills") }
                )
                DashboardButton(
                    modifier = Modifier.weight(1f),
                    title = "Kinh nghiệm",
                    icon = Icons.Default.Work,
                    color = Color(0xFF8B5CF6),
                    onClick = { onNavigate("staff_experience") }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button
        TextButton(onClick = { userViewModel.logout { onNavigate("login") } }) {
            Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
        }
    }
}
