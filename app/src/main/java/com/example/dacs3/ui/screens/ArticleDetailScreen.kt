package com.example.dacs3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Article
import com.example.dacs3.data.model.ArticleCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    onBack: () -> Unit,
    onNavigateToTour: () -> Unit,
    isLoggedIn: Boolean = false
) {
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFF2563EB)
    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = onNavigateToTour,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Xem chuyến đi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Đổi sang nền trắng để hòa hợp với nội dung phẳng
            .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // 1. Header Image with Title overlay
                Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = article.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Text(
                        text = article.title,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 34.sp
                    )
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // 2. Main Content Sections - Flat style
                    ContentSection(
                        title = "Giới thiệu",
                        description = "Khám phá vẻ đẹp truyền thống và những nét đặc sắc chỉ có tại ${article.title}. Một hành trình mang đậm giá trị văn hóa lịch sử, nơi mỗi góc nhỏ đều kể lên một câu chuyện riêng biệt về đất và người.",
                        imageRes = article.imageRes
                    )
                    
                    ContentSection(
                        title = "Trải nghiệm đặc sắc",
                        description = "Đến đây, du khách sẽ được tận mắt chứng kiến quy trình tạo ra các sản phẩm độc đáo, tham gia các hoạt động cộng đồng và thưởng thức ẩm thực địa phương phong phú.",
                        imageRes = R.drawable.a4
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. "Có thể bạn quan tâm" - Flat Horizontal Scroll
                    Text(
                        "Có thể bạn quan tâm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(getSampleArticles()) { item ->
                            RecommendedItem(item)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // 5. Comment Section - Flat style
                    CommentSection(
                        isLoggedIn = isLoggedIn,
                        onReportClick = { showReportDialog = true }
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Báo cáo bình luận") },
            text = { Text("Bạn có chắc chắn muốn báo cáo nội dung này là không phù hợp?") },
            confirmButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Đồng ý", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ContentSection(title: String, description: String, imageRes: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 15.sp,
            color = Color(0xFF475569),
            lineHeight = 24.sp
        )
        if (imageRes != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}

@Composable
fun RecommendedItem(article: Article) {
    Column(modifier = Modifier.width(160.dp)) {
        Image(
            painter = painterResource(id = article.imageRes),
            contentDescription = null,
            modifier = Modifier
                .height(110.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            article.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
fun CommentSection(isLoggedIn: Boolean, onReportClick: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bình luận", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "(10)",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form nhập bình luận phẳng
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = isLoggedIn,
            placeholder = { Text(if (isLoggedIn) "Chia sẻ cảm nghĩ của bạn..." else "Bạn cần đăng nhập để bình luận", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {},
            enabled = isLoggedIn,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Gửi bình luận", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Danh sách bình luận phẳng
        repeat(3) {
            CommentItem(onReportClick = onReportClick)
            if (it < 2) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.2f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem thêm bình luận", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CommentItem(onReportClick: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFFF1F5F9)) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Người dùng", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Text("2 giờ trước", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onReportClick) { 
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray) 
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Bài viết rất hay và bổ ích, tôi sẽ sớm ghé thăm địa điểm này cùng gia đình vào mùa hè tới!",
            fontSize = 14.sp,
            color = Color(0xFF475569),
            lineHeight = 22.sp
        )
        
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text("12", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(20.dp))
            Text("Trả lời", fontSize = 13.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
        }
    }
}

fun getSampleArticles() = listOf(
    Article("Cố đô Huế", "Khám phá kinh thành", R.drawable.a5, ArticleCategory.CULTURE),
    Article("Phố cổ Hội An", "Nét đẹp hoài cổ", R.drawable.a6, ArticleCategory.CULTURE),
    Article("Vịnh Hạ Long", "Kỳ quan thiên nhiên", R.drawable.a7, ArticleCategory.CULTURE)
)
