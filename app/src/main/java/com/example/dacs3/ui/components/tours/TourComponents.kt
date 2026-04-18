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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FilterTag(text: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
        modifier = Modifier.padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF334155)
        )
    }
}

@Composable
fun TourCard(tour: Tour, onClick: (Tour) -> Unit = {}) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("vi", "VN")) }
    val scaleInfo = tour.getTourScaleInfo()
    val appBlue = Color(0xFF2563EB)
    
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
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(tour.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Rating overlay
                if (tour.rating > 0 && tour.reviewCount > 0) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp)
                            .align(Alignment.TopStart),
                        color = Color.Black.copy(alpha = 0.5f),
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
                } else {
                    Surface(
                        modifier = Modifier
                            .padding(top = 12.dp, start = 8.dp)
                            .align(Alignment.TopStart),
                        color = appBlue.copy(alpha = 0.7f), // Màu xanh biển của app, trong suốt 0.7
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Mới",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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

                    Spacer(modifier = Modifier.height(4.dp))

                    if (scaleInfo != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, null, tint = appBlue, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(scaleInfo.transport, color = appBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tour.location, color = Color(0xFF475569), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Icon(Icons.Default.Timer, null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tour.duration, color = Color(0xFF475569), fontSize = 11.sp)
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
                            color = appBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text("mỗi khách", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    IconButton(
                        onClick = { onClick(tour) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(appBlue, CircleShape)
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
fun FilterContent(viewModel: MainViewModel, onApply: () -> Unit) {
    val scrollState = rememberScrollState()
    val selectedTourType by viewModel.selectedTourType.collectAsState()
    val availableProvinces by viewModel.availableProvinces.collectAsState()
    val selectedLocations by viewModel.selectedLocations.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val selectedDuration by viewModel.selectedDuration.collectAsState()
    val selectedRating by viewModel.selectedRating.collectAsState()
    val primaryColor = Color(0xFF2563EB)

    // Internal state for text fields, initialized empty if they are default values
    var minPriceText by remember { 
        mutableStateOf(if (priceRange.start == 0f) "" else priceRange.start.toLong().toString()) 
    }
    var maxPriceText by remember { 
        mutableStateOf(if (priceRange.endInclusive >= 1000000000f) "" else priceRange.endInclusive.toLong().toString()) 
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bộ lọc tìm kiếm", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. Loại tour
        FilterSectionTitle("Loại tour")
        Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterSelectButton("Tất cả", selectedTourType == "Tất cả") { viewModel.setTourType("Tất cả") }
            FilterSelectButton("Trong ngày", selectedTourType == "Trong ngày") { viewModel.setTourType("Trong ngày") }
            FilterSelectButton("Dài ngày", selectedTourType == "Dài ngày") { viewModel.setTourType("Dài ngày") }
        }

        // 2. Địa điểm
        FilterSectionTitle("Địa điểm")
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            if (availableProvinces.isEmpty()) {
                Text("Đang tải địa điểm...", color = Color(0xFF64748B), fontSize = 14.sp)
            } else {
                availableProvinces.forEach { name ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.toggleLocation(name) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedLocations.contains(name), onCheckedChange = { viewModel.toggleLocation(name) })
                            Text(name, fontSize = 15.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // 3. Khoảng giá
        FilterSectionTitle("Khoảng giá (đ)")
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = minPriceText,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        minPriceText = it
                        viewModel.setMinPrice(it.toFloatOrNull())
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("0", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
                label = { Text("Từ", fontSize = 12.sp, color = Color(0xFF475569)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color(0xFF475569)
                )
            )
            
            Text("-", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

            OutlinedTextField(
                value = maxPriceText,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        maxPriceText = it
                        viewModel.setMaxPrice(it.toFloatOrNull())
                    }
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Không giới hạn", fontSize = 14.sp, color = Color(0xFF94A3B8)) },
                label = { Text("Đến", fontSize = 12.sp, color = Color(0xFF475569)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedLabelColor = Color(0xFF475569)
                )
            )
        }

        // 4. Thời gian
        FilterSectionTitle("Thời gian")
        val durations = listOf("Tất cả", "1 ngày", "2-3 ngày", "4-5 ngày", "6+ ngày")
        LazyRow(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(durations) { duration ->
                FilterSelectButton(duration, selectedDuration == duration) { viewModel.setDuration(duration) }
            }
        }

        // 5. Đánh giá
        FilterSectionTitle("Đánh giá")
        val ratings = listOf(
            Triple(9.0f, "Tuyệt vời (9.0+)", Color(0xFF1E3A8A)),
            Triple(8.0f, "Rất tốt (8.0+)", Color(0xFF2563EB)),
            Triple(7.0f, "Tốt (7.0+)", Color(0xFF0EA5E9)),
            Triple(6.0f, "Khá (6.0+)", Color(0xFFF59E0B))
        )
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            ratings.forEach { (score, label, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setRating(score) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedRating == score, onClick = { viewModel.setRating(score) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
                        Text(score.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(label, fontSize = 15.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { 
                    viewModel.resetFilters()
                    minPriceText = ""
                    maxPriceText = ""
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
            ) {
                Text("Đặt lại", fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            }
            Button(
                onClick = {
                    viewModel.applyFilters()
                    onApply()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White)
            ) {
                Text("Áp dụng", fontWeight = FontWeight.Bold, color = Color.White)
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
fun FilterSelectButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)),
        modifier = Modifier.clickable { onClick() }
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
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF334155)
            )
        }
    }
}
