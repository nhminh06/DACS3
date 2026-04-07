package com.example.dacs3.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.ui.viewmodel.StaffViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StaffSkillsScreen(
    staffViewModel: StaffViewModel,
    onBack: () -> Unit
) {
    val guide by staffViewModel.guideProfile
    val context = LocalContext.current
    val primaryColor = Color(0xFF2563EB)
    
    var newSkill by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf<String>() }

    LaunchedEffect(guide) {
        guide?.skills?.let {
            skills.clear()
            skills.addAll(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kỹ năng", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF1E293B))
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
                .padding(24.dp)
        ) {
            Text("Thêm kỹ năng mới", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("VD: Tiếng Anh, Giao tiếp...") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = primaryColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newSkill.isNotBlank()) {
                            if (!skills.contains(newSkill.trim())) {
                                skills.add(newSkill.trim())
                                newSkill = ""
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Kỹ năng của bạn", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach { skill ->
                    SkillTag(
                        name = skill,
                        onDelete = { skills.remove(skill) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    staffViewModel.updateSkills(
                        newSkills = skills.toList(),
                        onSuccess = {
                            Toast.makeText(context, "Đã lưu kỹ năng", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White)
            ) {
                Text("Lưu thay đổi", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SkillTag(name: String, onDelete: () -> Unit) {
    Surface(
        color = Color(0xFFEFF6FF),
        shape = RoundedCornerShape(100.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDBEAFE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, color = Color(0xFF1E40AF), fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp).clickable { onDelete() },
                tint = Color(0xFF1E40AF)
            )
        }
    }
}
