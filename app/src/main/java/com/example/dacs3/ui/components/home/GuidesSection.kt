package com.example.dacs3.ui.components.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import com.example.dacs3.R
import com.example.dacs3.data.model.Guide
import com.example.dacs3.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GuidesSection(viewModel: MainViewModel) {
    val guides by viewModel.guides.collectAsState()
    
    // Fallback data if DB is empty
    val displayGuides = guides.ifEmpty {
        listOf(
            Guide(name = "Nguyễn Văn An", bio = "Chuyên gia Văn hóa Hội An", imageRes = R.drawable.a8),
            Guide(name = "Lê Thị Lan", bio = "Hướng dẫn viên bản địa Sapa", imageRes = R.drawable.a9)
        )
    }

    val pages = displayGuides.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var selectedGuide by remember { mutableStateOf<Guide?>(null) }

    Column {
        Text(
            text = "Người đồng hành cùng bạn",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(14.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pages[pageIndex].forEach { guide ->
                    Box(modifier = Modifier.weight(1f)) {
                        GuideCard(guide, onInfoClick = { selectedGuide = guide })
                    }
                }
                if (pages[pageIndex].size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (selectedGuide != null) {
        GuideExperienceDialog(
            guide = selectedGuide!!,
            viewModel = viewModel,
            onDismiss = { selectedGuide = null }
        )
    }
}

@Composable
fun GuideCard(guide: Guide, onInfoClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                if (guide.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = guide.imageUrl,
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (guide.imageRes != 0) {
                    Image(
                        painter = painterResource(id = guide.imageRes),
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = "https://ui-avatars.com/api/?name=${guide.name}&background=random&size=200",
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = guide.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = guide.bio.ifEmpty { "Chưa có giới thiệu" },
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2563EB), CircleShape)
                            .clickable { onInfoClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Xem chi tiết",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuideExperienceDialog(guide: Guide, viewModel: MainViewModel, onDismiss: () -> Unit) {
    val reviews by viewModel.guideReviews.collectAsState()
    
    LaunchedEffect(guide.userId) {
        viewModel.loadReviewsForGuide(guide.userId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp)) {
                    if (guide.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = guide.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (guide.imageRes != 0) {
                        Image(
                            painter = painterResource(id = guide.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = "https://ui-avatars.com/api/?name=${guide.name}&background=random&size=200",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = guide.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text("Hướng dẫn viên", fontSize = 12.sp, color = Color.Gray)
                    
                    if (guide.sdt.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(guide.sdt, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    if (guide.email.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(guide.email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                var selectedTab by remember { mutableStateOf(0) }
                
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Thông tin", modifier = Modifier.padding(8.dp), fontSize = 14.sp)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Đánh giá (${reviews.size})", modifier = Modifier.padding(8.dp), fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            if (guide.bio.isNotEmpty()) {
                                Text("Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(guide.bio, fontSize = 13.sp, color = Color(0xFF475569))
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (guide.skills.isNotEmpty()) {
                                Text("Kỹ năng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    guide.skills.forEach { skill ->
                                        AssistChip(
                                            onClick = { },
                                            label = { Text(skill, fontSize = 11.sp) },
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Text("Kinh nghiệm làm việc", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        if (guide.experiences.isEmpty()) {
                            item { Text("Chưa có thông tin kinh nghiệm.", color = Color.Gray, fontSize = 13.sp) }
                        } else {
                            items(guide.experiences) { exp ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = exp.title,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        fontSize = 14.sp
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
                                            fontSize = 13.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Reviews Tab
                    if (reviews.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Chưa có đánh giá nào từ các tour.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(reviews) { reviewPair ->
                                ReviewItem(reviewPair.first, reviewPair.second)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun ReviewItem(review: com.example.dacs3.data.model.Review, tourTitle: String? = null) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdf.format(Date(review.createdAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = review.userAvatar ?: "https://ui-avatars.com/api/?name=${review.userName}&background=random",
                contentDescription = null,
                modifier = Modifier.size(32.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, fontSize = 11.sp, color = Color.Gray)
                    if (!tourTitle.isNullOrEmpty()) {
                        Text(" • ", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = tourTitle,
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${review.rating}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFFFB800)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (review.comment.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 13.sp, 
                color = Color(0xFF475569),
                lineHeight = 18.sp
            )
        }
    }
}
