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
    val dateGuestCounts by bookingViewModel.dateGuestCounts.collectAsState()

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

    LaunchedEffect(tour.id, availableDates) {
        if (availableDates.isNotEmpty()) {
            bookingViewModel.loadGuestCountsForDates(tour.id, availableDates)
        }
    }

    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.sdt ?: "") }
    var address by remember { mutableStateOf(user?.dia_chi ?: "") }
    var note by remember { mutableStateOf("") }

    var selectedDate by remember {
        mutableStateOf(
            availableDates.firstOrNull()
                ?: LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        )
    }

    var adults by remember { mutableIntStateOf(initialAdults) }
    var children by remember { mutableIntStateOf(initialChildren) }
    var infants by remember { mutableIntStateOf(initialInfants) }

    var paymentMethod by remember { mutableStateOf("QR") }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val currentOccupied = dateGuestCounts["${tour.id}_$selectedDate"] ?: 0
    val totalRequested = adults + children + infants
    val overLimitCount = (currentOccupied + totalRequested) - tour.maxGuests

    val isOverLimit = overLimitCount > 0
    val shouldHidePaymentSection = overLimitCount >= 3
    val canBook = overLimitCount <= 2

    // Dùng helper functions thay vì truy cập trực tiếp
    val priceTreEm = if (tour.getGiaTreEm() > 0) tour.getGiaTreEm() else (tour.getPrice() * 0.7).toLong()
    val priceTreSoSinh = if (tour.getGiaTreNho() > 0) tour.getGiaTreNho() else (tour.getPrice() * 0.5).toLong()
    val totalPrice = (adults * tour.getPrice()) + (children * priceTreEm) + (infants * priceTreSoSinh)
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

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
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Đặt Tour Thành Công!",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Yêu cầu đặt tour của bạn đã được gửi đi. Vui lòng chờ nhân viên xác nhận để hoàn tất.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Xác nhận đặt tour",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tổng thanh toán:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currencyFormatter.format(totalPrice),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = {
                            if (!canBook) {
                                Toast.makeText(
                                    context,
                                    "Chuyến đi đã quá tải, không thể đặt thêm.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            if (paymentMethod == "QR" && receiptUri == null) {
                                Toast.makeText(
                                    context,
                                    "Vui lòng tải ảnh biên lai chuyển khoản",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            try {
                                val date = LocalDate.parse(
                                    selectedDate,
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                )
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
                                Toast.makeText(context, "Lỗi định dạng ngày", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canBook) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && canBook
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                if (canBook) "XÁC NHẬN ĐẶT TOUR" else "HẾT CHỖ TRỐNG",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            item { BookingTourCard(tour, selectedDate) }

            item {
                SectionTitle("THÔNG TIN KHÁCH HÀNG")
                CustomerFormSection(
                    name, { name = it },
                    email, { email = it },
                    phone, { phone = it },
                    address, { address = it },
                    note, { note = it }
                )
            }

            item {
                SectionTitle("NGÀY KHỞI HÀNH")
                AvailableDatesSection(
                    tour.id, tour.maxGuests, selectedDate,
                    availableDates, dateGuestCounts
                ) { selectedDate = it }
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

                if (shouldHidePaymentSection) {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "❌ Chuyến đi đã quá tải nghiêm trọng. Vui lòng giảm số lượng khách hoặc chọn ngày khác.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    if (isOverLimit) {
                        Card(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)), // Cam nhạt cảnh báo
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "⚠️ Chuyến đi đã vượt giới hạn quy định ($overLimitCount người). Bạn vẫn có thể đặt và thanh toán QR bình thường.",
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFFD97706),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    PaymentMethodSection(
                        selected = paymentMethod,
                        onSelect = { paymentMethod = it },
                        amount = debouncedAmount,
                        name = debouncedName,
                        receiptUri = receiptUri,
                        onReceiptSelected = { receiptUri = it }
                    )
                }
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
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun BookingTourCard(tour: Tour, date: String) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = tour.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    tour.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        tour.location,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Khởi hành: $date",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Họ tên *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone, onValueChange = onPhoneChange,
                label = { Text("Số điện thoại *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = address, onValueChange = onAddressChange,
                label = { Text("Địa chỉ") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                minLines = 2,
                leadingIcon = { Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = note, onValueChange = onNoteChange,
                label = { Text("Ghi chú (Không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                minLines = 3,
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Note,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableDatesSection(
    tourId: String,
    maxGuests: Int,
    selectedDate: String,
    availableDates: List<String>,
    guestCounts: Map<String, Int>,
    onDateSelected: (String) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    val currentGuestCount = guestCounts["${tourId}_$selectedDate"] ?: 0
    val currentRemaining = maxGuests - currentGuestCount

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { if (availableDates.size > 1) showSheet = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Chọn ngày khởi hành",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedDate,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        color = (if (currentRemaining > 0) Color(0xFF10B981) else Color(0xFFEF4444)).copy(
                            alpha = 0.1f
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentRemaining > 0) "Còn $currentRemaining chỗ" else "Hết chỗ (${currentGuestCount} khách)",
                            color = if (currentRemaining > 0) Color(0xFF059669) else Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            if (availableDates.size > 1) Icon(
                Icons.Default.ExpandMore,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    "Danh sách ngày khởi hành",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                availableDates.forEach { date ->
                    val isSelected = date == selectedDate
                    val count = guestCounts["${tourId}_$date"] ?: 0
                    val remaining = maxGuests - count
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onDateSelected(date); showSheet = false },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    date,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (remaining > 0) "Còn $remaining chỗ trống ($count khách đã đặt)" else "Đã hết chỗ ($count khách)",
                                    fontSize = 12.sp,
                                    color = if (remaining > 0) Color(0xFF059669) else Color(0xFFDC2626)
                                )
                            }
                            if (isSelected) Icon(
                                Icons.Default.Check,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PassengerStepper("Người lớn", "Trên 12 tuổi", adults, onAdultsChange)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            PassengerStepper("Trẻ em", "Dưới 12 tuổi", children, onChildrenChange)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            PassengerStepper("Trẻ nhỏ", "Dưới 2 tuổi", infants, onInfantsChange)
        }
    }
}

@Composable
fun PassengerStepper(
    label: String,
    subLabel: String,
    count: Int,
    onValueChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange(count - 1) },
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.Default.Remove,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { onValueChange(count + 1) },
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.Default.Add,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun PriceSummarySection(
    tour: Tour,
    adults: Int,
    children: Int,
    infants: Int,
    total: Long,
    formatter: NumberFormat
) {
    // Dùng helper functions thay vì truy cập trực tiếp
    val priceTreEm = if (tour.getGiaTreEm() > 0) tour.getGiaTreEm() else (tour.getPrice() * 0.7).toLong()
    val priceTreSoSinh = if (tour.getGiaTreNho() > 0) tour.getGiaTreNho() else (tour.getPrice() * 0.5).toLong()

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PriceRow(
                "Người lớn (${adults} x ${formatter.format(tour.getPrice())})",
                formatter.format(adults * tour.getPrice())
            )
            if (children > 0) PriceRow(
                "Trẻ em (${children} x ${formatter.format(priceTreEm)})",
                formatter.format(children * priceTreEm)
            )
            if (infants > 0) PriceRow(
                "Trẻ nhỏ (${infants} x ${formatter.format(priceTreSoSinh)})",
                formatter.format(infants * priceTreSoSinh)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "TỔNG TIỀN",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    formatter.format(total),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
    ) { uri: Uri? -> uri?.let { onReceiptSelected(it) } }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentTab("Chuyển khoản QR", selected == "QR", Modifier.weight(1f)) {
                onSelect("QR")
            }
            PaymentTab("Tiền mặt", selected == "CASH", Modifier.weight(1f)) {
                onSelect("CASH")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedContent(targetState = selected, label = "") { target ->
            if (target == "QR") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "QUÉT MÃ THANH TOÁN VIETQR",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(16.dp)) // QR code background should stay white for readability
                                .padding(12.dp)
                        ) {
                            val shortName =
                                name.split(" ").lastOrNull()?.uppercase() ?: "KHACH"
                            val qrUrl =
                                "https://img.vietqr.io/image/vcb-7899883653-compact2.jpg?amount=$amount&addInfo=DATTOUR%20$shortName&accountName=WIND%20Travel"
                            AsyncImage(
                                model = qrUrl,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(220.dp),
                                placeholder = null
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Ngân hàng",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Vietcombank",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Nội dung chuyển",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "DATTOUR ${
                                        name.split(" ").lastOrNull()?.uppercase() ?: "KHACH"
                                    }",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        if (receiptUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = receiptUri,
                                    contentDescription = "Receipt",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color.Black.copy(0.5f), CircleShape)
                                        .size(30.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (receiptUri == null) "Tải ảnh biên lai lên" else "Thay đổi ảnh biên lai",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Bạn sẽ thanh toán trực tiếp cho Hướng dẫn viên khi bắt đầu chuyến đi.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }
    }
}
