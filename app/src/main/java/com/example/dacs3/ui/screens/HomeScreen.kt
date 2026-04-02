package com.example.dacs3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.home.*
import com.example.dacs3.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AppHomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel,
    onTourClick: (Tour) -> Unit
) {
    val backgroundColor = Color(0xFFF1F5F9)
    val listState = rememberLazyListState()
    val tours by viewModel.tours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { 
            AppBottomBar(
                currentScreen = "home",
                onNavigate = onNavigate
            ) 
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Ảnh Header
                item { TopHeader() }

                // 2. Thanh tìm kiếm
                item {
                    HomePaddingWrapper {
                        SearchBar()
                    }
                }

                // 3. Thanh điều hướng nhanh (Quick Nav)
                item {
                    HomePaddingWrapper {
                        QuickNavSection(onItemClick = { /* Handle */ })
                    }
                }

                // 4. Danh mục
                item {
                    HomePaddingWrapper {
                        CategorySection()
                    }
                }

                // 5. Tour nổi bật (Thay thế cho Địa điểm nổi bật)
                if (tours.isNotEmpty()) {
                    item {
                        HomePaddingWrapper {
                            FeaturedToursSection(tours = tours, onTourClick = onTourClick)
                        }
                    }
                } else if (!isLoading) {
                    // Fallback if no tours from DB
                    item {
                        HomePaddingWrapper {
                            FeaturedDestinationsSection()
                        }
                    }
                }

                // 6. Bài viết văn hóa
                item {
                    HomePaddingWrapper {
                        CulturalArticlesSection()
                    }
                }

                // 7. Người đồng hành cùng bạn
                item {
                    HomePaddingWrapper {
                        GuidesSection()
                    }
                }

                // 8. Đánh giá khách hàng
                item {
                    HomePaddingWrapper {
                        ReviewsSection()
                    }
                }

                // 9. Form Liên hệ
                item {
                    HomePaddingWrapper {
                        ContactFormSection()
                    }
                }

                // 10. Lời chào kết thúc
                item {
                    ClosingGreeting()
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HomeTourCard(tour: Tour, onClick: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Adjusted height to fit 2 per row nicely
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (tour.imageUrl.isNotEmpty()) tour.imageUrl else tour.imageRes)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tour.rating.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = tour.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = tour.location,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currencyFormatter.format(tour.price),
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = tour.duration,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HomePaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
