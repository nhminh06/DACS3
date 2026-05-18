package com.example.dacs3.ui.components.guides

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(guide.userId) {
        viewModel.loadReviewsForGuide(guide.userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ người đồng hành", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                            listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(90.dp)) {
                        val avatarModifier = Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                        if (guide.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = guide.imageUrl,
                                contentDescription = null,
                                modifier = avatarModifier,
                                contentScale = ContentScale.Crop
                            )
                        } else if (guide.imageRes != 0) {
                            Image(
                                painter = painterResource(id = guide.imageRes),
                                contentDescription = null,
                                modifier = avatarModifier,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = "https://ui-avatars.com/api/?name=${guide.name}&background=random&size=200",
                                contentDescription = null,
                                modifier = avatarModifier,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = guide.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White // Keep white on colored background
                    )
                    Text("Hướng dẫn viên chuyên nghiệp", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Info Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Thông tin liên hệ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (guide.sdt.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = primaryColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(guide.sdt, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        if (guide.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(guide.bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                        }

                        if (guide.skills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Kỹ năng", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                guide.skills.forEach { skill ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(skill, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(100.dp),
                                        colors = AssistChipDefaults.assistChipColors(
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Experience Section
                Text("Kinh nghiệm làm việc", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (guide.experiences.isEmpty()) {
                    Text("Chưa có thông tin kinh nghiệm.", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
                } else {
                    guide.experiences.forEach { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = exp.title,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${exp.startTime} - ${exp.endTime.ifEmpty { "Hiện tại" }}",
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                                if (exp.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = exp.description,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews Section
                Text("Đánh giá từ khách hàng (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))

                if (reviews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có đánh giá nào.", color = MaterialTheme.colorScheme.outline)
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
