package com.example.plantdisease_jetpackcompose.presentation.home

import android.graphics.Bitmap
import android.net.Uri

data class HomeUiState(
    val selectedImage: Bitmap? = null,
    val imageUri: Uri? = null,
    val predictionResult: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val allPredictions: Map<String, Float> = emptyMap()
)

