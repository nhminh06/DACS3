package com.example.dacs3.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.ui.viewmodel.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTripDetailScreen(
    bookingId: String,
    staffViewModel: StaffViewModel,
    onBack: () -> Unit
) {
    val tours by staffViewModel.tours
    val tour = tours.find { it["bookingId"] == bookingId }
    val bookings by staffViewModel.selectedTourBookings
    
    var noteText by remember { mutableStateOf(tour?.get("tripNote") as? String ?: "") }
    val primaryColor = Color(0xFF2563EB)

    LaunchedEffect(bookingId) {
        tour?.let {
            val tourId = it["tourId"] as? String ?: ""
            staffViewModel.loadBookingsForTour(tourId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết chuyến đi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (tour == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC))
                    .verticalScroll(rememberScrollState())
            ) {
                // Info Header
                AsyncImage(
                    model = tour["imageUrl"] ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = tour["title"] as? String ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DetailRow(Icons.Default.Info, "Ngày khởi hành", tour["startDate"] as? String ?: "")
                    DetailRow(Icons.Default.Info, "Số ngày", tour["duration"] as? String ?: "")
                    
                    val totalPassengers = bookings.sumOf { 
                        ((it["adults"] as? Number)?.toInt() ?: 0) + 
                        ((it["children"] as? Number)?.toInt() ?: 0) + 
                        ((it["infants"] as? Number)?.toInt() ?: 0) 
                    }
                    DetailRow(Icons.Default.People, "Tổng khách", totalPassengers.toString())
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Timeline
                    Text("Trạng thái chuyến đi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    TripTimeline(currentStatus = tour["status"] as? String ?: "preparing")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Guest List
                    Text("Danh sách khách (${bookings.size} đơn)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (bookings.isEmpty()) {
                        Text("Chưa có danh sách khách cho chuyến này", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        bookings.forEach { bk ->
                            GuestItem(bk)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Notes from Admin
                    Text("Ghi chú từ quản trị", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = tour["tripNote"] as? String ?: "Không có ghi chú nào.",
                                fontSize = 14.sp,
                                color = if (tour["tripNote"] != null) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun TripTimeline(currentStatus: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        TimelineNode("Chuẩn bị", active = true, completed = currentStatus != "preparing")
        TimelineNode("Bắt đầu", active = currentStatus == "started" || currentStatus == "completed", completed = currentStatus == "completed")
        TimelineNode("Hoàn thành", active = currentStatus == "completed", completed = currentStatus == "completed")
    }
}

@Composable
fun TimelineNode(label: String, active: Boolean, completed: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    if (completed) Color(0xFF10B981) else if (active) Color(0xFF2563EB) else Color.LightGray, 
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (active) Color.Black else Color.Gray)
    }
}

@Composable
fun GuestItem(booking: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val id = booking["id"] as? String ?: ""
                val shortId = if (id.length > 8) id.substring(id.length - 8) else id
                Text(shortId.uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = booking["paymentStatus"] as? String ?: "Pending", 
                    color = if (booking["paymentStatus"] == "da_thanh_toan") Color(0xFF10B981) else Color(0xFFF59E0B), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 10.sp
                )
            }
            Text(text = booking["customerName"] as? String ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "${booking["phone"] ?: "N/A"} • ${booking["email"] ?: ""}", fontSize = 12.sp, color = Color.Gray)
            
            val adults = (booking["adults"] as? Number)?.toInt() ?: 0
            val children = (booking["children"] as? Number)?.toInt() ?: 0
            val infants = (booking["infants"] as? Number)?.toInt() ?: 0
            Text(text = "Đoàn: $adults người lớn, $children trẻ em, $infants em bé", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
