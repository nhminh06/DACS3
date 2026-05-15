package com.example.dacs3.ui.components.guides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.R
import com.example.dacs3.data.model.Guide
import com.example.dacs3.ui.components.home.ReviewItem
import com.example.dacs3.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GuideDetailScreen(
    guide: Guide,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val reviews by viewModel.guideReviews.collectAsState()

    LaunchedEffect(guide.userId) {
        viewModel.loadReviewsForGuide(guide.userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ người đồng hành", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Avatar and Basic Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(90.dp)) {
                        if (guide.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = guide.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        } else if (guide.imageRes != 0) {
                            Image(
                                painter = painterResource(id = guide.imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=${guide.name}&background=random&size=200",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = guide.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text("Hướng dẫn viên chuyên nghiệp", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Info Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (guide.sdt.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF2563EB))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(guide.sdt, fontSize = 14.sp, color = Color(0xFF475569))
                            }
                        }
                        
                        if (guide.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(guide.bio, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 20.sp)
                        }

                        if (guide.skills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Kỹ năng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                guide.skills.forEach { skill ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(skill, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(100.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Experience Section
                Text("Kinh nghiệm làm việc", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))
                
                if (guide.experiences.isEmpty()) {
                    Text("Chưa có thông tin kinh nghiệm.", color = Color.Gray, fontSize = 14.sp)
                } else {
                    guide.experiences.forEach { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = exp.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${exp.startTime} - ${exp.endTime.ifEmpty { "Hiện tại" }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Medium
                                )
                                if (exp.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = exp.description,
                                        fontSize = 14.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews Section
                Text("Đánh giá từ khách hàng (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))

                if (reviews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.White, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có đánh giá nào.", color = Color.Gray)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.forEach { reviewPair ->
                            ReviewItem(reviewPair.first, reviewPair.second)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
