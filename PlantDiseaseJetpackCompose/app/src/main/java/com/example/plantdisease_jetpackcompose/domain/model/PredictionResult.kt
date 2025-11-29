package com.example.plantdisease_jetpackcompose.domain.model

data class PredictionResult(
    val label: String,
    val confidence: Float,
    val allPredictions: Map<String, Float>
)
