package com.example.dacs3.ui.components.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoryItem(title: String) {
    Card(
        modifier = Modifier
            .padding(4.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(8.dp)
        )
    }
}
