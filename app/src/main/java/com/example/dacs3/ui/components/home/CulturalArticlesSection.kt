package com.example.dacs3.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.viewmodel.ArticleViewModel

@Composable
fun CulturalArticlesSection(
    articleViewModel: ArticleViewModel = viewModel(),
    onSeeAllClick: () -> Unit = {},
    onArticleClick: (ArticleEntity) -> Unit = {}
) {
    val articles by articleViewModel.homeArticles.collectAsState()
    val isLoading by articleViewModel.isLoading.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bài viết văn hóa", 
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
        
        if (articles.isEmpty() && isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
            }
        } else {
            articles.forEach { article ->
                ArticleCard(
                    article = article,
                    onClick = { onArticleClick(article) }
                )
            }
            
            if (articles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { articleViewModel.loadMoreHomeArticles() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Tải thêm bài viết",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: ArticleEntity, onClick: () -> Unit) {
    val categoryName = when(article.loai_id) {
        1 -> "Làng nghề"
        2 -> "Ẩm thực"
        3 -> "Văn hóa"
        else -> "Khám phá"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val firstImage = article.sections.firstOrNull { it["hinh_anh"] != null }?.get("hinh_anh")
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(firstImage)
                    .crossfade(true)
                    .build(),
                contentDescription = article.tieu_de,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    article.tieu_de, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val firstContent = article.sections.firstOrNull()?.get("noi_dung") ?: ""
                Text(
                    firstContent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category and Date info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = categoryName,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (article.ngay_tao.isNotEmpty()) article.ngay_tao else "Mới",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Xem thêm", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
