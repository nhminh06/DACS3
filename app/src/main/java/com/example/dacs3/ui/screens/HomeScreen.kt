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
fun HomeScreen() {
    val backgroundColor = Color(0xFFF1F5F9) 
    
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TopHeader() }

            item {
                PaddingWrapper {
                    SearchBar()
                }
            }

            item {
                PaddingWrapper {
                    CategorySection()
                }
            }

            item {
                PaddingWrapper {
                    FeaturedDestinationsSection()
                }
            }

            item {
                PaddingWrapper {
                    CulturalArticlesSection()
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
