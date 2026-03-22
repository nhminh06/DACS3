package com.example.dacs3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.model.TourType
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.tours.FilterContent
import com.example.dacs3.ui.components.tours.FilterTag
import com.example.dacs3.ui.components.tours.TourCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourScreen(onNavigate: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    
    val tours = remember {
        listOf(
            Tour("1", "Đà Nẵng - Hội An - Bà Nà Hills", R.drawable.a4, "25/05/2024", 9.2, 120, 3500000, "3 ngày 2 đêm", "Đà Nẵng", TourType.MULTI_DAY),
            Tour("2", "Khám phá Cố đô Huế", R.drawable.a7, "28/05/2024", 8.5, 85, 1200000, "1 ngày", "Huế", TourType.DAY_TOUR),
            Tour("3", "Tour Đảo Lý Sơn Kỳ Thú", R.drawable.a8, "01/06/2024", 8.8, 64, 2800000, "2 ngày 1 đêm", "Quảng Ngãi", TourType.MULTI_DAY),
            Tour("4", "Hành trình Di sản Hội An", R.drawable.a2, "Hàng ngày", 9.5, 210, 500000, "4 giờ", "Hội An", TourType.DAY_TOUR)
        )
    }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = "tours", onNavigate = onNavigate)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color(0xFFF8FAFC))
        ) {
            // Header Matched with previous screens
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.a2), // Changed from a1 to a2
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )

                    // Search Bar & Filter
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Tìm tour, địa điểm...", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            
                            VerticalDivider(modifier = Modifier.padding(vertical = 12.dp).width(1.dp), color = Color.LightGray)
                            
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(Icons.Default.FilterList, null, tint = Color(0xFF2563EB))
                            }
                        }
                    }

                    // Title
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, bottom = 32.dp)
                    ) {
                        Text(
                            text = "Hành trình\nTrải nghiệm",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp
                        )
                    }
                }
            }

            // Quick Filters Tags
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTag("Đà Nẵng", true)
                    FilterTag("Giá tốt nhất", false)
                    FilterTag("≥ 8.0", false)
                }
            }

            // Tour List
            items(tours) { tour ->
                TourCard(tour)
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                FilterContent(onApply = { showFilterSheet = false })
            }
        }
    }
}
