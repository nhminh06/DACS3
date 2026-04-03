package com.example.dacs3.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.ui.components.AppBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onNavigate: (String) -> Unit
) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    var contactType by remember { mutableStateOf("Góp ý") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text("Liên Hệ & Hỗ Trợ", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) 
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SupportAgent, 
                    contentDescription = null, 
                    modifier = Modifier.size(60.dp),
                    tint = primaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Chúng tôi luôn lắng nghe bạn", 
                fontWeight = FontWeight.Bold, 
                fontSize = 18.sp, 
                color = Color(0xFF1E293B)
            )
            Text(
                "Mọi ý kiến đóng góp hoặc khiếu nại của bạn đều giúp chúng tôi hoàn thiện hơn.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Bạn muốn gửi gì?", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ContactTypeChip(
                            label = "Góp ý", 
                            isSelected = contactType == "Góp ý",
                            onClick = { contactType = "Góp ý" },
                            modifier = Modifier.weight(1f)
                        )
                        ContactTypeChip(
                            label = "Khiếu nại", 
                            isSelected = contactType == "Khiếu nại",
                            onClick = { contactType = "Khiếu nại" },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ContactTextField(value = name, onValueChange = { name = it }, label = "Họ và tên", icon = Icons.Default.Person)
                    Spacer(modifier = Modifier.height(16.dp))
                    ContactTextField(value = email, onValueChange = { email = it }, label = "Email liên hệ", icon = Icons.Default.Email)
                    Spacer(modifier = Modifier.height(16.dp))
                    ContactTextField(
                        value = content, 
                        onValueChange = { content = it }, 
                        label = "Nội dung tin nhắn", 
                        icon = Icons.Default.Message,
                        isMultiline = true
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { 
                            if (name.isNotBlank() && email.isNotBlank() && content.isNotBlank()) {
                                isSubmitted = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Gửi thông tin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Support Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SupportInfoItem(Icons.Default.Phone, "Hotline", "1900 1234")
                SupportInfoItem(Icons.Default.Public, "Website", "windtravel.com")
            }
        }
    }

    if (isSubmitted) {
        AlertDialog(
            onDismissRequest = { isSubmitted = false },
            confirmButton = {
                Button(onClick = { isSubmitted = false }) { Text("Đóng") }
            },
            title = { Text("Gửi thành công!") },
            text = { Text("Cảm ơn bạn đã gửi $contactType. Chúng tôi sẽ phản hồi sớm nhất qua email của bạn.") },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun ContactTypeChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() },
        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, 
                fontWeight = FontWeight.Bold, 
                color = if (isSelected) Color.White else Color(0xFF64748B)
            )
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF2563EB)) },
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isMultiline) Modifier.height(150.dp) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2563EB),
            unfocusedBorderColor = Color(0xFFE2E8F0)
        )
    )
}

@Composable
fun SupportInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}
