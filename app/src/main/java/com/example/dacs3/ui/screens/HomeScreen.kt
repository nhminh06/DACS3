package com.example.dacs3.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.repository.ArticleEntity
import com.example.dacs3.ui.components.AppBottomBar
import com.example.dacs3.ui.components.home.*
import com.example.dacs3.ui.viewmodel.ArticleViewModel
import com.example.dacs3.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppHomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: MainViewModel,
    articleViewModel: ArticleViewModel,
    onTourClick: (Tour) -> Unit,
    onArticleClick: (ArticleEntity) -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    val backgroundColor = Color(0xFFF1F5F9)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val tours by viewModel.tours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { 
            AppBottomBar(
                currentScreen = "home",
                onNavigate = onNavigate
            ) 
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { TopHeader(onProfileClick = { onNavigate("profile") }) }
                item { HomePaddingWrapper { SearchBar() } }
                item { 
                    HomePaddingWrapper { 
                        QuickNavSection(onItemClick = { index ->
                            coroutineScope.launch {
                                // Cuộn đến các vị trí tương ứng trong LazyColumn
                                // 4: Địa điểm/Tour, 5: Văn hóa, 6: Hướng dẫn, 7: Đánh giá, 8: Liên hệ
                                listState.animateScrollToItem(index)
                            }
                        }) 
                    } 
                }
                item { 
                    HomePaddingWrapper { 
                        CategorySection(onCategoryClick = onCategoryClick)
                    } 
                }

                if (tours.isNotEmpty()) {
                    item {
                        HomePaddingWrapper {
                            FeaturedToursSection(
                                tours = tours, 
                                onTourClick = onTourClick,
                                onSeeAllClick = { onNavigate("tours") }
                            )
                        }
                    }
                } else if (!isLoading) {
                    item { HomePaddingWrapper { FeaturedDestinationsSection() } }
                }

                item {
                    HomePaddingWrapper {
                        CulturalArticlesSection(
                            articleViewModel = articleViewModel,
                            onSeeAllClick = { onNavigate("explore") },
                            onArticleClick = onArticleClick
                        )
                    }
                }

                item { HomePaddingWrapper { GuidesSection() } }
                item { HomePaddingWrapper { ReviewsSection() } }
                item { HomePaddingWrapper { ContactFormSection() } }
                item { ClosingGreeting() }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HomePaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
