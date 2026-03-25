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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.model.TourType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourDetailScreen(
    tour: Tour,
    onBack: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFF2563EB)

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
            // 2. Slider ảnh (Carousel giả lập)
            ImageCarousel(tour.imageRes)

            Column(modifier = Modifier.padding(20.dp)) {
                // 3. Đánh giá (Rating Badge)
                RatingSection(tour.rating, tour.reviewCount)

                Spacer(modifier = Modifier.height(16.dp))

                Text(tour.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Thông tin nhanh (Quick Info Grid)
                QuickInfoGrid(tour)

                Spacer(modifier = Modifier.height(32.dp))

                // 5. "Tour bao gồm"
                InclusionsSection()

                Spacer(modifier = Modifier.height(32.dp))

                // 7. Lịch trình tour (Accordion)
                ItinerarySection()

                Spacer(modifier = Modifier.height(32.dp))

                // 9. Đánh giá khách hàng
                CustomerReviewsSection()

                Spacer(modifier = Modifier.height(32.dp))

                // 12. Tour gợi ý (Horizontal scroll)
                SuggestedToursSection()

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showBookingSheet) {
            BookingBottomSheet(
                tourPrice = tour.price,
                onDismiss = { showBookingSheet = false },
                onConfirm = { /* Handle Booking */ }
            )
        }
    }
}

@Composable
fun ImageCarousel(imageRes: Int) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Indicator giả lập
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (index == 0) Color.White else Color.White.copy(alpha = 0.5f))
                )
            }
        }
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        InfoItem(Icons.Default.LocationOn, "Khởi hành", tour.location)
        InfoItem(Icons.Default.DirectionsCar, "Phương tiện", "Xe du lịch")
        InfoItem(Icons.Default.ConfirmationNumber, "Mã tour", "VN-${tour.id}024")
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InclusionsSection() {
    Column {
        Text("Tour bao gồm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        val inclusions = listOf("Vé tham quan các điểm", "Xe đưa đón đời mới", "Hướng dẫn viên nhiệt tình", "Bảo hiểm du lịch", "Nước uống đóng chai")
        inclusions.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(item, fontSize = 14.sp, color = Color(0xFF475569))
            }
        }
    }
}

@Composable
fun ItinerarySection() {
    Column {
        Text("Lịch trình tour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ItineraryItem("Ngày 1: Khởi hành & Tham quan điểm 1", "Bắt đầu hành trình đón khách tại điểm hẹn, di chuyển tham quan và dùng bữa trưa đặc sản.")
        ItineraryItem("Ngày 2: Khám phá & Trải nghiệm", "Tiếp tục hành trình với các hoạt động trải nghiệm văn hóa và mua sắm quà lưu niệm.")
    }
}

@Composable
fun ItineraryItem(day: String, detail: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(day, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(detail, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun CustomerReviewsSection() {
    Column {
        Text("Đánh giá khách hàng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        repeat(2) {
            ReviewCard()
            Spacer(modifier = Modifier.height(12.dp))
        }
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
                Text("Tour rất tuyệt vời, HDV rất nhiệt tình và chu đáo. Gia đình tôi rất hài lòng!", fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun SuggestedToursSection() {
    Column {
        Text("Có thể bạn thích", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) {
                SuggestedTourItem()
            }
        }
    }
}

@Composable
fun SuggestedTourItem() {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.a5),
                contentDescription = null,
                modifier = Modifier.height(120.dp).fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Tour Huế 1 Ngày", fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text("Từ 1.200.000đ", color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
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
                Text("Tổng giá từ", fontSize = 12.sp, color = Color.Gray)
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
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val totalPrice = (adultCount * tourPrice) + (childCount * tourPrice * 0.7).toLong()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
            Text("Chọn số lượng khách", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            CounterItem("Người lớn", "Từ 12 tuổi", adultCount) { adultCount = it }
            Spacer(modifier = Modifier.height(16.dp))
            CounterItem("Trẻ em", "2 - 11 tuổi", childCount) { childCount = it }

            Spacer(modifier = Modifier.height(32.dp))

            Divider(color = Color(0xFFF1F5F9))
            
            Row(
                modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng tạm tính", fontWeight = FontWeight.Bold)
                Text(formatter.format(totalPrice), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Xác nhận đặt tour", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
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
