package com.example.dacs3.ui.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
                comment = "Chuyến đi tuyệt vời, mình rất hài lòng với sự hỗ trợ của đội ngũ.",
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ),
            Review(
                userName = "Lê Văn Nam",
                rating = 4,
                comment = "Hướng dẫn viên rất nhiệt tình và am hiểu kiến thức địa phương.",
                createdAt = System.currentTimeMillis() - 86400000 * 5
            ),
            Review(
                userName = "Hoàng Anh",
                rating = 5,
                comment = "Dịch vụ đẳng cấp, sẽ quay lại lần sau cùng gia đình!",
                createdAt = System.currentTimeMillis() - 86400000 * 3
            ),
            Review(
                userName = "Phạm Lan",
                rating = 5,
                comment = "Dịch vụ rất chuyên nghiệp, mọi thứ đều đúng kế hoạch.",
                createdAt = System.currentTimeMillis() - 86400000 * 1
            )
        )
    }

    // Chunk to display 2 reviews vertically per page
    val pages = displayReviews.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column {
        Text(
            text = "Đánh giá",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Trải nghiệm thực tế",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        if (displayReviews.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                pageSpacing = 16.dp 
            ) { pageIndex ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pages[pageIndex].forEach { review ->
                        ReviewCard(review)
                    }
                    if (pages[pageIndex].size < 2) {
                        Spacer(modifier = Modifier.height(160.dp))
                    }
                }
            }

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
                                .height(6.dp)
                                .width(if (isSelected) 18.dp else 6.dp),
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ) {}
                    }
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
            .height(165.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = if (!review.userAvatar.isNullOrEmpty()) review.userAvatar else "https://ui-avatars.com/api/?name=${review.userName}&background=random",
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Trải nghiệm thực tế",
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = " • $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${review.rating}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFFFFB800),
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
