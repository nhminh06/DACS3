package com.example.dacs3.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.ui.viewmodel.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffNotesScreen(
    staffViewModel: StaffViewModel,
    onBack: () -> Unit
) {
    val tours by staffViewModel.tours
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ghi chú từ khách hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        if (tours.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không có dữ liệu chuyến đi", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tours) { trip ->
                    TripNotesCard(trip, staffViewModel)
                }
            }
        }
    }
}

@Composable
fun TripNotesCard(trip: Map<String, Any>, staffViewModel: StaffViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val tourId = trip["tourId"] as? String ?: ""
    val startDate = trip["startDate"] as? String ?: ""
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip["title"] as? String ?: "Chuyến đi không tên",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Khởi hành: $startDate", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Ghi chú điều hành: ${trip["tripNote"]?.toString()?.ifBlank { "Không có" } ?: "Không có"}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    if (expanded) "Ẩn ghi chú khách" else "Xem ghi chú khách hàng", 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (expanded) {
                TripCustomerNotesList(tourId, startDate, staffViewModel)
            }
        }
    }
}

@Composable
fun TripCustomerNotesList(tourId: String, startDate: String, staffViewModel: StaffViewModel) {
    val allBookings by staffViewModel.selectedTourBookings
    
    val tripBookings = allBookings.filter { 
        it["tourId"] == tourId && it["startDate"] == startDate && it["note"]?.toString()?.isNotBlank() == true
    }

    LaunchedEffect(tourId, startDate) {
        staffViewModel.loadBookingsForTour(tourId, startDate)
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        if (tripBookings.isEmpty()) {
            Text(
                "Không có ghi chú nào từ khách hàng cho chuyến này.", 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            tripBookings.forEach { booking ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.StickyNote2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = booking["customerName"] as? String ?: "Khách hàng", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = booking["note"] as? String ?: "", 
                            fontSize = 13.sp, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
