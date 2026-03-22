package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.dacs3.data.model.Destination

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeaturedDestinationsSection() {
    val destinations = listOf(
        Destination("Phố cổ Hội An", "Quảng Nam", R.drawable.a2),
        Destination("Bà Nà Hills", "Đà Nẵng", R.drawable.a4),
        Destination("Cố đô Huế", "Thừa Thiên Huế", R.drawable.a7),
        Destination("Đà Lạt", "Lâm Đồng", R.drawable.a1),
        Destination("Mũi Né", "Bình Thuận", R.drawable.a3),
        Destination("Nha Trang", "Khánh Hòa", R.drawable.a5),
        Destination("Phong Nha", "Quảng Bình", R.drawable.a6),
        Destination("Đảo Lý Sơn", "Quảng Ngãi", R.drawable.a8)
    )

    // Chia dữ liệu thành 2 hàng riêng biệt
    val row1Destinations = destinations.take(4)
    val row2Destinations = destinations.drop(4)

    // Mỗi hàng sẽ hiển thị 2 item mỗi trang
    val pagesRow1 = row1Destinations.chunked(2)
    val pagesRow2 = row2Destinations.chunked(2)

    val pagerState1 = rememberPagerState(pageCount = { pagesRow1.size })
    val pagerState2 = rememberPagerState(pageCount = { pagesRow2.size })

    Column {
        Text("Địa điểm nổi bật", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hàng thứ nhất
        HorizontalPager(
            state = pagerState1,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pagesRow1[pageIndex].forEach { dest ->
                    Box(modifier = Modifier.weight(1f)) {
                        DestinationCard(dest)
                    }
                }
                if (pagesRow1[pageIndex].size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hàng thứ hai - Hoạt động độc lập
        HorizontalPager(
            state = pagerState2,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pagesRow2[pageIndex].forEach { dest ->
                    Box(modifier = Modifier.weight(1f)) {
                        DestinationCard(dest)
                    }
                }
                if (pagesRow2[pageIndex].size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DestinationCard(dest: Destination) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = dest.imageRes),
                contentDescription = dest.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    dest.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFF2563EB), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(dest.location, color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
