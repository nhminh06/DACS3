package com.example.dacs3.ui.screens.home

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
import androidx.compose.ui.unit.sp
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
    
    // Sử dụng allTours để luôn hiển thị tour từ CSDL, không bị lọc ở trang Home
    val allTours by viewModel.allTours.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                item { 
                    HomePaddingWrapper { 
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = {
                                // Cập nhật query nhưng không làm lọc tour ở dưới
                                viewModel.setSearchQuery(it)
                            },
                            onSearchClick = {
                                val query = searchQuery.trim().lowercase()
                                if (query.isNotEmpty()) {
                                    val articles = articleViewModel.explorerArticles.value
                                    val hasMatchingArticle = articles.any {
                                        it.tieu_de.lowercase().contains(query)
                                    }

                                    if (hasMatchingArticle) {
                                        // Chuyển sang trang bài viết và lọc theo từ khóa
                                        articleViewModel.setSearchQuery(searchQuery)
                                        onNavigate("explore")
                                    } else {
                                        // Chuyển sang trang tour và lọc theo từ khóa
                                        onNavigate("tours")
                                    }
                                }
                            }
                        ) 
                    } 
                }
                
                item { 
                    HomePaddingWrapper { 
                        QuickNavSection(
                            onScrollTo = { index ->
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                            onNavigate = onNavigate
                        )
                    } 
                }
                item { 
                    HomePaddingWrapper { 
                        CategorySection(onCategoryClick = onCategoryClick)
                    } 
                }

                // Luôn hiển thị tour từ CSDL (allTours), không dùng tour ảo và không bị lọc mất
                if (allTours.isNotEmpty()) {
                    item {
                        HomePaddingWrapper {
                            FeaturedToursSection(
                                tours = allTours, 
                                onTourClick = onTourClick,
                                onSeeAllClick = { onNavigate("tours") }
                            )
                        }
                    }
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

                item { HomePaddingWrapper { GuidesSection(viewModel) } }
                item { HomePaddingWrapper { ReviewsSection(viewModel) } }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            
            if (isLoading && allTours.isEmpty()) {
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
