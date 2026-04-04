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
fun StaffPersonalScreen(
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

    // --- States cho Bio Dialog ---
    var showBioDialog by remember { mutableStateOf(false) }
    var tempBio by remember { mutableStateOf("") }

    // --- States cho Experience Dialog ---
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
            // --- PHẦN 1: THÔNG TIN CÁ NHÂN (HEADER) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(primaryColor, Color(0xFF3B82F6))
                        )
                    )
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = user?.avatar.takeIf { !it.isNullOrEmpty() } ?: R.drawable.a8,
                            contentDescription = null,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
                                .clickable { imagePicker.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                modifier = Modifier.padding(6.dp),
                                tint = primaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user?.name ?: "Nhân viên", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            "Nhân viên • ⭐ Rank ${user?.rank ?: "3"}",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // --- PHẦN 2: NỘI DUNG CHI TIẾT ---
            Column(
                modifier = Modifier
                    .offset(y = (-30).dp)
                    .padding(horizontal = 20.dp)
            ) {
                // 3.1 Thông tin cơ bản
                StaffSectionCard(
                    title = "THÔNG TIN CƠ BẢN",
                    action = {
                        Row {
                            if (isEditingInfo) {
                                TextButton(onClick = {
                                    val updated = user?.copy(
                                        name = name, sdt = phone, email = email,
                                        dia_chi = address, gioi_tinh = gender, ngay_sinh = dob
                                    )
                                    updated?.let {
                                        userViewModel.updateUserInfo(it,
                                            onSuccess = { isEditingInfo = false },
                                            onError = { e -> Toast.makeText(context, e, Toast.LENGTH_SHORT).show() }
                                        )
                                    }
                                }) {
                                    Text("Lưu", fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { isEditingInfo = false }) {
                                    Text("Hủy", color = Color.Gray)
                                }
                            } else {
                                TextButton(onClick = { isEditingInfo = true }) {
                                    Text("Chỉnh sửa", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                ) {
                    StaffProfileInfoRow("Họ tên", name, { name = it }, isEditingInfo, editable = true)
                    StaffProfileInfoRow("Giới tính", gender, { gender = it }, isEditingInfo, editable = true)
                    StaffProfileInfoRow("Ngày sinh", dob, { dob = it }, isEditingInfo, editable = true)
                    StaffProfileInfoRow("Địa chỉ", address, { address = it }, isEditingInfo, editable = true)
                    StaffProfileInfoRow("Email", email, { email = it }, isEditingInfo, editable = true)
                    StaffProfileInfoRow("SĐT", phone, { phone = it }, isEditingInfo, editable = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3.2 Giới thiệu bản thân
                StaffSectionCard("GIỚI THIỆU BẢN THÂN") {
                    if (guideProfile?.bio.isNullOrEmpty()) {
                        Text("Bạn chưa có giới thiệu", color = Color.Gray, fontSize = 14.sp)
                        Button(
                            onClick = { 
                                tempBio = ""
                                showBioDialog = true 
                            },
                            modifier = Modifier.padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(0.1f))
                        ) {
                            Text("+ Thêm", color = primaryColor)
                        }
                    } else {
                        Text(guideProfile?.bio ?: "", fontSize = 14.sp, color = Color(0xFF334155))
                        TextButton(onClick = { 
                            tempBio = guideProfile?.bio ?: ""
                            showBioDialog = true 
                        }, modifier = Modifier.align(Alignment.End)) {
                            Text("Chỉnh sửa", color = primaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3.3 Kỹ năng
                StaffSectionCard("KỸ NĂNG") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        guideProfile?.skills?.forEach { skill ->
                            Surface(
                                color = primaryColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, primaryColor.copy(0.1f))
                            ) {
                                Text(
                                    skill, 
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 13.sp,
                                    color = primaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    TextButton(onClick = { onNavigate("staff_skills") }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Quản lý kỹ năng", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3.4 KINH NGHIỆM
                StaffSectionCard("KINH NGHIỆM") {
                    if (guideProfile?.experiences.isNullOrEmpty()) {
                        Text("Bạn chưa thêm kinh nghiệm", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        guideProfile?.experiences?.forEach { exp ->
                            StaffExperienceItem(exp)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                        }
                    }
                    Button(
                        onClick = { 
                            expTitle = ""; expStart = ""; expEnd = ""; expDesc = ""
                            showExpDialog = true 
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm kinh nghiệm")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- PHẦN 3: CÁC NÚT CHỨC NĂNG ---
                Text("CÔNG CỤ TÁC VỤ", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StaffActionButton("Lịch Tour", Icons.Default.CalendarMonth, Modifier.weight(1f)) { onNavigate("staff_schedule") }
                    StaffActionButton("Chi tiết chuyến đi", Icons.AutoMirrored.Filled.Assignment, Modifier.weight(1f)) { onNavigate("staff_trip_detail") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StaffActionButton("Ghi chú chuyến đi", Icons.AutoMirrored.Filled.NoteAdd, Modifier.weight(1f)) { onNavigate("staff_notes") }
                    StaffActionButton("Đổi mật khẩu", Icons.Default.LockPerson, Modifier.weight(1f)) { onNavigate("change_password") }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Dialogs
    if (showBioDialog) {
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Giới thiệu bản thân") },
            text = {
                OutlinedTextField(
                    value = tempBio,
                    onValueChange = { tempBio = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    placeholder = { Text("Nhập giới thiệu của bạn...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    staffViewModel.updateBio(tempBio) {
                        showBioDialog = false
                        Toast.makeText(context, "Đã cập nhật giới thiệu", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showBioDialog = false }) { Text("Hủy") } }
        )
    }

    if (showExpDialog) {
        AlertDialog(
            onDismissRequest = { showExpDialog = false },
            title = { Text("Thêm kinh nghiệm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = expTitle, onValueChange = { expTitle = it }, label = { Text("Chức danh *") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = expStart, onValueChange = { expStart = it }, label = { Text("Bắt đầu *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = expEnd, onValueChange = { expEnd = it }, label = { Text("Kết thúc") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(
                        value = expDesc, 
                        onValueChange = { expDesc = it }, 
                        label = { Text("Mô tả chi tiết *") }, 
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("BẮT BUỘC: Nhập mô tả...") }
                    )
                    Text("* Thông tin bắt buộc", fontSize = 11.sp, color = Color.Red)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (expTitle.isEmpty() || expStart.isEmpty() || expDesc.isEmpty()) {
                        Toast.makeText(context, "Vui lòng điền đủ: Chức danh, Năm bắt đầu và Mô tả", Toast.LENGTH_LONG).show()
                    } else {
                        staffViewModel.addExperience(
                            Experience(title = expTitle, startTime = expStart, endTime = expEnd, description = expDesc),
                            onSuccess = { 
                                showExpDialog = false
                                Toast.makeText(context, "Đã thêm kinh nghiệm", Toast.LENGTH_SHORT).show()
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    }
                }) { Text("Thêm") }
            },
            dismissButton = { TextButton(onClick = { showExpDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun StaffSectionCard(title: String, action: @Composable (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StaffProfileInfoRow(label: String, value: String, onValueChange: (String) -> Unit, isEditing: Boolean, editable: Boolean) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        if (isEditing && editable) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                singleLine = true
            )
        } else {
            Text(value.ifEmpty { "Chưa cập nhật" }, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF1E293B))
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
private fun StaffExperienceItem(exp: Experience) {
    Column {
        Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
        Text("${exp.startTime} - ${exp.endTime.ifEmpty { "Hiện tại" }}", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(exp.description, fontSize = 14.sp, color = Color(0xFF475569), lineHeight = 20.sp)
    }
}

@Composable
private fun StaffActionButton(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFF2563EB).copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}
