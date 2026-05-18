package com.example.dacs3.ui.screens.user

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
            gioiTinh = it.gioi_tinh ?: ""
            ngaySinh = it.ngay_sinh ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !userViewModel.isLoading.value
            ) {
                if (userViewModel.isLoading.value) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("LƯU THAY ĐỔI", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Column {
        Text(
            label, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface, 
                fontWeight = FontWeight.Medium, 
                fontSize = 15.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = primaryColor,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
