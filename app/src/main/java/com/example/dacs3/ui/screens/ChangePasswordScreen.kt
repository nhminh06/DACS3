package com.example.dacs3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    val primaryColor = Color(0xFF2563EB)
    val backgroundColor = Color(0xFFF8FAFC)
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Tạo mật khẩu mới",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Text(
                "Mật khẩu của bạn phải có ít nhất 8 ký tự",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Current Password
            PasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Mật khẩu hiện tại",
                isVisible = currentPasswordVisible,
                onVisibilityChange = { currentPasswordVisible = !currentPasswordVisible },
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Password
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Mật khẩu mới",
                isVisible = newPasswordVisible,
                onVisibilityChange = { newPasswordVisible = !newPasswordVisible },
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Xác nhận mật khẩu mới",
                isVisible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                primaryColor = primaryColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { /* Handle Change Password */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Cập nhật mật khẩu", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit,
    primaryColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor) },
        trailingIcon = {
            val image = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = onVisibilityChange) {
                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primaryColor
        ),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true
    )
}
