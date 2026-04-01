package com.example.dacs3.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dacs3.data.repository.TourRepository
import kotlinx.coroutines.launch

@Composable
fun TestUploadScreen() {
    val scope = rememberCoroutineScope()
    val tourRepository = remember { TourRepository() }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadStatus by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Test Upload Cloudinary & Firebase", 
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            )
        }

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Chọn ảnh từ máy")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                selectedImageUri?.let { uri ->
                    scope.launch {
                        isLoading = true
                        uploadStatus = "Đang upload..."
                        // "test_tour_id" là ID mẫu để kiểm tra trong Firestore
                        val success = tourRepository.uploadTourImageAndSave(uri, "test_tour_id")
                        isLoading = false
                        uploadStatus = if (success) "Upload thành công!" else "Upload thất bại."
                    }
                }
            },
            enabled = selectedImageUri != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Bắt đầu Upload")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        Text(text = uploadStatus)
    }
}
