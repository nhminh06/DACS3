package com.example.dacs3.ui.components.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Nút hỗ trợ trực tiếp dạng slide-in từ cạnh phải màn hình.
 *
 * Cách dùng — đặt bên trong Box(modifier = Modifier.fillMaxSize()) ở AppHomeScreen:
 *
 *   Box(modifier = Modifier.fillMaxSize()) {
 *       LazyColumn(...) { ... }
 *       SupportSlidePanel(
 *           onChatClick   = { onNavigate("support_chat") },
 *           onCallClick   = { /* gọi điện */ },
 *           modifier      = Modifier.align(Alignment.CenterEnd)
 *       )
 *   }
 */
@Composable
fun SupportSlidePanel(
    onChatClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Hoạt ảnh trượt ngang
    val offsetX by animateDpAsState(
        targetValue  = if (expanded) 0.dp else 148.dp,   // 148dp = chiều rộng panel
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "slideOffset"
    )

    // Độ mờ của mũi tên khi thu gọn
    val arrowAlpha by animateFloatAsState(
        targetValue   = if (expanded) 0f else 0.45f,
        animationSpec = tween(300),
        label         = "arrowAlpha"
    )

    Row(
        modifier = modifier
            .zIndex(10f)
            .offset(x = offsetX),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Nửa hình tròn nhô ra từ cạnh phải ───────────────────────
        Box(
            modifier = Modifier
                .alpha(if (expanded) 0.6f else arrowAlpha)
                .size(width = 20.dp, height = 40.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp))
                .background(Color(0xFF94A3B8).copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = if (expanded) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                contentDescription = if (expanded) "Thu gọn" else "Hỗ trợ",
                tint               = Color.White.copy(alpha = 0.85f),
                modifier           = Modifier.size(14.dp)
            )
        }

        // ── Panel nút hỗ trợ ────────────────────────────────────────
        Column(
            modifier = Modifier
                .width(148.dp)
                .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E40AF), Color(0xFF2563EB))
                    ),
                    RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                )
                .padding(vertical = 16.dp, horizontal = 14.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            // Tiêu đề
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Headset,
                    contentDescription = null,
                    tint     = Color(0xFFBAE6FD),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Hỗ trợ",
                    color      = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 13.sp,
                    letterSpacing = 0.3.sp
                )
            }

            HorizontalDivider(
                color     = Color.White.copy(alpha = 0.2f),
                thickness = 0.8.dp
            )

            // Nút Chat
            SupportButton(
                icon    = Icons.Default.Chat,
                label   = "Chat ngay",
                bgColor = Color(0xFF3B82F6),
                onClick = {
                    expanded = false
                    onChatClick()
                }
            )

            // Nút Gọi điện
            SupportButton(
                icon    = Icons.Default.Phone,
                label   = "Gọi điện",
                bgColor = Color(0xFF10B981),
                onClick = {
                    expanded = false
                    onCallClick()
                }
            )
        }
    }
}

@Composable
private fun SupportButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = Color.White,
                modifier           = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text       = label,
            color      = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp
        )
    }
}