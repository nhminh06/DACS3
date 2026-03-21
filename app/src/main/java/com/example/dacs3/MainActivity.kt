package com.example.dacs3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dacs3.ui.screens.HomeScreen
import com.example.dacs3.ui.theme.DACS3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kích hoạt chế độ tràn viền
        enableEdgeToEdge()

        setContent {
            DACS3Theme {
                HomeScreen()
            }
        }
    }
}
