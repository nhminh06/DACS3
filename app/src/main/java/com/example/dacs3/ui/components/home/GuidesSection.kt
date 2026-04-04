package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Guide
import androidx.compose.foundation.Image

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GuidesSection() {
    val guides = listOf(
        Guide(name = "Nguyễn Văn An", bio = "Chuyên gia Văn hóa Hội An", imageRes = R.drawable.a8),
        Guide(name = "Lê Thị Lan", bio = "Hướng dẫn viên bản địa Sapa", imageRes = R.drawable.a9),
        Guide(name = "Trần Hùng", bio = "Hướng dẫn viên du lịch Vịnh Hạ Long", imageRes = R.drawable.a2),
        Guide(name = "Phạm Minh Hoàng", bio = "Chuyên gia ẩm thực Huế", imageRes = R.drawable.a7)
    )

    // Nhóm 2 người vào 1 trang để lướt một lần ra 2 cái
    val pages = guides.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column {
        Text(
            text = "Người đồng hành cùng bạn",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pages[pageIndex].forEach { guide ->
                    Box(modifier = Modifier.weight(1f)) {
                        GuideCard(guide)
                    }
                }
                // Nếu trang cuối chỉ có 1 phần tử, thêm một Box trống để giữ bố cục cân đối
                if (pages[pageIndex].size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun GuideCard(guide: Guide) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = guide.imageRes),
                contentDescription = guide.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = guide.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = guide.bio,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Xem chi tiết",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
