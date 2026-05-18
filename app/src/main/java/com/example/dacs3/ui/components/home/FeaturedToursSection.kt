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
import androidx.compose.ui.text.style.TextDecoration
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
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Xem tất cả",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ) {}
                }
            }
        }
    }
}

@Composable
fun HomeTourCard(tour: Tour, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val mainColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
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

                if (tour.isOffer && tour.discountTag.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 8.dp, end = 8.dp)
                            .align(Alignment.TopEnd),
                        color = Color(0xFFFF5722),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = tour.discountTag,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (tour.rating > 0 && tour.reviewCount > 0) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp)
                            .align(Alignment.TopStart),
                        color = Color.Black.copy(alpha = 0.5f),
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
                } else if (!tour.isOffer) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp)
                            .align(Alignment.TopStart),
                        color = mainColor.copy(alpha = 0.7f),
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
                Text(
                    text = tour.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Groups, null, tint = mainColor, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "${tour.minGuests}-${tour.maxGuests}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    }
                    Row(modifier = Modifier.weight(1.2f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = mainColor, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = tour.location, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = mainColor, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = tour.duration, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencyFormatter.format(tour.price ?: 0),
                        color = if (tour.isOffer) Color(0xFFEF4444) else mainColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    if (tour.isOffer && tour.originalPrice > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currencyFormatter.format(tour.originalPrice),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
