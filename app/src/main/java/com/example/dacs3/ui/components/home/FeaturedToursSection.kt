package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.data.model.Tour
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedToursSection(
    tours: List<Tour>,
    onTourClick: (Tour) -> Unit,
    onSeeAllClick: () -> Unit = {}
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
                fontSize = 13.sp,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(0.dp)
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pages[pageIndex].forEach { tour ->
                    Box(modifier = Modifier.weight(1f)) {
                        HomeTourCard(tour = tour, onClick = { onTourClick(tour) })
                    }
                }
                if (pages[pageIndex].size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
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
fun HomeTourCard(tour: Tour, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val mainColor = Color(0xFF2563EB) // Màu xanh biển chính của app
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // 1. Ảnh chiếm 1/2 khung
            Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(tour.imageUrl.ifEmpty { tour.imageRes })
                        .crossfade(true)
                        .build(),
                    contentDescription = tour.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Rating overlay: Chỉ hiển thị nếu rating > 0 và có review thực tế
                if (tour.rating > 0 && tour.reviewCount > 0) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp) // Hạ thấp xuống
                            .align(Alignment.TopStart),
                        color = Color.Black.copy(alpha = 0.5f), // Trong suốt
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = tour.rating.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Hiển thị nhãn "Mới" với màu xanh biển của app (giống nút "Xem tất cả")
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp) // Hạ thấp xuống
                            .align(Alignment.TopStart),
                        color = mainColor.copy(alpha = 0.7f), // Màu xanh biển app trong suốt
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Mới",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                // 2. Tên tour
                Text(
                    text = tour.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 3. Du khách & Địa điểm - Icon cùng màu với giá
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = mainColor, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${tour.minGuests}-${tour.maxGuests}", color = Color(0xFF64748B), fontSize = 9.sp)
                    }
                    Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = mainColor, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = tour.location, color = Color(0xFF64748B), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Thời gian - Icon cùng màu với giá
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = mainColor, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = tour.duration, color = Color(0xFF64748B), fontSize = 9.sp)
                }
                
                // Thu hẹp khoảng trống bằng cách sử dụng height thay vì weight(1f)
                Spacer(modifier = Modifier.height(8.dp))
                
                // 5. Giá
                Text(
                    text = currencyFormatter.format(tour.price),
                    color = mainColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                
                // Đẩy phần trống xuống dưới cùng của thẻ nếu cần
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
