package com.example.dacs3.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Article
import com.example.dacs3.data.model.ArticleCategory

@Composable
fun CulturalArticlesSection() {
    val articles = listOf(
        Article(
            "Tinh hoa Phở Việt", 
            "Phở không chỉ là một món ăn, mà còn là linh hồn của ẩm thực Việt Nam...", 
            R.drawable.a5,
            ArticleCategory.CUISINE
        ),
        Article(
            "Nét đẹp Tết truyền thống", 
            "Khám phá những phong tục độc đáo và ý nghĩa của ngày Tết cổ truyền...", 
            R.drawable.a6,
            ArticleCategory.CULTURE
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Bài viết văn hóa", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF1E293B))
        articles.forEach { article ->
            ArticleCard(article)
        }
    }
}

@Composable
fun ArticleCard(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = article.imageRes),
                contentDescription = article.title,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(article.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    article.desc,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xem thêm", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
