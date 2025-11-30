package com.example.plantdisease_jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import com.example.plantdisease_jetpackcompose.presentation.home.HomeScreen
import com.example.plantdisease_jetpackcompose.ui.theme.PlantDiseaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
//            MaterialTheme {
//                Surface {
//                    HomeScreen()
//                }
//            }
            PlantDiseaseTheme {
                HomeScreen()
            }
        }
    }
}
