package com.example.dacs3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClosingGreeting() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp), // Chỉ giữ padding dọc để khớp độ rộng với Form liên hệ
        contentAlignment = Alignment.Center
    ) {
        // Thiết kế dạng thiệp chúc mừng tinh tế, rộng bằng Form liên hệ
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.15f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(32.dp)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tên thương hiệu tinh tế
                Text(
                    text = "WINDTRAVEL",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Lời chào chân thành
                Text(
                    text = "Hẹn gặp lại bạn tại\nMiền Trung thân yêu!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lời chúc viết tay mềm mại
                Text(
                    text = "Chúc bạn có những hành trình thật ý nghĩa",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Cursive
                    ),
                    color = Color(0xFF2563EB),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Điểm nhấn trang trí tối giản
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF93C5FD))
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
