package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.screens.HomeTourCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedToursSection(
    tours: List<Tour>,
    onTourClick: (Tour) -> Unit
) {
    // Chia danh sách thành các trang, mỗi trang có 2 tour
    val pages = tours.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tour du lịch nổi bật",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = Color(0xFF1E293B)
            )
            Text(
                "Xem tất cả",
                color = Color(0xFF2563EB),
                fontSize = 13.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(0.dp) // Không để lộ trang sau để 2 mục hiện đầy đủ
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pages[pageIndex].forEach { tour ->
                    // Sử dụng weight(1f) để 2 card luôn có độ rộng bằng nhau
                    Box(modifier = Modifier.weight(1f)) {
                        HomeTourCard(tour = tour, onClick = { onTourClick(tour) })
                    }
                }
                // Nếu trang cuối chỉ có 1 tour, thêm Spacer để tour đó không bị giãn to
                if (pages[pageIndex].size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Indicators (Dấu chấm chuyển trang)
        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp),
                        shape = CircleShape,
                        color = if (isSelected) Color(0xFF2563EB) else Color.LightGray
                    ) {}
                }
            }
        }
    }
}
