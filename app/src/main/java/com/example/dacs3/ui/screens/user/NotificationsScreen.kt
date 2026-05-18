package com.example.dacs3.ui.screens.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
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
import com.example.dacs3.data.repository.ReportRepository
import com.example.dacs3.ui.viewmodel.ContactViewModel
import com.example.dacs3.ui.viewmodel.NotificationViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class NotificationItem {
    abstract val timestamp: Timestamp
    abstract val isUnread: Boolean

    data class System(val data: Map<String, Any>, override val timestamp: Timestamp, override val isUnread: Boolean) : NotificationItem()
    data class Report(val data: Map<String, Any>, override val timestamp: Timestamp, override val isUnread: Boolean) : NotificationItem()
    data class ContactReply(val data: Map<String, Any>, override val timestamp: Timestamp, override val isUnread: Boolean) : NotificationItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    userViewModel: UserViewModel,
    contactViewModel: ContactViewModel,
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    
    val user by userViewModel.currentUser
    var allNotifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?.id) {
        if (user?.id != null) {
            val currentUserId = user!!.id
            try {
                // Fetch System Notifs
                val sysDocs = FirebaseFirestore.getInstance().collection("notifications")
                    .whereEqualTo("userId", currentUserId).get().await()
                val systems = sysDocs.documents.map { doc ->
                    NotificationItem.System(doc.data ?: emptyMap(), doc.getTimestamp("timestamp") ?: Timestamp.now(), doc.getBoolean("isRead") != true)
                }

                // Fetch Reports
                val reportDocs = FirebaseFirestore.getInstance().collection("reports")
                    .whereEqualTo("reporterId", currentUserId).get().await()
                val reports = reportDocs.documents.filter { it.get("reply") != null }.map { doc ->
                    NotificationItem.Report(doc.data ?: emptyMap(), doc.getTimestamp("createdAt") ?: Timestamp.now(), doc.getBoolean("isSeen") != true)
                }

                // Fetch Contacts
                val contactDocs = FirebaseFirestore.getInstance().collection("contacts")
                    .whereEqualTo("userId", currentUserId).get().await()
                val contacts = contactDocs.documents.filter { it.get("reply") != null }.map { doc ->
                    NotificationItem.ContactReply(doc.data ?: emptyMap(), doc.getTimestamp("replyAt") ?: doc.getTimestamp("timestamp") ?: Timestamp.now(), doc.getBoolean("isSeen") != true)
                }

                // SORTING: Unread at TOP, then Newest at TOP
                allNotifications = (systems + reports + contacts).sortedWith(
                    compareByDescending<NotificationItem> { it.isUnread }
                        .thenByDescending { it.timestamp.seconds }
                )
                
                // Đợi một lát rồi mới đánh dấu đã đọc để người dùng kịp thấy chữ MỚI
                delay(3000)
                notificationViewModel.markAllAsRead(currentUserId)
            } catch (e: Exception) {
                Log.e("NOTIF_DEBUG", "Error: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông báo của tôi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) { 
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        ) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(backgroundColor)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = primaryColor) }
            } else if (allNotifications.isEmpty()) {
                EmptyNotifications()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allNotifications) { item ->
                        when (item) {
                            is NotificationItem.System -> SystemNotificationItem(item.data, item.isUnread)
                            is NotificationItem.Report -> ReportReplyItem(item.data, item.isUnread, primaryColor)
                            is NotificationItem.ContactReply -> AdminReplyItem(item.data, item.isUnread, primaryColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemNotificationItem(data: Map<String, Any>, isUnread: Boolean) {
    val sdf = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    val timestamp = data["timestamp"] as? Timestamp
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp), 
                shape = CircleShape, 
                color = Color(0xFFFACC15).copy(alpha = if (isSystemInDarkTheme()) 0.25f else 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CardTravel, null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = data["title"]?.toString() ?: "Thông báo", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isUnread) { Spacer(modifier = Modifier.width(8.dp)); NewTag() }
                }
                Text(
                    text = data["message"]?.toString() ?: "", 
                    fontSize = 13.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = timestamp?.let { sdf.format(it.toDate()) } ?: "", 
                    fontSize = 10.sp, 
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun AdminReplyItem(data: Map<String, Any>, isUnread: Boolean, primaryColor: Color) {
    val sdf = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    val timestamp = (data["replyAt"] ?: data["timestamp"]) as? Timestamp
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Reply, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Phản hồi hỗ trợ", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isUnread) { Spacer(modifier = Modifier.weight(1f)); NewTag() }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "Câu hỏi của bạn:", 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        data["content"]?.toString() ?: "", 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Admin phản hồi:", 
                fontSize = 11.sp, 
                color = primaryColor, 
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                data["reply"]?.toString() ?: "", 
                fontSize = 14.sp, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                timestamp?.let { sdf.format(it.toDate()) } ?: "", 
                fontSize = 10.sp, 
                color = MaterialTheme.colorScheme.outline, 
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun ReportReplyItem(data: Map<String, Any>, isUnread: Boolean, primaryColor: Color) {
    val sdf = remember { SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()) }
    val timestamp = data["createdAt"] as? Timestamp
    val type = data["type"]?.toString() ?: ""
    val targetName = if (type == "COMMENT") data["reportedUserName"] else data["articleTitle"]
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Report, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Phản hồi báo cáo", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 15.sp, 
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isUnread) { Spacer(modifier = Modifier.weight(1f)); NewTag() }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), 
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "Đối tượng:", 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.error, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        targetName?.toString() ?: "", 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Lý do: ${data["reason"]}", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Admin phản hồi:", 
                fontSize = 11.sp, 
                color = primaryColor, 
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                data["reply"]?.toString() ?: "", 
                fontSize = 14.sp, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                timestamp?.let { sdf.format(it.toDate()) } ?: "", 
                fontSize = 10.sp, 
                color = MaterialTheme.colorScheme.outline, 
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun NewTag() {
    Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(4.dp)) {
        Text(
            "MỚI", 
            color = MaterialTheme.colorScheme.onError, 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EmptyNotifications() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications, 
                null, 
                modifier = Modifier.size(64.dp), 
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chưa có thông báo nào", 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Medium
            )
        }
    }
}
