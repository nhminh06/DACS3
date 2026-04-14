package com.example.dacs3.ui.screens.articles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.R
import com.example.dacs3.data.model.ArticleCategory
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.articles.ArticleCategoryTabs
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import kotlin.math.ceil

@Composable
fun ArticleExplorerScreen(
    onNavigate: (String) -> Unit,
    onArticleClick: (ArticleEntity) -> Unit,
    articleViewModel: ArticleViewModel = viewModel(),
    initialCategory: ArticleCategory = ArticleCategory.CULTURE
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val allArticles by articleViewModel.explorerArticles.collectAsState()
    val isLoading by articleViewModel.isLoading.collectAsState()
    val searchQuery by articleViewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Phân trang: 3 bài mỗi trang
    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 3
    
    val filteredArticles = allArticles.filter { article ->
        if (searchQuery.isNotBlank()) {
            article.tieu_de.lowercase().contains(searchQuery.lowercase().trim())
        } else {
            val category = when(article.loai_id) {
                1 -> ArticleCategory.CRAFT_VILLAGE
                2 -> ArticleCategory.CUISINE
                3 -> ArticleCategory.CULTURE
                else -> ArticleCategory.CULTURE
            }
            category == selectedCategory
        }
    }

    val totalPages = ceil(filteredArticles.size.toDouble() / itemsPerPage).toInt().coerceAtLeast(1)
    val pagedArticles = filteredArticles.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)

    // Reset trang khi đổi category hoặc tìm kiếm
    LaunchedEffect(selectedCategory, searchQuery) {
        currentPage = 1
    }

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentScreen = "explore",
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
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
                        painter = painterResource(id = R.drawable.a4),
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
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Tìm kiếm bài viết văn hóa...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { articleViewModel.setSearchQuery(it) },
                                    textStyle = TextStyle(color = Color(0xFF1E293B), fontSize = 14.sp),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                                )
                            }
                            
                            IconButton(onClick = { }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 24.dp, bottom = 32.dp)
                    ) {
                        Text(
                            text = "Văn hóa & Di sản\nMiền Trung",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                            letterSpacing = 0.sp
                        )
                    }
                }
            }

            if (searchQuery.isBlank()) {
                item {
                    Surface(
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        ArticleCategoryTabs(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (searchQuery.isNotBlank()) "Kết quả tìm kiếm" else when(selectedCategory) {
                        ArticleCategory.CULTURE -> "Di sản văn hóa tiêu biểu"
                        ArticleCategory.CRAFT_VILLAGE -> "Tinh hoa làng nghề Việt"
                        ArticleCategory.CUISINE -> "Ẩm thực vùng miền đặc sắc"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (filteredArticles.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Không tìm thấy bài viết nào phù hợp", color = Color.Gray)
                        }
                    }
                } else {
                    items(pagedArticles) { article ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                            ExplorerArticleItem(
                                article = article,
                                onClick = { onArticleClick(article) }
                            )
                        }
                    }

                    // Điều khiển phân trang
                    item {
                        PaginationControls(
                            currentPage = currentPage,
                            totalPages = totalPages,
                            onPageChange = { currentPage = it }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Trước
        FilledIconButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB),
                disabledContainerColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Hiển thị số trang
        repeat(totalPages) { index ->
            val page = index + 1
            // Hiển thị tối đa 5 nút trang xung quanh trang hiện tại
            if (page == 1 || page == totalPages || (page >= currentPage - 1 && page <= currentPage + 1)) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (currentPage == page) Color(0xFF2563EB) else Color.White)
                        .clickable { onPageChange(page) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page.toString(),
                        color = if (currentPage == page) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else if (page == currentPage - 2 || page == currentPage + 2) {
                Text("...", color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nút Sau
        FilledIconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB),
                disabledContainerColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ExplorerArticleItem(article: ArticleEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            val firstImage = article.sections.firstOrNull()?.get("hinh_anh")
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(firstImage)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Khám phá",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mới nhất",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = article.tieu_de,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val firstContent = article.sections.firstOrNull()?.get("noi_dung") ?: ""
                Text(
                    text = firstContent,
                    color = Color(0xFF475569),
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider(color = Color(0xFFF1F5F9))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Đọc thêm",
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Text(
                        text = "3 phút đọc",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
