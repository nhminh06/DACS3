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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { TopHeader() }

            item {
                HomePaddingWrapper {
                    SearchBar()
                }
            }

            item {
                HomePaddingWrapper {
                    CategorySection()
                }
            }

            item {
                HomePaddingWrapper {
                    FeaturedDestinationsSection()
                }
            }

            item {
                HomePaddingWrapper {
                    CulturalArticlesSection()
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HomePaddingWrapper(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        content()
    }
}
