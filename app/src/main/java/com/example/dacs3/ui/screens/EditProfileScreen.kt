package com.example.dacs3.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(userViewModel: UserViewModel, onBack: () -> Unit) {
    val user by userViewModel.currentUser
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Sử dụng LaunchedEffect để cập nhật lại các trường khi dữ liệu user thay đổi
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sdt by remember { mutableStateOf("") }
    var diaChi by remember { mutableStateOf("") }
    var gioiTinh by remember { mutableStateOf("") }
    var ngaySinh by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.let {
            name = it.name
            email = it.email
            sdt = it.sdt
            diaChi = it.dia_chi
            gioiTinh = it.gioi_tinh
            ngaySinh = it.ngay_sinh
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val updatedUser = user?.copy(
                                name = name,
                                email = email,
                                sdt = sdt,
                                dia_chi = diaChi,
                                gioi_tinh = gioiTinh,
                                ngay_sinh = ngaySinh
                            )
                            if (updatedUser != null) {
                                userViewModel.updateUserInfo(
                                    updatedUser, 
                                    onSuccess = {
                                        Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }, 
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        enabled = !userViewModel.isLoading.value
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF2563EB))
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditField(label = "Họ và tên", value = name, onValueChange = { name = it })
            EditField(label = "Email", value = email, onValueChange = { email = it })
            EditField(label = "Số điện thoại", value = sdt, onValueChange = { sdt = it })
            EditField(label = "Địa chỉ", value = diaChi, onValueChange = { diaChi = it })
            EditField(label = "Giới tính", value = gioiTinh, onValueChange = { gioiTinh = it })
            EditField(label = "Ngày sinh", value = ngaySinh, onValueChange = { ngaySinh = it })
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {
                    val updatedUser = user?.copy(
                        name = name,
                        email = email,
                        sdt = sdt,
                        dia_chi = diaChi,
                        gioi_tinh = gioiTinh,
                        ngay_sinh = ngaySinh
                    )
                    if (updatedUser != null) {
                        userViewModel.updateUserInfo(
                            updatedUser, 
                            onSuccess = {
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                onBack()
                            }, 
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                enabled = !userViewModel.isLoading.value
            ) {
                if (userViewModel.isLoading.value) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Lưu thay đổi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Medium, color = Color(0xFF64748B), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
