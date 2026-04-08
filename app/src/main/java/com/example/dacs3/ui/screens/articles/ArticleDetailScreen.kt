package com.example.dacs3.ui.screens.articles

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dacs3.data.model.Comment
import com.example.dacs3.data.model.Report
import com.example.dacs3.data.model.ReportType
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.UserViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: ArticleEntity,
    onBack: () -> Unit,
    onNavigateToTour: () -> Unit,
    userViewModel: UserViewModel,
    articleViewModel: ArticleViewModel
) {
    val scrollState = rememberScrollState()
    val primaryColor = Color(0xFF2563EB)
    
    // Report States
    var showReportDialog by remember { mutableStateOf(false) }
    var reportTargetComment by remember { mutableStateOf<Comment?>(null) }
    var reportReason by remember { mutableStateOf("") }
    
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    
    val comments by articleViewModel.comments.collectAsState()
    val isCommenting by articleViewModel.isCommenting.collectAsState()
    val currentUser by userViewModel.currentUser
    val isLoggedIn = userViewModel.isLoggedIn()
    val context = LocalContext.current

    LaunchedEffect(article.id) {
        articleViewModel.fetchComments(article.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.tieu_de, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { 
                        if (isLoggedIn) {
                            reportTargetComment = null
                            reportReason = ""
                            showReportDialog = true 
                        } else {
                            Toast.makeText(context, "Bạn cần đăng nhập để báo cáo", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Outlined.Flag, contentDescription = "Report Article")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = onNavigateToTour,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Xem chuyến đi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header Image
                val firstImage = article.sections.firstOrNull { it["hinh_anh"] != null }?.get("hinh_anh")
                Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(firstImage)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )
                    Text(
                        text = article.tieu_de,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 34.sp
                    )
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    // Dynamic Content Sections
                    article.sections.forEach { section ->
                        val tieuDe = section["tieu_de"]
                        val noiDung = section["noi_dung"]
                        val hinhAnh = section["hinh_anh"]

                        if (!tieuDe.isNullOrEmpty() || !noiDung.isNullOrEmpty()) {
                            ContentSection(
                                title = tieuDe ?: "",
                                description = noiDung ?: "",
                                imageUrl = hinhAnh
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Comment Section
                    CommentSection(
                        isLoggedIn = isLoggedIn,
                        onReportClick = { comment -> 
                            if (isLoggedIn) {
                                reportTargetComment = comment
                                reportReason = ""
                                showReportDialog = true
                            } else {
                                Toast.makeText(context, "Bạn cần đăng nhập để báo cáo", Toast.LENGTH_SHORT).show()
                            }
                        },
                        comments = comments,
                        currentUserId = currentUser?.id ?: "",
                        onPostComment = { content ->
                            val newComment = Comment(
                                articleId = article.id,
                                userId = currentUser?.id ?: "",
                                userName = currentUser?.name ?: "Người dùng",
                                userAvatar = currentUser?.avatar ?: "",
                                content = content,
                                createdAt = Timestamp.now()
                            )
                            articleViewModel.postComment(newComment) { success ->
                                if (success) {
                                    Toast.makeText(context, "Đã gửi bình luận", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDeleteComment = { comment ->
                            commentToDelete = comment
                        },
                        onLikeComment = { comment, isLiked ->
                            if (isLoggedIn) {
                                articleViewModel.toggleLikeComment(article.id, comment.id, currentUser?.id ?: "")
                            } else {
                                Toast.makeText(context, "Bạn cần đăng nhập để thả tim", Toast.LENGTH_SHORT).show()
                            }
                        },
                        isCommenting = isCommenting,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(if (reportTargetComment == null) "Báo cáo bài viết" else "Báo cáo bình luận") },
            text = {
                Column {
                    Text("Lý do báo cáo:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Nhập lý do tại đây...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            val report = if (reportTargetComment == null) {
                                Report(
                                    type = ReportType.ARTICLE,
                                    reporterId = currentUser?.id ?: "",
                                    reporterName = currentUser?.name ?: "Ẩn danh",
                                    articleId = article.id,
                                    articleTitle = article.tieu_de,
                                    reason = reportReason
                                )
                            } else {
                                Report(
                                    type = ReportType.COMMENT,
                                    reporterId = currentUser?.id ?: "",
                                    reporterName = currentUser?.name ?: "Ẩn danh",
                                    commentId = reportTargetComment?.id,
                                    reportedUserId = reportTargetComment?.userId,
                                    reportedUserName = reportTargetComment?.userName,
                                    commentContent = reportTargetComment?.content,
                                    reason = reportReason
                                )
                            }
                            
                            articleViewModel.sendReport(report) { success ->
                                if (success) {
                                    Toast.makeText(context, "Đã gửi báo cáo thành công", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gửi báo cáo thất bại", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showReportDialog = false
                        } else {
                            Toast.makeText(context, "Vui lòng nhập lý do", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Gửi báo cáo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (commentToDelete != null) {
        AlertDialog(
            onDismissRequest = { commentToDelete = null },
            title = { Text("Xóa bình luận") },
            text = { Text("Bạn có chắc chắn muốn xóa bình luận này không?") },
            confirmButton = {
                TextButton(onClick = {
                    commentToDelete?.let {
                        articleViewModel.deleteComment(article.id, it.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Đã xóa bình luận", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Lỗi khi xóa bình luận", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    commentToDelete = null
                }) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { commentToDelete = null }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ContentSection(title: String, description: String, imageUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (description.isNotEmpty()) {
            Text(
                text = description,
                fontSize = 15.sp,
                color = Color(0xFF475569),
                lineHeight = 24.sp
            )
        }
        
        if (!imageUrl.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
    }
}

@Composable
fun CommentSection(
    isLoggedIn: Boolean,
    onReportClick: (Comment) -> Unit,
    comments: List<Comment>,
    currentUserId: String,
    onPostComment: (String) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onLikeComment: (Comment, Boolean) -> Unit,
    isCommenting: Boolean,
    primaryColor: Color
) {
    var commentText by remember { mutableStateOf("") }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bình luận", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "(${comments.size})",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            enabled = isLoggedIn && !isCommenting,
            placeholder = { Text(if (isLoggedIn) "Chia sẻ cảm nghĩ của bạn..." else "Bạn cần đăng nhập để bình luận", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Gray,
                cursorColor = primaryColor
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                if (commentText.isNotBlank()) {
                    onPostComment(commentText)
                    commentText = ""
                }
            },
            enabled = isLoggedIn && !isCommenting && commentText.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            if (isCommenting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Gửi bình luận", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (comments.isEmpty()) {
            Text(
                "Chưa có bình luận nào. Hãy là người đầu tiên chia sẻ cảm nghĩ!",
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
        } else {
            comments.forEachIndexed { index, comment ->
                CommentItem(
                    comment = comment,
                    currentUserId = currentUserId,
                    onReportClick = { onReportClick(comment) },
                    onDeleteClick = { onDeleteComment(comment) },
                    onLikeClick = { isLiked -> onLikeComment(comment, isLiked) }
                )
                if (index < comments.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.2f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onReportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateStr = comment.createdAt?.let { sdf.format(it.toDate()) } ?: ""
    val isLikedByMe = comment.likedBy.contains(currentUserId)
    val isOwnComment = comment.userId == currentUserId

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF1F5F9))) {
                if (comment.userAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(comment.userAvatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp).fillMaxSize(), tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(comment.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Text(dateStr, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            
            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }) { 
                    Icon(Icons.Default.MoreVert, null, tint = Color.Gray) 
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isOwnComment) {
                        DropdownMenuItem(
                            text = { Text("Xóa bình luận", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Báo cáo") },
                            onClick = {
                                showMenu = false
                                onReportClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Report, null) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = comment.content,
            fontSize = 14.sp,
            color = Color(0xFF475569),
            lineHeight = 22.sp
        )
        
        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onLikeClick(isLikedByMe) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLikedByMe) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = comment.likes.toString(),
                fontSize = 12.sp, 
                color = if (isLikedByMe) Color.Red else Color.Gray
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text("Trả lời", fontSize = 13.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
        }
    }
}
