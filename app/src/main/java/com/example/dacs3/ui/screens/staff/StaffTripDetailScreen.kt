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
    tourId: String,
    staffViewModel: StaffViewModel,
    onBack: () -> Unit
) {
    val tours by staffViewModel.tours
    val tour = tours.find { it["id"] == tourId }
    
    var noteText by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF2563EB)

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
                    DetailRow(Icons.Default.People, "Tổng khách", "25") // Mock
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Timeline
                    Text("Trạng thái chuyến đi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    TripTimeline(currentStatus = tour["status"] as? String ?: "upcoming")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Guest List
                    Text("Danh sách khách", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    repeat(3) { // Mock guest list
                        GuestItem()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Notes
                    Text("Ghi chú", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                placeholder = { Text("Thêm ghi chú mới...") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { /* Add Note */ },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm ghi chú")
                            }
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
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TimelineNode("Chuẩn bị", active = true)
        TimelineNode("Bắt đầu", active = currentStatus == "ongoing" || currentStatus == "completed")
        TimelineNode("Hoàn thành", active = currentStatus == "completed")
    }
}

@Composable
fun TimelineNode(label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(if (active) Color(0xFF2563EB) else Color.LightGray, CircleShape)
        )
        Text(label, fontSize = 12.sp, color = if (active) Color.Black else Color.Gray)
    }
}

@Composable
fun GuestItem() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("BK-2024001", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text("Confirmed", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Text("Nguyễn Thị B", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("0987654321 • ntb@email.com", fontSize = 12.sp, color = Color.Gray)
            Text("4 người", fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
