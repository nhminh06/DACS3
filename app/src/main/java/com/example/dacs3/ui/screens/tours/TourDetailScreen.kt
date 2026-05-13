@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.example.dacs3.ui.screens.tours

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.R
import com.example.dacs3.data.model.Review
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.viewmodel.MainViewModel
import com.example.dacs3.ui.viewmodel.ReviewViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TourDetailScreen(
    tour: Tour,
    onBack: () -> Unit,
    onNavigateToBooking: (Int, Int, Int) -> Unit,
    reviewViewModel: ReviewViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    var isFavorite by remember { mutableStateOf(false) }
    var showBookingSheet by remember { mutableStateOf(false) }
    var isMapVisible by remember { mutableStateOf(false) }
    val allTours by mainViewModel.allTours.collectAsState()
    val liveTour = allTours.find { it.id == tour.id } ?: tour

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        tour.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color(0xFF0F172A)
                        )
                    }
                    IconButton(onClick = { /* Share */ }) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BookingBottomBar(
                price = tour.getPrice(),
                originalPrice = if (tour.isOffer) tour.originalPrice else 0
            ) { showBookingSheet = true }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RatingSection(tour.rating, tour.reviewCount)
                    
                    if (tour.isOffer && tour.discountTag.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFF5722),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tour.discountTag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    tour.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                if (tour.isOffer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ưu đãi kết thúc sau: ",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        CountdownTimer(timeLeftStr = liveTour.timeLeft)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                QuickInfoGrid(tour)

                if (tour.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { isMapVisible = !isMapVisible },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMapVisible) Color(0xFFE2E8F0) else Color(0xFFF1F5F9),
                            contentColor = Color(0xFF2563EB)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isMapVisible) "Ẩn bản đồ" else "Xem vị trí trên bản đồ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isMapVisible) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            MapWebView(location = tour.location, title = tour.title)
                        }
                    }
                }

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
                            color = Color(0xFF334155),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                CustomerReviewsSection(tour.id, reviewViewModel)

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showBookingSheet) {
            BookingBottomSheet(
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

@Composable
fun MapWebView(location: String, title: String) {
    val context = LocalContext.current
    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(location, title) {
        isLoading = true
        try {
            val geocoder = android.location.Geocoder(context, Locale.getDefault())
            val cleanTitle =
                title.replace(Regex("(?i)tour|du lịch|khám phá|chuyến đi|trọn gói|tại"), "")
                    .trim()
            val queries = mutableListOf<String>()
            if (cleanTitle.isNotEmpty()) {
                queries.add("$cleanTitle, $location")
                queries.add(cleanTitle)
            }
            queries.add(location)

            for (query in queries) {
                if (query.isBlank()) continue
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    latLng = Pair(addresses[0].latitude, addresses[0].longitude)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF2563EB), strokeWidth = 2.dp)
        } else if (latLng != null) {
            val staticMapUrl =
                "https://static-maps.yandex.ru/1.x/?lang=vi_VN&ll=${latLng!!.second},${latLng!!.first}&z=17&l=sat,skl&size=600,450&pt=${latLng!!.second},${latLng!!.first},pm2rdm"

            AsyncImage(
                model = ImageRequest.Builder(context).data(staticMapUrl).crossfade(true).build(),
                contentDescription = "Satellite Map Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:${latLng!!.first},${latLng!!.second}?q=${Uri.encode(title + " " + location)}")
                        )
                        context.startActivity(intent)
                    },
                contentScale = ContentScale.Crop
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (title.length > 25) title.take(25) + "..." else title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=${Uri.encode(title + " " + location)}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF2563EB)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.LocationOff,
                    null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Không tìm thấy: $title",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(title + " " + location)}")
                    )
                    context.startActivity(intent)
                }) {
                    Text("Tìm thủ công trên Maps", fontSize = 12.sp)
                }
            }
        }
    }
}

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
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
                            .background(
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitleWithIcon(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun DetailTextWithTicks(text: String) {
    val lines = text.split("\n", "- ", "* ").filter { it.isNotBlank() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (lines.isEmpty() && text.isNotBlank()) {
            TickItem(text)
        } else {
            lines.forEach { line -> TickItem(line.trim()) }
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
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 14.sp, color = Color(0xFF334155), lineHeight = 20.sp)
    }
}

@Composable
fun RatingSection(rating: Double, reviews: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (reviews > 0) {
            Surface(color = Color(0xFF2563EB), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = String.format("%.1f", rating),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "Đánh giá",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2563EB)
                )
                Text("($reviews đánh giá)", fontSize = 12.sp, color = Color(0xFF475569))
            }
        } else {
            Surface(color = Color(0xFF2563EB), shape = RoundedCornerShape(8.dp)) {
                Text(
                    text = "Mới",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Chưa có đánh giá", fontSize = 13.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun QuickInfoGrid(tour: Tour) {
    val scaleInfo = tour.getTourScaleInfo()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                Icons.Default.LocationOn,
                "Khởi hành",
                tour.diemKhoiHanh.ifEmpty { "Liên hệ" }
            )
            InfoItem(
                Icons.Default.DirectionsCar,
                if (scaleInfo != null) scaleInfo.label else "Phương tiện",
                if (scaleInfo != null) scaleInfo.transport else "Xe du lịch"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoItem(
                Icons.Default.ConfirmationNumber,
                "Mã tour",
                tour.maTour.ifEmpty { "N/A" }
            )
            InfoItem(Icons.Default.CalendarToday, "Thời gian", tour.duration)
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(160.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CustomerReviewsSection(tourId: String, reviewViewModel: ReviewViewModel) {
    val reviews by reviewViewModel.reviews.collectAsState()

    LaunchedEffect(tourId) {
        reviewViewModel.loadReviewsForTour(tourId)
    }

    Column {
        Text(
            "Đánh giá khách hàng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (reviews.isEmpty()) {
            Text(
                "Chưa có đánh giá nào cho tour này.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            reviews.forEach { review ->
                ReviewCard(review)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
@Composable
fun CountdownTimer(timeLeftStr: String) {
    // Parse HH:mm:ss → ms
    fun parseToMs(s: String): Long {
        if (s.isBlank() || s == "00:00:00") return 0L
        return try {
            val parts = s.trim().split(":").map { it.toLong() }
            when (parts.size) {
                3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000L
                else -> 0L
            }
        } catch (e: Exception) { 0L }
    }

    var remainingMs by remember(timeLeftStr) { mutableStateOf(parseToMs(timeLeftStr)) }
    LaunchedEffect(timeLeftStr) {
        remainingMs = parseToMs(timeLeftStr)
    }

    val display = when {
        remainingMs <= 0L -> "Đã hết hạn"
        else -> {
            val h = remainingMs / 3_600_000
            val m = (remainingMs % 3_600_000) / 60_000
            val s = (remainingMs % 60_000) / 1_000
            "%02d:%02d:%02d".format(h, m, s)
        }
    }

    Text(
        text = display,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = if (remainingMs <= 0L) Color(0xFF94A3B8) else Color(0xFFEF4444)
    )
}
@Composable
fun ReviewCard(review: Review) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = sdf.format(Date(review.createdAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = if (!review.userAvatar.isNullOrEmpty()) review.userAvatar else R.drawable.a9,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        review.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(review.rating) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFFE9BC3C),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Text(dateStr, fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(review.comment, fontSize = 13.sp, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun BookingBottomBar(price: Long, originalPrice: Long = 0, onBook: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Giá từ", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatter.format(price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2563EB)
                    )
                    if (originalPrice > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            formatter.format(originalPrice),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }
            Button(
                onClick = onBook,
                modifier = Modifier
                    .height(50.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Đặt ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun BookingBottomSheet(
    tour: Tour,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    var adultCount by remember { mutableIntStateOf(1) }
    var childCount by remember { mutableIntStateOf(0) }
    var infantCount by remember { mutableIntStateOf(0) }

    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val priceTreEm = if (tour.getGiaTreEm() > 0) tour.getGiaTreEm() else (tour.getPrice() * 0.7).toLong()
    val priceTreSoSinh = if (tour.getGiaTreNho() > 0) tour.getGiaTreNho() else (tour.getPrice() * 0.5).toLong()
    
    val origPriceTreEm = if (tour.originalPriceChild > 0) tour.originalPriceChild else (tour.originalPrice * 0.7).toLong()
    val origPriceTreSoSinh = if (tour.originalPriceInfant > 0) tour.originalPriceInfant else (tour.originalPrice * 0.5).toLong()

    val totalPrice = (adultCount * tour.getPrice()) + (childCount * priceTreEm) + (infantCount * priceTreSoSinh)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Chọn số lượng khách",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(24.dp))

            CounterItem(
                title = "Người lớn",
                subtitle = "Giá: ${formatter.format(tour.getPrice())}",
                originalPrice = if (tour.isOffer) tour.originalPrice else 0,
                count = adultCount
            ) { adultCount = it }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CounterItem(
                title = "Trẻ em",
                subtitle = "Giá: ${formatter.format(priceTreEm)}",
                originalPrice = if (tour.isOffer) origPriceTreEm else 0,
                count = childCount
            ) { childCount = it }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CounterItem(
                title = "Trẻ nhỏ",
                subtitle = "Giá: ${formatter.format(priceTreSoSinh)}",
                originalPrice = if (tour.isOffer) origPriceTreSoSinh else 0,
                count = infantCount
            ) { infantCount = it }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tổng tạm tính",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    formatter.format(totalPrice),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB)
                )
            }

            Button(
                onClick = { onConfirm(adultCount, childCount, infantCount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text(
                    "Xác nhận đặt tour",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CounterItem(
    title: String,
    subtitle: String,
    originalPrice: Long = 0,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
                if (originalPrice > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        formatter.format(originalPrice),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                enabled = count > 0
            ) {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    null,
                    tint = if (count > 0) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                )
            }
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            IconButton(onClick = { onCountChange(count + 1) }) {
                Icon(Icons.Default.AddCircleOutline, null, tint = Color(0xFF2563EB))
            }
        }
    }
}
