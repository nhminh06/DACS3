package com.example.dacs3.ui.screens.tours

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.tours.FilterContent
import com.example.dacs3.ui.components.tours.FilterTag
import com.example.dacs3.ui.components.tours.TourCard
import com.example.dacs3.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourScreen(
    onNavigate: (String) -> Unit,
    onTourClick: (Tour) -> Unit,
    viewModel: MainViewModel
) {
    val tours by viewModel.tours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Phân trang
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 6
    val totalPages = maxOf(1, (tours.size + itemsPerPage - 1) / itemsPerPage)

    // Filter states
    val selectedTourType by viewModel.selectedTourType.collectAsState()
    val selectedScale by viewModel.selectedScale.collectAsState()

    // Reset về trang 1 khi danh sách tour thay đổi
    LaunchedEffect(tours) {
        currentPage = 1
    }

    val pagedTours = remember(tours, currentPage) {
        val startIndex = (currentPage - 1) * itemsPerPage
        tours.drop(startIndex).take(itemsPerPage)
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(currentScreen = "tours", onNavigate = onNavigate)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            LazyColumn(
                state = listState,
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
                                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
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
                                
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text("Tìm tour, địa điểm...", color = Color(0xFF475569), fontSize = 14.sp)
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        textStyle = TextStyle(color = Color(0xFF1E293B), fontSize = 14.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                                    )
                                }
                                
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
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lọc nhanh:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterTag(
                                    text = "Trong ngày", 
                                    isSelected = selectedTourType == "Trong ngày",
                                    onClick = { viewModel.setTourType(if (selectedTourType == "Trong ngày") "Tất cả" else "Trong ngày") }
                                )
                            }
                            item {
                                FilterTag(
                                    text = "Dài ngày", 
                                    isSelected = selectedTourType == "Dài ngày",
                                    onClick = { viewModel.setTourType(if (selectedTourType == "Dài ngày") "Tất cả" else "Dài ngày") }
                                )
                            }
                            item {
                                FilterTag(
                                    text = "Tour nhỏ", 
                                    isSelected = selectedScale == "Tour nhỏ",
                                    onClick = { viewModel.setTourScale(if (selectedScale == "Tour nhỏ") "Tất cả" else "Tour nhỏ") }
                                )
                            }
                            item {
                                FilterTag(
                                    text = "Tour vừa", 
                                    isSelected = selectedScale == "Tour vừa",
                                    onClick = { viewModel.setTourScale(if (selectedScale == "Tour vừa") "Tất cả" else "Tour vừa") }
                                )
                            }
                            item {
                                FilterTag(
                                    text = "Tour lớn", 
                                    isSelected = selectedScale == "Tour lớn",
                                    onClick = { viewModel.setTourScale(if (selectedScale == "Tour lớn") "Tất cả" else "Tour lớn") }
                                )
                            }
                        }
                    }
                }

                if (tours.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Không có tour nào khả dụng", color = Color(0xFF64748B))
                        }
                    }
                }

                items(pagedTours) { tour ->
                    TourCard(tour = tour, onClick = { onTourClick(tour) })
                }

                if (totalPages > 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { if (currentPage > 1) { currentPage--; coroutineScope.launch { listState.animateScrollToItem(1) } } },
                                enabled = currentPage > 1,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF1E293B))
                            ) { Text("Trước", fontWeight = FontWeight.Bold) }

                            Box(modifier = Modifier.padding(horizontal = 20.dp).background(Color(0xFF2563EB).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text(text = "$currentPage / $totalPages", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            FilledTonalButton(
                                onClick = { if (currentPage < totalPages) { currentPage++; coroutineScope.launch { listState.animateScrollToItem(1) } } },
                                enabled = currentPage < totalPages,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE2E8F0), contentColor = Color(0xFF1E293B))
                            ) { Text("Sau", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2563EB))
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                FilterContent(viewModel = viewModel, onApply = { showFilterSheet = false })
            }
        }
    }
}
