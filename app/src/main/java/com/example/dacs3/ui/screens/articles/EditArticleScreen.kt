package com.example.dacs3.ui.screens.articles

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.ArticleCategory
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(
    article: ArticleEntity,
    userViewModel: UserViewModel,
    articleViewModel: ArticleViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFF2563EB)
    
    var title by remember { mutableStateOf(article.tieu_de) }
    var selectedCategory by remember { 
        mutableStateOf(
            when(article.loai_id) {
                1 -> ArticleCategory.CRAFT_VILLAGE
                2 -> ArticleCategory.CUISINE
                else -> ArticleCategory.CULTURE
            }
        ) 
    }
    
    val sections = remember { 
        mutableStateListOf<MutableMap<String, String>>().apply {
            addAll(article.sections.map { it.toMutableMap() })
        } 
    }
    
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa bài viết", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề bài viết *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Chủ đề bài viết", fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 14.sp)
            Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChipItem("Văn hóa", selectedCategory == ArticleCategory.CULTURE) { selectedCategory = ArticleCategory.CULTURE }
                CategoryChipItem("Ẩm thực", selectedCategory == ArticleCategory.CUISINE) { selectedCategory = ArticleCategory.CUISINE }
                CategoryChipItem("Làng nghề", selectedCategory == ArticleCategory.CRAFT_VILLAGE) { selectedCategory = ArticleCategory.CRAFT_VILLAGE }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            sections.forEachIndexed { index, section ->
                SectionInputItem(
                    index = index,
                    section = section,
                    viewModel = articleViewModel,
                    onDelete = { if (sections.size > 1) sections.removeAt(index) },
                    onUpdate = { key, value ->
                        val updated = section.toMutableMap()
                        updated[key] = value
                        sections[index] = updated
                    }
                )
            }
            
            OutlinedButton(
                onClick = { sections.add(mutableMapOf("tieu_de" to "", "noi_dung" to "", "hinh_anh" to "")) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm mục nội dung")
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {
                    if (title.isBlank() || sections.all { it["noi_dung"].isNullOrBlank() }) {
                        Toast.makeText(context, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSubmitting = true
                    val updatedArticle = article.copy(
                        tieu_de = title,
                        loai_id = when(selectedCategory) {
                            ArticleCategory.CRAFT_VILLAGE -> 1
                            ArticleCategory.CUISINE -> 2
                            ArticleCategory.CULTURE -> 3
                        },
                        sections = sections.toList()
                    )
                    
                    articleViewModel.updateArticle(updatedArticle) { success ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Cập nhật bài viết thành công!", Toast.LENGTH_SHORT).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("CẬP NHẬT BÀI VIẾT", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
