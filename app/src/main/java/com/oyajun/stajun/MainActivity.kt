package com.oyajun.stajun

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.oyajun.stajun.StaJunApp
import com.oyajun.stajun.ui.theme.StaJunTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StaJunTheme {
                StaJunApp()
            }
        }
    }
}