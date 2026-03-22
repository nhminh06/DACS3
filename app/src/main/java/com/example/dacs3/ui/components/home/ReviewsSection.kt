package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Review

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewsSection() {
    val reviews = listOf(
        Review(
            userName = "Trần Thị Minh",
            date = "12/05/2024",
            rating = 5,
            comment = "Chuyến đi vừa rồi được anh An hướng dẫn thực sự rất ấn tượng. Ứng dụng này đã giúp mình có những trải nghiệm văn hóa địa phương tuyệt vời. Chắc chắn sẽ giới thiệu cho bạn bè!",
            userAvatar = R.drawable.a9
        ),
        Review(
            userName = "Lê Văn Nam",
            date = "10/05/2024",
            rating = 4,
            comment = "Dịch vụ rất tốt, hướng dẫn viên nhiệt tình và am hiểu kiến thức lịch sử. Mình rất hài lòng với chuyến đi Sapa vừa qua.",
            userAvatar = R.drawable.a8
        ),
        Review(
            userName = "Hoàng Anh",
            date = "08/05/2024",
            rating = 5,
            comment = "Một trải nghiệm không thể quên tại Hội An. Cảm ơn đội ngũ đã hỗ trợ!",
            userAvatar = R.drawable.a2
        )
    )

    // Thiết lập pagerState với pageCount là số lượng bình luận
    val pagerState = rememberPagerState(pageCount = { reviews.size })

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
        
        // Sử dụng HorizontalPager để hiển thị đúng 1 đánh giá tại một thời điểm
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            // Không sử dụng contentPadding để đảm bảo 1 cái tràn hết phần hiển thị bên trong wrapper
            pageSpacing = 16.dp 
        ) { page ->
            ReviewCard(reviews[page])
        }

        // Dấu chấm chỉ số (Indicators)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(reviews.size) { index ->
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

@Composable
fun ReviewCard(review: Review) {
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
                Image(
                    painter = painterResource(id = review.userAvatar),
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
                            maxLines = 1
                        )
                        Text(text = review.date, fontSize = 11.sp, color = Color.Gray)
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
                maxLines = 3
            )
        }
    }
}
