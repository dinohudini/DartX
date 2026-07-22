package com.example.dartx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dartx.ui.screens.PlayersScreen
import com.example.dartx.ui.theme.DartXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DartXTheme {
                PlayersScreen()
            }
        }
    }
}
