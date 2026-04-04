package com.example.dacs3.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
fun StaffScheduleScreen(
    staffViewModel: StaffViewModel,
    onBack: () -> Unit,
    onTourClick: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tours by staffViewModel.tours
    val tabs = listOf("Tour sắp tới", "Lịch sử")

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch trình Tour", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tìm tên tour...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            if (tours.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Hiện chưa có lịch trình nào được gán cho bạn", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val filteredTours = if (selectedTab == 0) {
                        tours.filter { 
                            val status = it["status"] as? String ?: "preparing"
                            status == "preparing" || status == "started"
                        }
                    } else {
                        tours.filter { 
                            val status = it["status"] as? String ?: ""
                            (status == "completed" || status == "cancelled") &&
                            (it["title"] as? String ?: "").contains(searchQuery, ignoreCase = true)
                        }
                    }

                    items(filteredTours) { tour ->
                        ScheduleTourItem(tour = tour, onClick = { onTourClick(tour["bookingId"] as String) })
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleTourItem(tour: Map<String, Any>, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = tour["imageUrl"] ?: "",
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tour["startDate"] as? String ?: "No Date",
                    fontSize = 12.sp,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tour["title"] as? String ?: "Tour Title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val status = tour["status"] as? String ?: "preparing"
                    val (label, statusColor) = when(status) {
                        "preparing" -> "Sắp đi" to Color(0xFF3B82F6)
                        "started" -> "Đang đi" to Color(0xFFF59E0B)
                        "completed" -> "Xong" to Color(0xFF10B981)
                        "cancelled" -> "Đã hủy" to Color.Red
                        else -> status.uppercase() to Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(label, fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text("Chi tiết >", fontSize = 12.sp, color = Color(0xFF2563EB))
                }
            }
        }
    }
}
