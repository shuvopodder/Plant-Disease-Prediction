package com.example.plantdisease_jetpackcompose.data.repository

import android.graphics.Bitmap
import com.example.plantdisease_jetpackcompose.data.ml.TFLiteClassifier
import com.example.plantdisease_jetpackcompose.domain.model.DiseaseType
import com.example.plantdisease_jetpackcompose.domain.model.PredictionResult
import com.example.plantdisease_jetpackcompose.domain.repository.ImageClassifierRepository
import javax.inject.Inject

class ImageClassifierRepositoryImpl @Inject constructor(
    private val classifier: TFLiteClassifier
) : ImageClassifierRepository {

    override suspend fun classifyImage(bitmap: Bitmap): Result<PredictionResult> {
        return try {
            classifier.initialize()
            val predictions = classifier.classify(bitmap)

            val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: 0
            val predictedLabel = DiseaseType.fromIndex(maxIndex).label
            val confidence = predictions[maxIndex]

            val allPredictions = DiseaseType.getAllLabels()
                .mapIndexed { index, label -> label to predictions[index] }
                .toMap()

            Result.success(
                PredictionResult(
                    label = predictedLabel,
                    confidence = confidence,
                    allPredictions = allPredictions
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
