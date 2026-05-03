package com.example.dacs3.ui.screens.staff

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.R
import com.example.dacs3.data.model.Experience
import com.example.dacs3.ui.viewmodel.StaffViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StaffProfileScreen(
    userViewModel: UserViewModel,
    staffViewModel: StaffViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val user by userViewModel.currentUser
    val guideProfile by staffViewModel.guideProfile
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFF2563EB)

    LaunchedEffect(user?.id) {
        user?.id?.let { staffViewModel.loadGuideProfile(it) }
    }

    // --- States cho Chỉnh sửa thông tin cơ bản ---
    var isEditingInfo by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            name = it.name
            phone = it.sdt
            email = it.email
            address = it.dia_chi
            gender = it.gioi_tinh
            dob = it.ngay_sinh
        }
    }

    var showBioDialog by remember { mutableStateOf(false) }
    var tempBio by remember { mutableStateOf("") }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var tempSkills by remember { mutableStateOf("") }
    var showExpDialog by remember { mutableStateOf(false) }
    var expTitle by remember { mutableStateOf("") }
    var expStart by remember { mutableStateOf("") }
    var expEnd by remember { mutableStateOf("") }
    var expDesc by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            userViewModel.updateAvatar(it) { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // --- PHẦN 1: CÔNG CỤ TÁC VỤ (ĐƯA LÊN ĐẦU TIÊN) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryColor)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text("CÔNG CỤ TÁC VỤ", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 4 DÒNG CÔNG CỤ
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Dòng 1
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigActionButton("Lịch Tour", Icons.Default.CalendarMonth, Modifier.weight(1f)) { onNavigate("staff_tours") }
                        BigActionButton("Chi tiết chuyến", Icons.AutoMirrored.Filled.Assignment, Modifier.weight(1f)) { onNavigate("staff_trip_detail") }
                    }
                    // Dòng 2
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigActionButton("Viết bài", Icons.Default.EditNote, Modifier.weight(1f)) { onNavigate("create_article") }
                        BigActionButton("Bài viết", Icons.AutoMirrored.Filled.Article, Modifier.weight(1f)) { onNavigate("my_articles") }
                    }
                    // Dòng 3
                    BigActionButton("Ghi chú chuyến đi", Icons.AutoMirrored.Filled.NoteAdd, Modifier.fillMaxWidth()) { onNavigate("staff_notes") }
                    // Dòng 4
                    BigActionButton("Đổi mật khẩu tài khoản", Icons.Default.LockPerson, Modifier.fillMaxWidth()) { onNavigate("change_password") }
                }
            }

            // --- PHẦN 2: THÔNG TIN CÁ NHÂN (HEADER) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, Color(0xFF3B82F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = user?.avatar.takeIf { !it.isNullOrEmpty() } ?: R.drawable.a8,
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Color.White) {
                            Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(6.dp), tint = primaryColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(user?.name ?: "Nhân viên", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Nhân viên • ⭐ Rank ${user?.rank ?: "3"}", color = Color.White.copy(0.8f), fontSize = 13.sp)
                }
            }

            // --- PHẦN 3: CHI TIẾT HỒ SƠ ---
            Column(
                modifier = Modifier
                    .offset(y = (-20).dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Thông tin cơ bản
                SectionCard(
                    title = "THÔNG TIN CƠ BẢN",
                    action = {
                        TextButton(onClick = {
                            if (isEditingInfo) {
                                val updated = user?.copy(name = name, sdt = phone, email = email, dia_chi = address, gioi_tinh = gender, ngay_sinh = dob)
                                updated?.let { userViewModel.updateUserInfo(it, onSuccess = { isEditingInfo = false }, onError = { e -> Toast.makeText(context, e, Toast.LENGTH_SHORT).show() }) }
                            } else { isEditingInfo = true }
                        }) { Text(if (isEditingInfo) "Lưu" else "Chỉnh sửa", fontWeight = FontWeight.Bold) }
                    }
                ) {
                    ProfileEditField("Họ tên", name, { name = it }, isEditingInfo)
                    ProfileEditField("Email", email, { email = it }, isEditingInfo)
                    ProfileEditField("SĐT", phone, { phone = it }, isEditingInfo)
                    ProfileEditField("Địa chỉ", address, { address = it }, isEditingInfo)
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionCard("GIỚI THIỆU BẢN THÂN") {
                    if (guideProfile?.bio.isNullOrEmpty()) {
                        Button(onClick = { tempBio = ""; showBioDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(0.1f))) { Text("+ Thêm giới thiệu", color = primaryColor) }
                    } else {
                        Text(guideProfile?.bio ?: "", fontSize = 14.sp, color = Color(0xFF334155))
                        TextButton(onClick = { tempBio = guideProfile?.bio ?: ""; showBioDialog = true }, modifier = Modifier.align(Alignment.End)) { Text("Chỉnh sửa", color = primaryColor) }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionCard("KỸ NĂNG") {
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        guideProfile?.skills?.forEach { skill ->
                            Surface(color = primaryColor.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
                                Text(skill, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 13.sp, color = primaryColor)
                            }
                        }
                    }
                    TextButton(onClick = { tempSkills = guideProfile?.skills?.joinToString(", ") ?: ""; showSkillsDialog = true }) { Text("Quản lý kỹ năng", color = primaryColor, fontWeight = FontWeight.Bold) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SectionCard("KINH NGHIỆM") {
                    guideProfile?.experiences?.forEach { exp ->
                        ProfileExperienceItem(exp) { staffViewModel.deleteExperience(exp.id, onSuccess = {}, onError = {}) }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                    }
                    Button(onClick = { expTitle = ""; expStart = ""; expEnd = ""; expDesc = ""; showExpDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Thêm kinh nghiệm") }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Dialogs... (Giữ nguyên logic xử lý Dialogs)
    if (showBioDialog) {
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Giới thiệu", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = tempBio, onValueChange = { tempBio = it }, modifier = Modifier.fillMaxWidth().height(150.dp)) },
            confirmButton = { TextButton(onClick = { staffViewModel.updateBio(tempBio, onSuccess = { showBioDialog = false }, onError = {}) }) { Text("Lưu") } }
        )
    }
    // ... Các dialog khác tương tự
}

@Composable
fun BigActionButton(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(60.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.5f), modifier = Modifier.size(16.dp))
        }
    }
}

// Các Composable phụ khác (SectionCard, ProfileEditField, v.v. giữ nguyên logic)
@Composable
fun SectionCard(title: String, action: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileEditField(label: String, value: String, onValueChange: (String) -> Unit, isEditing: Boolean) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        if (isEditing) {
            TextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), singleLine = true)
        } else {
            Text(value.ifEmpty { "N/A" }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
fun ProfileExperienceItem(exp: Experience, onDelete: () -> Unit) {
    Row {
        Column(modifier = Modifier.weight(1f)) {
            Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("${exp.startTime} - ${exp.endTime}", color = Color(0xFF2563EB), fontSize = 12.sp)
            Text(exp.description, fontSize = 13.sp, color = Color.Gray)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.5f)) }
    }
}
