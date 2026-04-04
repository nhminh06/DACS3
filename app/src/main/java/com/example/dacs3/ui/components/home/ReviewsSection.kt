package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.R
import com.example.dacs3.data.model.Review
import com.example.dacs3.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewsSection(viewModel: MainViewModel) {
    val reviews by viewModel.allReviews.collectAsState()

    // Fallback data if DB is empty
    val displayReviews = reviews.ifEmpty {
        listOf(
            Review(
                userName = "Trần Thị Minh",
                rating = 5,
                comment = "Chuyến đi vừa rồi thực sự rất ấn tượng. Ứng dụng này đã giúp mình có những trải nghiệm tuyệt vời.",
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            Review(
                userName = "Lê Văn Nam",
                rating = 4,
                comment = "Dịch vụ rất tốt, hướng dẫn viên nhiệt tình và am hiểu kiến thức. Mình rất hài lòng.",
                createdAt = System.currentTimeMillis() - 86400000 * 5
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { displayReviews.size })

    Column {
        Text(
            text = "Đánh giá",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = "Trải nghiệm thực tế",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 19.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(14.dp))
        
        if (displayReviews.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 16.dp 
            ) { page ->
                ReviewCard(displayReviews[page])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(displayReviews.size) { index ->
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

@Composable
fun ReviewCard(review: Review) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(review.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = if (!review.userAvatar.isNullOrEmpty()) review.userAvatar else "https://ui-avatars.com/api/?name=${review.userName}&background=random",
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = review.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = dateStr, fontSize = 11.sp, color = Color.Gray)
                    }
                    Row {
                        repeat(review.rating) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = review.comment,
                fontSize = 13.sp,
                color = Color(0xFF475569),
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
