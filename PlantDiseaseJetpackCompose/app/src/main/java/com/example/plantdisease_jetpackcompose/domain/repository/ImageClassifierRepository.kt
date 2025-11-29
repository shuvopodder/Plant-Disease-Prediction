package com.example.plantdisease_jetpackcompose.domain.repository

import android.graphics.Bitmap
import com.example.plantdisease_jetpackcompose.domain.model.PredictionResult

interface ImageClassifierRepository {
    suspend fun classifyImage(bitmap: Bitmap): Result<PredictionResult>
}
