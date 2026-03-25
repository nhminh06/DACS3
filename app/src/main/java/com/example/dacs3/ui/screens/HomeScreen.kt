package com.example.dacs3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.home.*
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen(onNavigate: (String) -> Unit) {
    val backgroundColor = Color(0xFFF1F5F9)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { 
            AppBottomBar(
                currentScreen = "home",
                onNavigate = onNavigate
            ) 
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
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

            // 3. Thanh điều hướng nhanh (Quick Nav)
            item {
                HomePaddingWrapper {
                    QuickNavSection(onItemClick = { index ->
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    })
                }
            }

            // 4. Danh mục (index: 3)
            item {
                HomePaddingWrapper {
                    CategorySection()
                }
            }

            // 5. Địa điểm nổi bật (index: 4)
            item {
                HomePaddingWrapper {
                    FeaturedDestinationsSection()
                }
            }

            // 6. Bài viết văn hóa (index: 5)
            item {
                HomePaddingWrapper {
                    CulturalArticlesSection()
                }
            }

            // 7. Người đồng hành cùng bạn (index: 6)
            item {
                HomePaddingWrapper {
                    GuidesSection()
                }
            }

            // 8. Đánh giá khách hàng (index: 7)
            item {
                HomePaddingWrapper {
                    ReviewsSection()
                }
            }

            // 9. Form Liên hệ (index: 8)
            item {
                HomePaddingWrapper {
                    ContactFormSection()
                }
            }

            // 10. Lời chào kết thúc (index: 9)
            item {
                ClosingGreeting()
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
