package com.example.dacs3.ui.components.tours

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.Tour
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FilterTag(text: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
        )
    }
}

@Composable
fun TourCard(tour: Tour, onClick: (Tour) -> Unit = {}) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick(tour) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // Left Side: Image
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = tour.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Rating Overlay
                Surface(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFACC15), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tour.rating.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Side: Info
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = tour.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tour.location, color = Color(0xFF64748B), fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Icon(Icons.Default.Timer, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tour.duration, color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = currencyFormatter.format(tour.price),
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text("mỗi khách", color = Color.Gray, fontSize = 9.sp)
                    }
                    
                    IconButton(
                        onClick = { onClick(tour) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2563EB), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterContent(onApply: () -> Unit) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bộ lọc tìm kiếm", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. Loại tour
        FilterSectionTitle("Loại tour")
        Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterSelectButton("Tất cả", true)
            FilterSelectButton("Trong ngày", false)
            FilterSelectButton("Dài ngày", false)
        }

        // 2. Địa điểm
        FilterSectionTitle("Địa điểm")
        val locations = listOf("Đà Nẵng" to 12, "Hội An" to 8, "Huế" to 5, "Quảng Bình" to 3, "Đà Lạt" to 7)
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            locations.forEach { (name, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = name == "Đà Nẵng", onCheckedChange = {})
                        Text(name, fontSize = 15.sp, color = Color(0xFF334155))
                    }
                    Text("($count)", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        // 3. Khoảng giá
        FilterSectionTitle("Khoảng giá")
        var priceRange by remember { mutableStateOf(0f..10000000f) }
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            RangeSlider(
                value = priceRange,
                onValueChange = { priceRange = it },
                valueRange = 0f..10000000f,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0đ", fontSize = 12.sp, color = Color.Gray)
                Text("10tr+ đ", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // 4. Thời gian
        FilterSectionTitle("Thời gian")
        val durations = listOf("1 ngày", "2-3 ngày", "4-5 ngày", "6+ ngày")
        LazyRow(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(durations) { duration ->
                FilterSelectButton(duration, duration == "2-3 ngày")
            }
        }

        // 5. Đánh giá
        FilterSectionTitle("Đánh giá")
        val ratings = listOf(
            Triple("9.0+", "Tuyệt vời", Color(0xFF1E3A8A)),
            Triple("8.0+", "Rất tốt", Color(0xFF2563EB)),
            Triple("7.0+", "Tốt", Color(0xFF0EA5E9)),
            Triple("6.0+", "Khá", Color(0xFFF59E0B))
        )
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            ratings.forEach { (score, label, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = score == "8.0+", onClick = {})
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
                        Text(score, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, fontSize = 15.sp, color = Color(0xFF334155))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Nút hành động
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Đặt lại", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Áp dụng", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FilterSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        color = Color(0xFF0F172A),
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
fun FilterSelectButton(text: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
        modifier = Modifier.clickable { }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B)
            )
        }
    }
}
