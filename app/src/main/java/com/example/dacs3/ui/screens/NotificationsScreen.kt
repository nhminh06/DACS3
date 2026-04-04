package com.example.dacs3.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Contact
import com.example.dacs3.ui.viewmodel.ContactViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    userViewModel: UserViewModel,
    contactViewModel: ContactViewModel,
    onBack: () -> Unit
) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    val user by userViewModel.currentUser
    val userContacts by contactViewModel.userContacts.collectAsState()
    
    var systemNotifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.id) {
        if (user?.id != null) {
            val currentUserId = user!!.id
            Log.d("NOTIF_DEBUG", "Fetching notifications for UserID: $currentUserId")
            contactViewModel.fetchUserContacts(currentUserId)
            
            try {
                // Thử lấy tất cả thông báo của User này
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .await()
                
                Log.d("NOTIF_DEBUG", "Found ${snapshot.size()} notifications")
                
                systemNotifications = snapshot.documents.map { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                }.sortedByDescending { (it["timestamp"] as? com.google.firebase.Timestamp)?.seconds ?: 0L }
            } catch (e: Exception) {
                Log.e("NOTIF_DEBUG", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Thông báo của tôi", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        if (user != null) {
                            Text("ID: ${user?.id?.take(8)}...", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF1E293B))
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
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                val repliedContacts = userContacts.filter { it.reply != null }

                if (repliedContacts.isEmpty() && systemNotifications.isEmpty()) {
                    EmptyNotifications()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Thông báo Tour
                        items(systemNotifications) { notification ->
                            SystemNotificationItem(notification)
                        }
                        
                        // 2. Phản hồi liên hệ
                        items(repliedContacts) { contact ->
                            AdminReplyItem(contact, primaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemNotificationItem(data: Map<String, Any>) {
    val sdf = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    val timestamp = data["timestamp"] as? com.google.firebase.Timestamp
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFFACC15).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CardTravel, null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data["title"]?.toString() ?: "Thông báo đặt tour",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = data["message"]?.toString() ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timestamp?.let { sdf.format(it.toDate()) } ?: "",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AdminReplyItem(contact: Contact, primaryColor: Color) {
    val sdf = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Reply, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phản hồi: ${contact.type}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(8.dp)) {
                Column {
                    Text("Nội dung của bạn:", fontSize = 10.sp, color = Color.Gray)
                    Text(contact.content, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("Admin phản hồi:", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
            Text(contact.reply ?: "", fontSize = 13.sp)
            
            Text(
                contact.replyAt?.let { sdf.format(it.toDate()) } ?: sdf.format(contact.timestamp.toDate()),
                fontSize = 9.sp, color = Color.Gray, modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun EmptyNotifications() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chưa có thông báo nào", color = Color.Gray)
        }
    }
}
