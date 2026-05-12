package com.example.dacs3.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.R
import kotlinx.coroutines.delay

@Composable
fun PromoBannersSection(
    banners: List<Map<String, Any>> = emptyList()
) {
    // Tạo các nhóm 2 banner mỗi trang
    // Nếu không có data từ Firebase, dùng fallback local
    val bannerPages: List<List<Any>> = remember(banners) {
        if (banners.isNotEmpty()) {
            banners.chunked(2)
        } else {
            // Fallback: dùng ảnh local (thêm nhiều ảnh local tùy ý)
            listOf(
                listOf(R.drawable.a5, R.drawable.a5),
                listOf(R.drawable.a5, R.drawable.a5),
                listOf(R.drawable.a5, R.drawable.a5)
            )
        }
    }

    var currentPage by remember { mutableIntStateOf(0) }
    var visible by remember { mutableStateOf(true) }

    // Auto-rotate mỗi 5 giây với hiệu ứng fade
    LaunchedEffect(bannerPages.size) {
        if (bannerPages.size > 1) {
            while (true) {
                delay(10000L)
                // Fade out
                visible = false
                delay(400L)
                // Chuyển trang
                currentPage = (currentPage + 1) % bannerPages.size
                // Fade in
                visible = true
            }
        }
    }

    val currentGroup = bannerPages.getOrElse(currentPage) { emptyList() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Hàng 1
            val first = currentGroup.getOrNull(0)
            if (first != null) {
                PromoBannerCard(item = first)
            }
            // Hàng 2
            val second = currentGroup.getOrNull(1)
            if (second != null) {
                PromoBannerCard(item = second)
            }
        }
    }
}

@Composable
fun PromoBannerCard(item: Any) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 7f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        when (item) {
            is String -> {
                // URL từ Firebase/Cloudinary
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            is Map<*, *> -> {
                // Map từ Firestore
                val imageUrl = item["imageUrl"] as? String ?: ""
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
            is Int -> {
                // Ảnh local (drawable resource id)
                Image(
                    painter = painterResource(id = item),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}