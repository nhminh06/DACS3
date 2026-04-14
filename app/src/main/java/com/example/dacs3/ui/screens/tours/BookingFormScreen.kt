package com.example.dacs3.ui.screens.tours

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
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
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.example.dacs3.data.model.Tour
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.example.dacs3.ui.viewmodel.BookingViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    tour: Tour,
    initialAdults: Int = 1,
    initialChildren: Int = 0,
    initialInfants: Int = 0,
    onNavigateBack: () -> Unit,
    onBookingSuccess: () -> Unit,
    userViewModel: UserViewModel,
    bookingViewModel: BookingViewModel
) {
    val context = LocalContext.current
    val user = userViewModel.currentUser.value
    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    
    // Parse available dates from tour.startDate (comma-separated yyyy-MM-dd)
    val availableDates = remember(tour.startDate) {
        tour.startDate.split(",")
            .filter { it.isNotBlank() }
            .map { dateStr ->
                try {
                    LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (e: Exception) {
                    dateStr.trim()
                }
            }
    }

    // Form State
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.sdt ?: "") }
    var address by remember { mutableStateOf(user?.dia_chi ?: "") }
    var note by remember { mutableStateOf("") }
    
    // Default to the first available date
    var selectedDate by remember { 
        mutableStateOf(availableDates.firstOrNull() ?: LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) 
    }
    
    var adults by remember { mutableIntStateOf(initialAdults) }
    var children by remember { mutableIntStateOf(initialChildren) }
    var infants by remember { mutableIntStateOf(initialInfants) }
    
    var paymentMethod by remember { mutableStateOf("QR") }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val priceTreEm = if (tour.giaTreEm > 0) tour.giaTreEm else (tour.price * 0.7).toLong()
    val priceTreSoSinh = if (tour.giaTreNho > 0) tour.giaTreNho else (tour.price * 0.5).toLong()
    val totalPrice = (adults * tour.price) + (children * priceTreEm) + (infants * priceTreSoSinh)
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    // Tối ưu hóa VietQR
    var debouncedAmount by remember { mutableLongStateOf(totalPrice) }
    var debouncedName by remember { mutableStateOf(name) }
    
    LaunchedEffect(totalPrice, name) {
        delay(800)
        debouncedAmount = totalPrice
        debouncedName = name
    }

    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess == true) {
            showSuccessDialog = true
            bookingViewModel.resetBookingStatus()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBookingSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("XEM ĐƠN ĐẶT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(48.dp)) },
            title = { Text("Đặt Tour Thành Công!", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
            text = { Text("Yêu cầu đặt tour của bạn đã được gửi đi. Vui lòng chờ nhân viên xác nhận và thanh toán để hoàn tất.", color = Color(0xFF334155)) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đặt tour", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF0F172A),
                    navigationIconContentColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.navigationBarsPadding().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng thanh toán:", color = Color(0xFF475569), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(currencyFormatter.format(totalPrice), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF2563EB))
                    }
                    Button(
                        onClick = {
                            if (paymentMethod == "QR" && receiptUri == null) {
                                Toast.makeText(context, "Vui lòng tải ảnh biên lai chuyển khoản", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            try {
                                val date = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                val bookingId = "BK${System.currentTimeMillis()}"
                                
                                val newBooking = Booking(
                                    id = bookingId,
                                    userId = user?.id ?: "",
                                    tour = tour,
                                    status = BookingStatus.PENDING,
                                    startDate = date,
                                    adults = adults,
                                    children = children,
                                    infants = infants,
                                    totalPrice = totalPrice,
                                    customerName = name,
                                    email = email,
                                    phone = phone,
                                    address = address,
                                    note = note.ifBlank { null },
                                    paymentMethod = paymentMethod
                                )
                                
                                bookingViewModel.createBooking(
                                    newBooking, 
                                    if (paymentMethod == "QR") receiptUri else null
                                )
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                        enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("XÁC NHẬN ĐẶT TOUR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
        ) {
            item { BookingTourCard(tour, selectedDate) }
            
            item { 
                SectionTitle("THÔNG TIN KHÁCH HÀNG")
                CustomerFormSection(
                    name = name, onNameChange = { name = it },
                    email = email, onEmailChange = { email = it },
                    phone = phone, onPhoneChange = { phone = it },
                    address = address, onAddressChange = { address = it },
                    note = note, onNoteChange = { note = it }
                )
            }
            
            item {
                SectionTitle("NGÀY KHỞI HÀNH")
                AvailableDatesSection(
                    selectedDate = selectedDate,
                    availableDates = availableDates,
                    onDateSelected = { selectedDate = it }
                )
            }
            
            item {
                SectionTitle("SỐ LƯỢNG KHÁCH")
                PassengerSection(
                    adults, { if (it >= 1) adults = it },
                    children, { if (it >= 0) children = it },
                    infants, { if (it >= 0) infants = it }
                )
            }
            
            item {
                SectionTitle("CHI TIẾT THANH TOÁN")
                PriceSummarySection(tour, adults, children, infants, totalPrice, currencyFormatter)
            }
            
            item {
                SectionTitle("PHƯƠNG THỨC THANH TOÁN")
                PaymentMethodSection(
                    selected = paymentMethod, 
                    onSelect = { paymentMethod = it }, 
                    amount = debouncedAmount, 
                    name = debouncedName, 
                    receiptUri = receiptUri,
                    onReceiptSelected = { receiptUri = it }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1E293B)
    )
}

@Composable
fun BookingTourCard(tour: Tour, date: String) {
    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = tour.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(tour.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tour.location, color = Color(0xFF334155), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Khởi hành: $date", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun CustomerFormSection(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    note: String, onNoteChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Họ tên *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF334155)
                ),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF334155)
                ),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF2563EB)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone, onValueChange = onPhoneChange,
                label = { Text("Số điện thoại *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF334155)
                ),
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF2563EB)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = address, onValueChange = onAddressChange,
                label = { Text("Địa chỉ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF334155)
                ),
                minLines = 2,
                leadingIcon = { Icon(Icons.Default.Home, null, tint = Color(0xFF2563EB)) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note, onValueChange = onNoteChange,
                label = { Text("Ghi chú (Không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 15.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedLabelColor = Color(0xFF2563EB),
                    unfocusedLabelColor = Color(0xFF334155)
                ),
                minLines = 3,
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Note, null, tint = Color(0xFF2563EB)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableDatesSection(
    selectedDate: String,
    availableDates: List<String>,
    onDateSelected: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().clickable {
            if (availableDates.size > 1) showSheet = true
        },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Event, null, tint = Color(0xFF2563EB))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Chọn ngày khởi hành", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Text(selectedDate, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
            }
            Spacer(modifier = Modifier.weight(1f))
            if (availableDates.size > 1) {
                Icon(Icons.Default.ExpandMore, null, tint = Color(0xFF64748B))
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                Text(
                    "Danh sách ngày khởi hành", 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 18.sp, 
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                availableDates.forEach { date ->
                    val isSelected = date == selectedDate
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onDateSelected(date)
                                showSheet = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFF0F7FF) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, Color(0xFF2563EB)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                date, 
                                modifier = Modifier.weight(1f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF0F172A)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerSection(
    adults: Int, onAdultsChange: (Int) -> Unit,
    children: Int, onChildrenChange: (Int) -> Unit,
    infants: Int, onInfantsChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PassengerStepper("Người lớn", "Trên 12 tuổi", adults, onAdultsChange)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
            PassengerStepper("Trẻ em", "Dưới 12 tuổi", children, onChildrenChange)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
            PassengerStepper("Trẻ sơ sinh", "Dưới 2 tuổi", infants, onInfantsChange)
        }
    }
}

@Composable
fun PassengerStepper(label: String, subLabel: String, count: Int, onValueChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A))
            Text(subLabel, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange(count - 1) },
                modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F172A))
            }
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color(0xFF0F172A)
            )
            IconButton(
                onClick = { onValueChange(count + 1) },
                modifier = Modifier.size(32.dp).background(Color(0xFF2563EB), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PriceSummarySection(tour: Tour, adults: Int, children: Int, infants: Int, total: Long, formatter: NumberFormat) {
    val priceTreEm = if (tour.giaTreEm > 0) tour.giaTreEm else (tour.price * 0.7).toLong()
    val priceTreSoSinh = if (tour.giaTreNho > 0) tour.giaTreNho else (tour.price * 0.5).toLong()

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PriceRow("Người lớn (${adults} x ${formatter.format(tour.price)})", formatter.format(adults * tour.price))
            if (children > 0) PriceRow("Trẻ em (${children} x ${formatter.format(priceTreEm)})", formatter.format(children * priceTreEm))
            if (infants > 0) PriceRow("Trẻ sơ sinh (${infants} x ${formatter.format(priceTreSoSinh)})", formatter.format(infants * priceTreSoSinh))
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Color(0xFFF1F5F9))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("TỔNG TIỀN", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                Text(formatter.format(total), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 14.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun PaymentMethodSection(
    selected: String, 
    onSelect: (String) -> Unit, 
    amount: Long, 
    name: String,
    receiptUri: Uri?,
    onReceiptSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onReceiptSelected(it) }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PaymentTab("Chuyển khoản QR", selected == "QR", Modifier.weight(1f)) { onSelect("QR") }
            PaymentTab("Tiền mặt", selected == "CASH", Modifier.weight(1f)) { onSelect("CASH") }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedContent(targetState = selected, label = "") { target ->
            if (target == "QR") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("QUÉT MÃ THANH TOÁN VIETQR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E40AF))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp)).padding(12.dp)) {
                            val shortName = name.split(" ").lastOrNull()?.uppercase() ?: "KHACH"
                            val qrUrl = "https://img.vietqr.io/image/vcb-7899883653-compact2.jpg?amount=$amount&addInfo=DATTOUR%20$shortName&accountName=WIND%20Travel"
                            
                            AsyncImage(
                                model = qrUrl,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(220.dp),
                                placeholder = null
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Ngân hàng", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("Vietcombank", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F172A))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Nội dung chuyển", fontSize = 11.sp, color = Color(0xFF475569))
                                Text("DATTOUR ${name.split(" ").lastOrNull()?.uppercase() ?: "KHACH"}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        if (receiptUri != null) {
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = receiptUri,
                                    contentDescription = "Selected Receipt",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black.copy(0.5f), CircleShape).size(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF2563EB)),
                            border = BorderStroke(1.dp, Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (receiptUri == null) "Tải ảnh biên lai lên" else "Thay đổi ảnh biên lai", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Bạn sẽ thanh toán trực tiếp cho Hướng dẫn viên khi bắt đầu chuyến đi.", fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTab(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF2563EB) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.White else Color(0xFF334155), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}
