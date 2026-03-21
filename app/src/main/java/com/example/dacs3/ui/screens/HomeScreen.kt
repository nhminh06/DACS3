package com.example.dacs3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dacs3.ui.components.*

@Composable
fun AppHomeScreen() {
    val backgroundColor = Color(0xFFF1F5F9)
    
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { AppBottomBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Ảnh Header
            item { TopHeader() }

            // 2. Thanh tìm kiếm
            item {
                HomePaddingWrapper {
                    SearchBar()
                }
            }

            // 3. Danh mục
            item {
                HomePaddingWrapper {
                    CategorySection()
                }
            }

            // 4. Địa điểm nổi bật (hiện 2 mục 1 lần)
            item {
                HomePaddingWrapper {
                    FeaturedDestinationsSection()
                }
            }

            // 5. Bài viết văn hóa
            item {
                HomePaddingWrapper {
                    CulturalArticlesSection()
                }
            }

            // 6. Người đồng hành cùng bạn (Hướng dẫn viên - hiện 2 người 1 lần)
            item {
                HomePaddingWrapper {
                    GuidesSection()
                }
            }

            // 7. Đánh giá khách hàng (Trải nghiệm thực tế)
            item {
                HomePaddingWrapper {
                    ReviewsSection()
                }
            }
            item {
                HomePaddingWrapper {
                    ContactFormSection()
                }
            }
            item {
                HomePaddingWrapper {
                    ClosingGreeting()
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun HomePaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
