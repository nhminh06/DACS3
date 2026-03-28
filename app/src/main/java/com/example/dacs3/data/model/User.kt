package com.example.dacs3.data.model

import com.google.firebase.Timestamp

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val avatar: String = "",
    val rank: String = "Bronze",
    val role: String = "user",
    val sdt: String = "",
    val dia_chi: String = "",
    val gioi_tinh: String = "",
    val ngay_sinh: String = "",
    val trang_thai: String = "active",
    val created_at: Timestamp? = null
)
