package com.example.dacs3.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthPreviewContainer(startWithLogin: Boolean = true) {
    var showLogin by remember { mutableStateOf(startWithLogin) }

    if (showLogin) {
        LoginScreen(
            onNavigateToRegister = { showLogin = false },
            onLoginSuccess = { /* Preview: Do nothing */ }
        )
    } else {
        RegisterScreen(
            onNavigateToLogin = { showLogin = true },
            onRegisterSuccess = { showLogin = true }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Màn hình Đăng nhập")
@Composable
fun PreviewLogin() {
    AuthPreviewContainer(startWithLogin = true)
}

@Preview(showBackground = true, showSystemUi = true, name = "Màn hình Đăng ký")
@Composable
fun PreviewRegister() {
    AuthPreviewContainer(startWithLogin = false)
}
