package com.example.dacs3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.viewmodel.BookingViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
    bookingId: String,
    onNavigateBack: () -> Unit,
    bookingViewModel: BookingViewModel
) {
    val booking by bookingViewModel.currentBooking.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    
    var showCancelDialog by remember { mutableStateOf(false) }

    // Tải dữ liệu từ Firestore khi màn hình được mở
    LaunchedEffect(bookingId) {
        bookingViewModel.loadBookingById(bookingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đặt tour", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { /* More actions */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Thêm")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            booking?.let { 
                if (it.status != BookingStatus.CANCELLED) {
                    ActionButtons(it, onCancelClick = { showCancelDialog = true }) 
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading && booking == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2563EB)
                )
            } else if (booking != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { TourHeader(booking!!) }
                    item { TourQuickInfo(booking!!) }
                    item { BookingTimeline(booking!!) }
                    item { BookingCustomerInfo(booking!!) }
                    item { PaymentSummary(booking!!) }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (!isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Không tìm thấy thông tin đặt chỗ", color = Color.Gray)
                    Button(onClick = onNavigateBack, modifier = Modifier.padding(16.dp)) {
                        Text("Quay lại")
                    }
                }
            }
        }
    }

    // Dialog xác nhận hủy tour
    if (showCancelDialog && booking != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Hủy đặt tour", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn hủy tour '${booking?.tour?.title}' không? Hành động này không thể hoàn tác.") },
            confirmButton = {
                Button(
                    onClick = {
                        bookingViewModel.cancelBooking(booking!!.id, booking!!.email)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Xác nhận hủy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Quay lại")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun TourHeader(booking: Booking) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = if (booking.tour.imageUrl.isNotEmpty()) booking.tour.imageUrl else booking.tour.imageRes,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
                
                val statusColor = when(booking.status) {
                    BookingStatus.CONFIRMED -> Color(0xFF22C55E)
                    BookingStatus.PENDING -> Color(0xFFF59E0B)
                    BookingStatus.CANCELLED -> Color(0xFFEF4444)
                }
                
                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when(booking.status) {
                            BookingStatus.CONFIRMED -> "Đã xác nhận"
                            BookingStatus.PENDING -> "Chờ xác nhận"
                            BookingStatus.CANCELLED -> "Đã hủy"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = booking.tour.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(booking.tour.location, color = Color(0xFF64748B), fontSize = 15.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Surface(
                        color = Color(0xFF2563EB).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "#${if(booking.id.length > 6) booking.id.takeLast(6).uppercase() else booking.id.uppercase()}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TourQuickInfo(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickInfoItem(Icons.Default.CalendarMonth, booking.tour.duration, "Thời gian")
            QuickInfoItem(Icons.Default.DirectionsBus, "Xe du lịch", "Phương tiện")
            QuickInfoItem(Icons.Default.Tag, booking.tour.maTour.ifEmpty { "TOUR001" }, "Mã tour")
        }
    }
}

@Composable
fun QuickInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun BookingTimeline(booking: Booking) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Tiến trình chuyến đi", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(20.dp))
        
        TimelineItem(
            title = "Đặt tour thành công",
            date = "Hoàn tất",
            isCompleted = true,
            isLast = false
        )
        TimelineItem(
            title = if (booking.status == BookingStatus.CONFIRMED) "Đã xác nhận" else if (booking.status == BookingStatus.CANCELLED) "Đã hủy" else "Đang xử lý",
            date = if (booking.status == BookingStatus.CONFIRMED) "Admin đã phê duyệt" else if (booking.status == BookingStatus.CANCELLED) "Yêu cầu đã bị hủy" else "Chờ Admin xác nhận",
            isCompleted = booking.status == BookingStatus.CONFIRMED,
            isLast = false,
            color = if (booking.status == BookingStatus.CANCELLED) Color.Red else Color(0xFF2563EB)
        )
        TimelineItem(
            title = "Ngày khởi hành",
            date = booking.startDate.format(dateFormatter),
            isCompleted = false,
            isLast = true
        )
    }
}

@Composable
fun TimelineItem(title: String, date: String, isCompleted: Boolean, isLast: Boolean, color: Color = Color(0xFF22C55E)) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(if (isCompleted) color else Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(if (isCompleted) color.copy(alpha = 0.3f) else Color(0xFFE2E8F0))
                )
            }
        }
        
        Column(modifier = Modifier.padding(start = 12.dp, bottom = 24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isCompleted) Color(0xFF1E293B) else Color.Gray)
            Text(date, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BookingCustomerInfo(booking: Booking) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Chi tiết khách hàng", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            DetailInfoRow(Icons.Default.FlightTakeoff, "Khởi hành", booking.startDate.format(dateFormatter))
            DetailInfoRow(Icons.Default.FlightLand, "Ngày về", booking.endDate.format(dateFormatter))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            
            DetailInfoRow(Icons.Default.Person, "Hành khách", booking.customerName)
            DetailInfoRow(Icons.Default.Email, "Email", booking.email)
            DetailInfoRow(Icons.Default.Phone, "Số điện thoại", booking.phone)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Tổng khách:", color = Color(0xFF64748B), fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("${booking.totalPeople} người", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF1E293B))
            }
            
            val details = mutableListOf<String>()
            if (booking.adults > 0) details.add("${booking.adults} người lớn")
            if (booking.children > 0) details.add("${booking.children} trẻ em")
            if (booking.infants > 0) details.add("${booking.infants} trẻ sơ sinh")
            
            Text(
                "(${details.joinToString(", ")})", 
                fontSize = 12.sp, 
                color = Color.Gray,
                modifier = Modifier.padding(start = 32.dp)
            )
        }
    }
}

@Composable
fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color(0xFF64748B), fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun PaymentSummary(booking: Booking) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Thanh toán", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            PaymentRow("Người lớn x${booking.adults}", currencyFormatter.format(booking.tour.price * booking.adults))
            if (booking.children > 0) {
                PaymentRow("Trẻ em x${booking.children}", currencyFormatter.format(booking.tour.giaTreEm * booking.children))
            }
            if (booking.infants > 0) {
                PaymentRow("Trẻ sơ sinh x${booking.infants}", currencyFormatter.format(booking.tour.giaTreNho * booking.infants))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 1.dp, color = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng thanh toán", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text(
                    currencyFormatter.format(booking.totalPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
    }
}

@Composable
fun ActionButtons(booking: Booking, onCancelClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (booking.canCancel) {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFEF4444))
                ) {
                    Text("HỦY ĐẶT TOUR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            } else if (booking.status == BookingStatus.CONFIRMED) {
                Button(
                    onClick = { /* Navigate to Review Screen */ },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    val brush = Brush.horizontalGradient(listOf(Color(0xFFf093fb), Color(0xFFf5576c)))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "ĐÁNH GIÁ TOUR",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

fun getSampleBookings(): List<Booking> {
    return listOf(
        Booking(
            id = "BK001",
            tour = Tour(
                id = "1",
                title = "Tour Đà Nẵng - Hội An - Bà Nà Hills",
                location = "Đà Nẵng",
                duration = "3 ngày 2 đêm",
                price = 3500000,
                maTour = "DAN001"
            ),
            status = BookingStatus.CONFIRMED,
            startDate = LocalDate.of(2026, 1, 10),
            adults = 2,
            children = 1,
            totalPrice = 8750000,
            customerName = "Nguyễn Hoàng Nhật Minh",
            email = "mnhat0034@gmail.com",
            phone = "0899883653"
        )
    )
}
