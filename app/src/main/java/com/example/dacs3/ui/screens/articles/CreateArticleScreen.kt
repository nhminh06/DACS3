package com.example.dacs3.ui.screens.articles

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dacs3.data.model.ArticleCategory
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateArticleScreen(
    userViewModel: UserViewModel,
    articleViewModel: ArticleViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(0xFF2563EB)
    val coroutineScope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ArticleCategory.CULTURE) }
    val sections = remember { mutableStateListOf<MutableMap<String, String>>(mutableMapOf("tieu_de" to "", "noi_dung" to "", "hinh_anh" to "")) }
    
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đóng góp bài viết", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
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
            
            // Tiêu đề chính
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề bài viết *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = primaryColor
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chọn danh mục
            Text("Chủ đề bài viết", fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 14.sp)
            Row(modifier = Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryChipItem("Văn hóa", selectedCategory == ArticleCategory.CULTURE) { selectedCategory = ArticleCategory.CULTURE }
                CategoryChipItem("Ẩm thực", selectedCategory == ArticleCategory.CUISINE) { selectedCategory = ArticleCategory.CUISINE }
                CategoryChipItem("Làng nghề", selectedCategory == ArticleCategory.CRAFT_VILLAGE) { selectedCategory = ArticleCategory.CRAFT_VILLAGE }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Các mục nội dung
            Text("Nội dung bài viết", fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 14.sp)
            
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
            
            // Nút thêm mục
            OutlinedButton(
                onClick = { sections.add(mutableMapOf("tieu_de" to "", "noi_dung" to "", "hinh_anh" to "")) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Icon(Icons.Default.Add, null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm mục nội dung", color = primaryColor, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {
                    if (title.isBlank() || sections.all { it["noi_dung"].isNullOrBlank() }) {
                        Toast.makeText(context, "Vui lòng nhập tiêu đề và nội dung", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSubmitting = true
                    val newArticle = ArticleEntity(
                        tieu_de = title,
                        loai_id = when(selectedCategory) {
                            ArticleCategory.CRAFT_VILLAGE -> 1
                            ArticleCategory.CUISINE -> 2
                            ArticleCategory.CULTURE -> 3
                        },
                        sections = sections.toList(),
                        ngay_tao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        trang_thai = 0, // Chờ duyệt
                        nguon_goc = "user",
                        tac_gia = userViewModel.currentUser.value?.name ?: "Người dùng"
                    )
                    
                    articleViewModel.createArticle(newArticle) { success ->
                        isSubmitting = false
                        if (success) {
                            Toast.makeText(context, "Gửi bài viết thành công! Vui lòng chờ phê duyệt.", Toast.LENGTH_LONG).show()
                            onBack()
                        } else {
                            Toast.makeText(context, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.White),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("GỬI BÀI VIẾT", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CategoryChipItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF2563EB) else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            label, 
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun SectionInputItem(
    index: Int,
    section: Map<String, String>,
    viewModel: ArticleViewModel,
    onDelete: () -> Unit,
    onUpdate: (String, String) -> Unit
) {
    val primaryColor = Color(0xFF2563EB)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                isUploading = true
                val url = viewModel.uploadImage(it)
                if (url != null) {
                    onUpdate("hinh_anh", url)
                    Toast.makeText(context, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mục ${index + 1}", fontWeight = FontWeight.Bold, color = primaryColor)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f))
                }
            }
            
            OutlinedTextField(
                value = section["tieu_de"] ?: "",
                onValueChange = { onUpdate("tieu_de", it) },
                label = { Text("Tiêu đề mục (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = section["noi_dung"] ?: "",
                onValueChange = { onUpdate("noi_dung", it) },
                label = { Text("Nội dung mục *") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Lựa chọn hình ảnh
            Text("Hình ảnh mục", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = primaryColor),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = primaryColor)
                    } else {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Từ máy", fontSize = 13.sp)
                    }
                }
                
                Text("hoặc", fontSize = 12.sp, color = Color.Gray)
                
                OutlinedTextField(
                    value = section["hinh_anh"] ?: "",
                    onValueChange = { onUpdate("hinh_anh", it) },
                    label = { Text("Link URL") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("https://...") },
                    textStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            if (!section["hinh_anh"].isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = section["hinh_anh"],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { onUpdate("hinh_anh", "") },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(0.5f), CircleShape).size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
