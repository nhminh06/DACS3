package com.example.dacs3.ui.components.articles

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dacs3.data.model.ArticleCategory

@Composable
fun ArticleCategoryTabs(
    selectedCategory: ArticleCategory,
    onCategorySelected: (ArticleCategory) -> Unit
) {
    val categories = listOf(
        Triple(ArticleCategory.CULTURE, "Văn hóa", "Nét đẹp truyền thống"),
        Triple(ArticleCategory.CRAFT_VILLAGE, "Làng nghề", "Bàn tay khéo léo"),
        Triple(ArticleCategory.CUISINE, "Ẩm thực", "Hương vị quê hương")
    )

    val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 20.dp,
        divider = {},
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        categories.forEach { (category, title, subtitle) ->
            val isSelected = selectedCategory == category
            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
