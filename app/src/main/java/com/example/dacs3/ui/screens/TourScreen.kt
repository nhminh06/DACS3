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
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.tours.FilterContent
import com.example.dacs3.ui.components.tours.FilterTag
import com.example.dacs3.ui.components.tours.TourCard
import com.example.dacs3.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourScreen(
    onNavigate: (String) -> Unit,
    onTourClick: (Tour) -> Unit,
    viewModel: MainViewModel
) {
    val tours by viewModel.tours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = "tours", onNavigate = onNavigate)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.a2),
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

                if (tours.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Không có tour nào khả dụng", color = Color.Gray)
                        }
                    }
                }

                items(tours) { tour ->
                    TourCard(
                        tour = tour,
                        onClick = { onTourClick(tour) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
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
