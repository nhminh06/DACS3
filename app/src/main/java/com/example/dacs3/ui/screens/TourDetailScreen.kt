package com.example.dacs3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.R
import com.example.dacs3.data.model.Tour
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    tour: Tour,
    onBack: () -> Unit,
    onNavigateToBooking: (Int, Int, Int) -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tour.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                    IconButton(onClick = { /* Share */ }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BookingBottomBar(tour.price) { showBookingSheet = true }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            ImageCarousel(tour)

            Column(modifier = Modifier.padding(20.dp)) {
                RatingSection(tour.rating, tour.reviewCount)

                Spacer(modifier = Modifier.height(16.dp))

                Text(tour.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))

                Spacer(modifier = Modifier.height(20.dp))

                QuickInfoGrid(tour)

                if (tour.traiNghiem.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionTitleWithIcon(Icons.Default.Explore, "Trải nghiệm nổi bật")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailTextWithTicks(tour.traiNghiem)
                }

                if (tour.dichVu.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionTitleWithIcon(Icons.Default.Beenhere, "Dịch vụ bao gồm")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailTextWithTicks(tour.dichVu)
                }

                if (tour.loTrinh.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionTitleWithIcon(Icons.Default.Map, "Lịch trình chi tiết")
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Text(
                            tour.loTrinh, 
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp, 
                            color = Color.DarkGray, 
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                CustomerReviewsSection()

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showBookingSheet) {
            BookingBottomSheet(
                tourPrice = tour.price,
                tour = tour,
                onDismiss = { showBookingSheet = false },
                onConfirm = { adults, children, infants ->
                    showBookingSheet = false
                    onNavigateToBooking(adults, children, infants) 
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(tour: Tour) {
    val images = remember(tour) {
        val list = mutableListOf<String>()
        if (tour.imageUrl.isNotEmpty()) list.add(tour.imageUrl)
        list.addAll(tour.banners)
        if (list.isEmpty()) list.add("") 
        list.take(5)
    }
    
    val pagerState = rememberPagerState(pageCount = { images.size })

    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(images[pageIndex])
                    .crossfade(true)
                    .error(R.drawable.a5)
                    .placeholder(R.drawable.a5)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitleWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

@Composable
fun DetailTextWithTicks(text: String) {
    val lines = text.split("\n", "- ", "* ").filter { it.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (lines.isEmpty() && text.isNotBlank()) {
            TickItem(text)
        } else {
            lines.forEach { line ->
                TickItem(line.trim())
            }
        }
    }
}

@Composable
fun TickItem(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.CheckCircle, 
            contentDescription = null, 
            tint = Color(0xFF10B981), 
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text, 
            fontSize = 14.sp, 
            color = Color(0xFF475569), 
            lineHeight = 20.sp
        )
    }
}

@Composable
fun RatingSection(rating: Double, reviews: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = Color(0xFF2563EB),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = rating.toString(),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text("Tuyệt vời", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2563EB))
            Text("($reviews đánh giá)", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickInfoGrid(tour: Tour) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoItem(Icons.Default.LocationOn, "Khởi hành", tour.diemKhoiHanh.ifEmpty { "Liên hệ" })
            InfoItem(Icons.Default.DirectionsCar, "Phương tiện", "Xe du lịch")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            InfoItem(Icons.Default.ConfirmationNumber, "Mã tour", tour.maTour.ifEmpty { "N/A" })
            InfoItem(Icons.Default.CalendarToday, "Thời gian", tour.duration)
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(160.dp)) {
        Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CustomerReviewsSection() {
    Column {
        Text("Đánh giá khách hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ReviewCard()
    }
}

@Composable
fun ReviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFFF1F5F9)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nguyễn Văn A", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFE9BC3C), modifier = Modifier.size(14.dp))
                        Text(" 5.0", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Dịch vụ rất tốt, gia đình tôi đã có một chuyến đi tuyệt vời!", fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun BookingBottomBar(price: Long, onBook: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Giá từ", fontSize = 12.sp, color = Color.Gray)
                Text(formatter.format(price), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
            }
            Button(
                onClick = onBook,
                modifier = Modifier.height(50.dp).width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Đặt ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingBottomSheet(
    tourPrice: Long,
    tour: Tour,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    var infantCount by remember { mutableIntStateOf(0) }
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    val priceTreEm = if (tour.giaTreEm > 0) tour.giaTreEm else (tourPrice * 0.7).toLong()
    val priceTreSoSinh = if (tour.giaTreNho > 0) tour.giaTreNho else (tourPrice * 0.5).toLong()
    val totalPrice = (adultCount * tourPrice) + (childCount * priceTreEm) + (infantCount * priceTreSoSinh)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
            Text("Chọn số lượng khách", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            CounterItem("Người lớn", "Giá: ${formatter.format(tourPrice)}", adultCount) { adultCount = it }
            Spacer(modifier = Modifier.height(16.dp))
            CounterItem("Trẻ em", "Giá: ${formatter.format(priceTreEm)}", childCount) { childCount = it }
            Spacer(modifier = Modifier.height(16.dp))
            CounterItem("Trẻ sơ sinh", "Giá: ${formatter.format(priceTreSoSinh)}", infantCount) { infantCount = it }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = Color(0xFFF1F5F9))
            
            Row(
                modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng tạm tính", fontWeight = FontWeight.Bold)
                Text(formatter.format(totalPrice), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
            }
            
            Button(
                onClick = { onConfirm(adultCount, childCount, infantCount) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Xác nhận đặt tour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CounterItem(title: String, subtitle: String, count: Int, onCountChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                enabled = count > 0
            ) { Icon(Icons.Default.RemoveCircleOutline, null, tint = if (count > 0) Color(0xFF2563EB) else Color.Gray) }
            Text(count.toString(), modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            IconButton(onClick = { onCountChange(count + 1) }) {
                Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF2563EB))
            }
        }
    }
}
