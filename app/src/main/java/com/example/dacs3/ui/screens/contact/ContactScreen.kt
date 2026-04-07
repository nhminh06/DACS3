package com.example.dacs3.ui.screens.contact

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Contact
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.example.dacs3.ui.viewmodel.ContactViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    userViewModel: UserViewModel,
    contactViewModel: ContactViewModel,
    onNavigate: (String) -> Unit
) {
    val backgroundColor = Color(0xFFF8FAFC)
    
    val currentUser by userViewModel.currentUser
    val isLoading by contactViewModel.isLoading
    val userContacts by contactViewModel.userContacts.collectAsState()
    
    var showForm by remember { mutableStateOf(true) }
    var contactType by remember { mutableStateOf("Góp ý") }
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var content by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tự động tải dữ liệu khi vào màn hình hoặc khi user thay đổi
    LaunchedEffect(currentUser) {
        currentUser?.id?.let { contactViewModel.fetchUserContacts(it) }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("Liên Hệ & Hỗ Trợ", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A)) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )
        },
        bottomBar = { 
            AppBottomBar(
                currentScreen = "contact",
                onNavigate = onNavigate
            ) 
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "Gửi yêu cầu",
                    isSelected = showForm,
                    onClick = { showForm = true },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Lịch sử",
                    isSelected = !showForm,
                    onClick = { showForm = false },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (showForm) {
                    ContactForm(
                        name = name,
                        onNameChange = { name = it },
                        email = email,
                        onEmailChange = { email = it },
                        content = content,
                        onContentChange = { content = it },
                        contactType = contactType,
                        onTypeChange = { contactType = it },
                        isLoading = isLoading,
                        onSubmit = {
                            if (name.isNotBlank() && email.isNotBlank() && content.isNotBlank()) {
                                val contact = Contact(
                                    userId = currentUser?.id,
                                    name = name,
                                    email = email,
                                    type = contactType,
                                    content = content
                                )
                                contactViewModel.submitContact(contact) { success, error ->
                                    if (success) {
                                        isSubmitted = true
                                        content = ""
                                    } else {
                                        errorMessage = error
                                    }
                                }
                            }
                        }
                    )
                } else {
                    ContactHistory(userContacts)
                }
            }
        }
    }

    if (isSubmitted) {
        AlertDialog(
            onDismissRequest = { 
                isSubmitted = false
                contactViewModel.resetSuccess()
            },
            confirmButton = {
                TextButton(onClick = { 
                    isSubmitted = false
                    contactViewModel.resetSuccess()
                }) { Text("Đóng", fontWeight = FontWeight.Bold) }
            },
            title = { Text("Gửi thành công!", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = { Text("Cảm ơn bạn đã gửi $contactType. Chúng tôi sẽ phản hồi sớm nhất.", color = Color(0xFF334155)) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("Đóng", fontWeight = FontWeight.Bold) }
            },
            title = { Text("Lỗi", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text(errorMessage!!, color = Color(0xFF334155)) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ContactHistory(contacts: List<Contact>) {
    if (contacts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chưa có lịch sử gửi yêu cầu", color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contacts) { contact ->
                ContactHistoryItem(contact)
            }
        }
    }
}

@Composable
fun ContactHistoryItem(contact: Contact) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (contact.type == "Góp ý") Color(0xFF3B82F6) else Color(0xFFF43F5E),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        contact.type, color = Color.White, fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(sdf.format(contact.timestamp.toDate()), fontSize = 10.sp, color = Color(0xFF64748B))
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                contact.content, 
                fontSize = 14.sp, 
                color = Color(0xFF0F172A), 
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (contact.reply != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Reply, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Phản hồi từ hệ thống:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(contact.reply!!, fontSize = 13.sp, color = Color(0xFF1E293B), lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                        
                        if (contact.replyAt != null) {
                            Text(
                                sdf.format(contact.replyAt.toDate()), 
                                fontSize = 9.sp, 
                                color = Color(0xFF64748B), 
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (contact.status == "processed") Color(0xFF10B981) else Color(0xFFF59E0B), 
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (contact.status == "processed") "Đã xử lý" else "Đang chờ phản hồi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (contact.status == "processed") Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF2563EB) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF475569)
        ),
        elevation = null,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun ContactForm(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    content: String, onContentChange: (String) -> Unit,
    contactType: String, onTypeChange: (String) -> Unit,
    isLoading: Boolean,
    onSubmit: () -> Unit
) {
    val primaryColor = Color(0xFF2563EB)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(primaryColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SupportAgent, null, modifier = Modifier.size(32.dp), tint = primaryColor)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Text("Chúng tôi luôn lắng nghe bạn", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
        Text(
            "Mọi ý kiến đóng góp giúp chúng tôi hoàn thiện hơn.",
            textAlign = TextAlign.Center,
            fontSize = 13.sp, color = Color(0xFF475569),
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ContactTypeChip("Góp ý", contactType == "Góp ý", { onTypeChange("Góp ý") }, Modifier.weight(1f))
                    ContactTypeChip("Khiếu nại", contactType == "Khiếu nại", { onTypeChange("Khiếu nại") }, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                ContactTextField(value = name, onValueChange = onNameChange, label = "Họ và tên", icon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(10.dp))
                ContactTextField(value = email, onValueChange = onEmailChange, label = "Email liên hệ", icon = Icons.Default.Email)
                Spacer(modifier = Modifier.height(10.dp))
                ContactTextField(
                    value = content, 
                    onValueChange = onContentChange, 
                    label = "Nội dung tin nhắn", 
                    icon = Icons.AutoMirrored.Filled.Message, 
                    isMultiline = true
                )

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onSubmit, enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Gửi thông tin", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ContactTypeChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.height(38.dp).clickable { onClick() },
        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSelected) Color.White else Color(0xFF475569))
        }
    }
}

@Composable
fun ContactTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isMultiline: Boolean = false
) {
    val primaryColor = Color(0xFF2563EB)
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = { Icon(icon, null, tint = primaryColor, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.fillMaxWidth().then(if (isMultiline) Modifier.height(130.dp) else Modifier),
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 15.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color(0xFFCBD5E1),
            focusedLabelColor = primaryColor,
            unfocusedLabelColor = Color(0xFF475569),
            cursorColor = primaryColor
        )
    )
}
