package com.example.dacs3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.model.TourType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(onBack: () -> Unit) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    var selectedTab by remember { mutableStateOf("Tất cả") }
    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã hủy")
    
    var bookings by remember { mutableStateOf(getSampleBookings()) }
    var showAlert by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // Pair(isSuccess, message)
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(showAlert) {
        if (showAlert != null) {
            delay(5000)
            showAlert = null
        }
    }

    val filteredBookings = when (selectedTab) {
        "Chờ xác nhận" -> bookings.filter { it.status == BookingStatus.PENDING }
        "Đã xác nhận" -> bookings.filter { it.status == BookingStatus.CONFIRMED }
        "Đã hủy" -> bookings.filter { it.status == BookingStatus.CANCELLED }
        else -> bookings
    }

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
            if (filteredBookings.isEmpty()) {
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(filteredBookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancelClick = { bookingToCancel = it },
                            primaryColor = primaryColor
                        )
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
                            bookings = bookings.map { 
                                if (it.id == booking.id) it.copy(status = BookingStatus.CANCELLED) else it 
                            }
                            showAlert = Pair(true, "Hủy đặt chỗ thành công!")
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
}

@Composable
fun BookingCard(booking: Booking, onCancelClick: (Booking) -> Unit, primaryColor: Color) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Tour Image and Status Badge
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                Image(
                    painter = painterResource(id = booking.tour.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay for better text visibility
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
                val (statusColor, statusText) = when (booking.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF10B981) to "Đã xác nhận"
                    BookingStatus.PENDING -> Color(0xFFF59E0B) to "Chờ xác nhận"
                    BookingStatus.CANCELLED -> Color(0xFFEF4444) to "Đã hủy"
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
                BookingDetailRow(Icons.Default.Groups, "Hành khách", "${booking.totalPeople} người (NL: ${booking.adults}, TE: ${booking.children}, TN: ${booking.infants})")
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
                        Text(booking.note, fontSize = 13.sp, color = Color(0xFF64748B), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { /* View Detail */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, primaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                    ) {
                        Text("Chi tiết", fontWeight = FontWeight.Bold)
                    }
                    
                    if (booking.status != BookingStatus.CANCELLED) {
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

@Composable
fun BookingDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
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

fun getSampleBookings(): List<Booking> {
    val tours = listOf(
        Tour(id = "1", title = "Đà Nẵng - Hội An - Bà Nà Hills", imageRes = R.drawable.a5, startDate = "20/12/2024", rating = 4.8, reviewCount = 120, price = 4500000L, duration = "3 ngày 2 đêm", location = "Đà Nẵng", type = TourType.MULTI_DAY),
        Tour(id = "2", title = "Khám phá Vịnh Hạ Long", imageRes = R.drawable.a7, startDate = "25/12/2024", rating = 4.9, reviewCount = 85, price = 3200000L, duration = "2 ngày 1 đêm", location = "Quảng Ninh", type = TourType.MULTI_DAY),
        Tour(id = "3", title = "Tour ẩm thực Sài Gòn đêm", imageRes = R.drawable.a6, startDate = "15/12/2024", rating = 4.7, reviewCount = 50, price = 800000L, duration = "1 ngày", location = "TP. Hồ Chí Minh", type = TourType.DAY_TOUR)
    )

    return listOf(
        Booking("BK001", tours[0], BookingStatus.CONFIRMED, LocalDate.of(2024, 12, 20), 2, 1, 0, 10000000, "Phòng view biển, yêu cầu thêm giường phụ"),
        Booking("BK002", tours[1], BookingStatus.PENDING, LocalDate.of(2024, 12, 25), 4, 0, 0, 12800000),
        Booking("BK003", tours[2], BookingStatus.CANCELLED, LocalDate.of(2024, 12, 15), 1, 0, 0, 800000)
    )
}
