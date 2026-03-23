package com.example.dacs3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Article
import com.example.dacs3.data.model.ArticleCategory
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.articles.ArticleCategoryTabs
import com.example.dacs3.ui.components.articles.ArticleItem

@Composable
fun ArticleExplorerScreen(
    onNavigate: (String) -> Unit,
    onArticleClick: (Article) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ArticleCategory.CULTURE) }
    
    val allArticles = remember {
        listOf(
            Article(
                "Nghệ thuật Tuồng cổ Quảng Nam",
                "Khám phá nét đặc sắc của bộ môn nghệ thuật sân khấu truyền thống với những mặt nạ vẽ cầu kỳ và điệu bộ ước lệ.",
                R.drawable.a5,
                ArticleCategory.CULTURE
            ),
            Article(
                "Lễ hội Cầu Ngư ven biển",
                "Tìm hiểu về tín ngưỡng thờ cá Ông của ngư dân miền Trung qua lễ hội cầu ngư linh thiêng và sôi động.",
                R.drawable.a6,
                ArticleCategory.CULTURE
            ),
            Article(
                "Làng gốm Thanh Hà 500 năm tuổi",
                "Trải nghiệm quy trình làm gốm thủ công từ đất sét sông Thu Bồn tại ngôi làng cổ bên dòng sông Hoài.",
                R.drawable.a7,
                ArticleCategory.CRAFT_VILLAGE
            ),
            Article(
                "Làng lụa Mã Châu danh tiếng",
                "Dòng lụa tiến vua nổi tiếng với sự mềm mại, tinh xảo được tạo ra từ bàn tay tài hoa của các nghệ nhân.",
                R.drawable.a2,
                ArticleCategory.CRAFT_VILLAGE
            ),
            Article(
                "Mì Quảng - Hồn đất Quảng",
                "Bí quyết tạo nên sợi mì dai ngon và nước lèo đậm đà đặc trưng của món ăn nổi tiếng nhất vùng đất này.",
                R.drawable.a1,
                ArticleCategory.CUISINE
            ),
            Article(
                "Cao lầu Hội An - Hương vị xưa",
                "Món ăn đặc sản with sợi mì vàng óng được làm từ nước giếng Bá Lễ và tro củi cù lao Chàm.",
                R.drawable.a3,
                ArticleCategory.CUISINE
            )
        )
    }
    
    val filteredArticles = allArticles.filter { it.category == selectedCategory }

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
                            Text(
                                text = "Tìm kiếm bài viết văn hóa...",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
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
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = when(selectedCategory) {
                        ArticleCategory.CULTURE -> "Di sản văn hóa tiêu biểu"
                        ArticleCategory.CRAFT_VILLAGE -> "Tinh hoa làng nghề Việt"
                        ArticleCategory.CUISINE -> "Ẩm thực vùng miền đặc sắc"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            items(filteredArticles) { article ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    ArticleItem(
                        article = article,
                        onClick = { onArticleClick(article) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
