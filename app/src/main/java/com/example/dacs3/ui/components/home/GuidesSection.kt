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
fun GuidesSection(
    viewModel: MainViewModel,
    onSeeAllClick: () -> Unit = {}
) {
    val guides by viewModel.guides.collectAsState()

    val displayGuides = guides.ifEmpty {
        listOf(
            Guide(name = "Nguyễn Văn An", bio = "Chuyên gia Văn hóa Hội An", imageRes = R.drawable.a8),
            Guide(name = "Lê Thị Lan", bio = "Hướng dẫn viên bản địa Sapa", imageRes = R.drawable.a9),
            Guide(name = "Trần Minh", bio = "Thợ săn ảnh tại Đà Lạt", imageRes = R.drawable.a1),
            Guide(name = "Hương Giang", bio = "Khám phá ẩm thực Huế", imageRes = R.drawable.a2)
        )
    }

    val pages = displayGuides.chunked(2)
    val pagerState = rememberPagerState(pageCount = { pages.size })
    var selectedGuide by remember { mutableStateOf<Guide?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Người đồng hành",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Đội ngũ chuyên nghiệp",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Xem thêm",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 16.dp
        ) { pageIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        if (pages.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(if (isSelected) 16.dp else 5.dp),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ) {}
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
            .height(280.dp)
            .clickable { onInfoClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (guide.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = guide.imageUrl,
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (guide.imageRes != 0) {
                    Image(
                        painter = painterResource(id = guide.imageRes),
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = "https://ui-avatars.com/api/?name=${guide.name}&background=random&size=200",
                        contentDescription = guide.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                startY = 200f
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = guide.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = guide.bio.ifEmpty { "Chưa có giới thiệu" },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Xem hồ sơ",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Xem chi tiết",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
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
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("Hướng dẫn viên", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (guide.sdt.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(guide.sdt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp)) {
                var selectedTab by remember { mutableStateOf(0) }

                TabRow(
                    selectedTabIndex = selectedTab, 
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0, 
                        onClick = { selectedTab = 0 },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text("Thông tin", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                    Tab(
                        selected = selectedTab == 1, 
                        onClick = { selectedTab = 1 },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text("Đánh giá (${reviews.size})", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            if (guide.bio.isNotEmpty()) {
                                Text("Giới thiệu", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(guide.bio, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (guide.skills.isNotEmpty()) {
                                Text("Kỹ năng", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
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

                            Text("Kinh nghiệm làm việc", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (guide.experiences.isEmpty()) {
                            item { Text("Chưa có thông tin kinh nghiệm.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }
                        } else {
                            items(guide.experiences) { exp ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = exp.title,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${exp.startTime} - ${exp.endTime.ifEmpty { "Hiện tại" }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (exp.description.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = exp.description,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (reviews.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Chưa có đánh giá nào từ các tour.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(reviews) { reviewPair ->
                                ReviewItem(reviewPair.first, reviewPair.second)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Đóng")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ReviewItem(review: com.example.dacs3.data.model.Review, tourTitle: String? = null) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdf.format(Date(review.createdAt))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = review.userAvatar ?: "https://ui-avatars.com/api/?name=${review.userName}&background=random",
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!tourTitle.isNullOrEmpty()) {
                        Text(" • ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = tourTitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
