package com.example.dacs3.ui.components.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.R
import com.example.dacs3.data.model.Tour
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpecialOffersSection(
    tours: List<Tour>,
    onTourClick: (Tour) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    if (tours.isEmpty()) return

    val pages = tours.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF4D4D),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ưu đãi đặc biệt",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            TextButton(onClick = onSeeAllClick) {
                Text(
                    text = "Xem tất cả",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pages[pageIndex].forEach { tour ->
                    Box(modifier = Modifier.weight(1f)) {
                        SpecialOfferCard(tour = tour, onClick = { onTourClick(tour) })
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
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SpecialOfferCard(tour: Tour, onClick: () -> Unit) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val priceStr = "${numberFormat.format(tour.price ?: 0)} đ"
    val originalPriceStr = "${numberFormat.format(tour.originalPrice)} đ"

    val displayImage = when {
        tour.offerImageUrl.isNotEmpty() -> tour.offerImageUrl
        tour.imageUrl.isNotEmpty() -> tour.imageUrl
        else -> tour.imageRes
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(displayImage)
                    .crossfade(true)
                    .placeholder(R.drawable.a5)
                    .error(R.drawable.a5)
                    .build(),
                contentDescription = tour.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Lớp phủ Gradient để văn bản luôn rõ trên cả nền sáng và tối
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x14000000),
                                0.35f to Color(0x00000000),
                                0.65f to Color(0x8C000000),
                                1.0f to Color(0xD1000000)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xFFFF5252),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = tour.discountTag,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tour.title,
                        color = Color.White, // Luôn trắng trên nền tối
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = priceStr,
                            color = Color(0xFFEF2E2E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = originalPriceStr,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Kết thúc sau",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = tour.timeLeft,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
