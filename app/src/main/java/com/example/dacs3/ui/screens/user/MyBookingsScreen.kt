package com.example.dacs3.ui.screens.user

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.model.Review
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.example.dacs3.ui.viewmodel.BookingViewModel
import com.example.dacs3.ui.viewmodel.ReviewViewModel
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.delay
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    onBookingClick: (String) -> Unit,
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel,
    reviewViewModel: ReviewViewModel = viewModel()
) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    val user = userViewModel.currentUser.value
    val bookings by bookingViewModel.bookings.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val submitSuccess by reviewViewModel.submitSuccess.collectAsState()
    
    var selectedTab by remember { mutableStateOf("Tất cả") }
    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Đã hủy")
    
    var showAlert by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // Pair(isSuccess, message)
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var bookingToReview by remember { mutableStateOf<Booking?>(null) }

    // Pagination state
    var currentPage by remember { mutableStateOf(1) }
    val pageSize = 3

    // Load real bookings when screen opens
    LaunchedEffect(user) {
        user?.email?.let { email ->
            bookingViewModel.loadUserBookings(email)
        }
    }

    LaunchedEffect(showAlert) {
        if (showAlert != null) {
            delay(5000)
            showAlert = null
        }
    }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess == true) {
            showAlert = Pair(true, "Cảm ơn bạn đã đánh giá chuyến đi!")
            reviewViewModel.resetSubmitStatus()
            bookingToReview = null
        } else if (submitSuccess == false) {
            showAlert = Pair(false, "Gửi đánh giá thất bại. Vui lòng thử lại.")
            reviewViewModel.resetSubmitStatus()
        }
    }

    val filteredBookings = when (selectedTab) {
        "Chờ xác nhận" -> bookings.filter { it.status == BookingStatus.PENDING }
        "Đã xác nhận" -> bookings.filter { it.status == BookingStatus.CONFIRMED && it.tripStatus != "completed" }
        "Hoàn thành" -> bookings.filter { it.tripStatus == "completed" }
        "Đã hủy" -> bookings.filter { it.status == BookingStatus.CANCELLED }
        else -> bookings
    }

    // Update current page when tab changes or data changes
    LaunchedEffect(selectedTab, filteredBookings.size) {
        currentPage = 1
    }

    val totalPages = if (filteredBookings.isEmpty()) 1 else ceil(filteredBookings.size.toDouble() / pageSize).toInt()
    val paginatedBookings = filteredBookings.drop((currentPage - 1) * pageSize).take(pageSize)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = primaryColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Đặt Chỗ Của Tôi",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B),
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(backgroundColor)
        ) {
            // Alert Message
            AnimatedVisibility(
                visible = showAlert != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                showAlert?.let { alert ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = if (alert.first) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (alert.first) Color(0xFFBBF7D0) else Color(0xFFFECACA))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (alert.first) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (alert.first) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                alert.second,
                                color = if (alert.first) Color(0xFF166534) else Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color.White,
                contentColor = primaryColor,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabs.indexOf(selectedTab)]),
                        color = primaryColor,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { 
                            Text(
                                tab, 
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == tab) primaryColor else Color(0xFF64748B)
                            ) 
                        }
                    )
                }
            }

            // Booking List
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Không có đặt chỗ nào", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(paginatedBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancelClick = { bookingToCancel = it },
                            onReviewClick = { bookingToReview = it },
                            onDetailClick = { onBookingClick(booking.id) },
                            primaryColor = primaryColor
                        )
                    }

                    // Pagination Controls moved inside LazyColumn to prevent covering content
                    if (totalPages > 1) {
                        item {
                            PaginationControls(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                onPageChange = { currentPage = it },
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Cancel Confirmation Dialog
    if (bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444)) },
            title = { Text("Hủy đặt chỗ", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn hủy đặt chỗ cho tour '${bookingToCancel?.tour?.title}' không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        val booking = bookingToCancel!!
                        if (booking.canCancel) {
                            user?.email?.let { email ->
                                bookingViewModel.cancelBooking(booking.id, email)
                                showAlert = Pair(true, "Yêu cầu hủy đã được gửi!")
                            }
                        } else {
                            showAlert = Pair(false, "Không đủ điều kiện hủy (phải trước 3 ngày khởi hành)")
                        }
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận hủy", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("Quay lại", color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Review Dialog
    if (bookingToReview != null) {
        ReviewDialog(
            booking = bookingToReview!!,
            onDismiss = { bookingToReview = null },
            onSubmit = { rating, comment ->
                user?.let { u ->
                    val review = Review(
                        userId = u.id,
                        userName = u.name,
                        userAvatar = u.avatar,
                        tourId = bookingToReview!!.tour.id,
                        guideId = bookingToReview!!.guideId, // Cập nhật guideId từ booking
                        bookingId = bookingToReview!!.id,
                        rating = rating,
                        comment = comment
                    )
                    reviewViewModel.submitReview(review)
                }
            },
            isSubmitting = reviewViewModel.isSubmitting.collectAsState().value
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = if (currentPage > 1) primaryColor else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Page Numbers logic
        val maxVisiblePages = 5
        val startPage = maxOf(1, currentPage - 2)
        val endPage = minOf(totalPages, startPage + maxVisiblePages - 1)
        val actualStartPage = maxOf(1, endPage - maxVisiblePages + 1)

        for (i in actualStartPage..endPage) {
            val isSelected = i == currentPage
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primaryColor else Color.Transparent)
                    .clickable { onPageChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = i.toString(),
                    color = if (isSelected) Color.White else Color(0xFF475569),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (currentPage < totalPages) primaryColor else Color.Gray
            )
        }
    }
}

@Composable
fun ReviewDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isSubmitting: Boolean
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF2563EB)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đánh giá chuyến đi", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(booking.tour.title, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Bạn đánh giá thế nào?", fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        IconButton(onClick = { rating = starIndex }) {
                            Icon(
                                if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (starIndex <= rating) Color(0xFFFACC15) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Lời bình luận của bạn", color = Color(0xFF475569)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color(0xFF475569)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = !isSubmitting && comment.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
            ) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Gửi đánh giá", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Hủy", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancelClick: (Booking) -> Unit,
    onReviewClick: (Booking) -> Unit,
    onDetailClick: () -> Unit,
    primaryColor: Color
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetailClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Tour Image and Status Badge
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                AsyncImage(
                    model = if (booking.tour.imageUrl.isNotEmpty()) booking.tour.imageUrl else booking.tour.imageRes,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Transparent)
                            )
                        )
                )

                // Status Badge
                val (statusColor, statusText) = when {
                    booking.status == BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Đã hủy"
                    booking.tripStatus == "completed" -> Color(0xFF10B981) to "Đã hoàn thành"
                    booking.status == BookingStatus.CONFIRMED -> Color(0xFF2563EB) to "Đã xác nhận"
                    else -> Color(0xFFF59E0B) to "Chờ xác nhận"
                }

                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        statusText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    booking.tour.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B),
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tag, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mã đặt chỗ: ${booking.id}", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // Details
                BookingDetailRow(Icons.Default.CalendarMonth, "Thời gian", "${booking.startDate.format(dateFormatter)} - ${booking.endDate.format(dateFormatter)}")
                BookingDetailRow(
                    Icons.Default.Groups,
                    "Hành khách",
                    "${booking.totalPeople} người (NL: ${booking.adults}, TE: ${booking.children}, SS: ${booking.infants})"
                )
                BookingDetailRow(Icons.Default.HistoryToggleOff, "Độ dài", booking.tour.duration)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F7FF), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Tổng thanh toán", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Text(
                        String.format(Locale.getDefault(), "%,d VNĐ", booking.totalPrice),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }

                if (!booking.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.AutoMirrored.Filled.Note, contentDescription = null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(booking.note, fontSize = 13.sp, color = Color(0xFF64748B), fontStyle = FontStyle.Italic)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDetailClick,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                    ) {
                        Text("Chi tiết", fontWeight = FontWeight.Bold)
                    }

                    if (booking.status != BookingStatus.CANCELLED) {
                        if (booking.tripStatus == "completed") {
                            Button(
                                onClick = { onReviewClick(booking) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Đánh giá ngay", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { onCancelClick(booking) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (booking.canCancel) Color(0xFFFEE2E2) else Color(0xFFF1F5F9),
                                    contentColor = if (booking.canCancel) Color(0xFFEF4444) else Color(0xFF94A3B8)
                                ),
                                enabled = booking.canCancel
                            ) {
                                Text("Hủy đặt chỗ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2563EB))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Text(value, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
        }
    }
}
