package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.CareerCraftApp
import com.example.ui.ResumeViewModel
import com.example.ui.theme.CareerCraftTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ResumeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareerCraftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CareerCraftApp(viewModel = viewModel)
                }
            }
        }
    }
}
