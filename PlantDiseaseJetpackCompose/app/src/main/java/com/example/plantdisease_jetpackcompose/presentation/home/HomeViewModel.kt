package com.example.plantdisease_jetpackcompose.presentation.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantdisease_jetpackcompose.domain.repository.ImageClassifierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ImageClassifierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.ImageSelected -> handleImageSelection(event.uri)
            is HomeUiEvent.ImageCaptured -> handleImageSelection(event.uri)
            is HomeUiEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is HomeUiEvent.Reset -> resetState()
        }
    }

    private fun resetState() {
        // Clear the current image and recycle bitmap to free memory
        _uiState.value.selectedImage?.recycle()

        // Reset to initial state
        _uiState.update {
            HomeUiState(
                selectedImage = null,
                imageUri = null,
                predictionResult = "",
                isLoading = false,
                error = null,
                allPredictions = emptyMap()
            )
        }
    }

    private fun handleImageSelection(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val bitmap = decodeSampledBitmapFromUri(uri, 224, 224)
                _uiState.update { it.copy(selectedImage = bitmap, imageUri = uri) }

                classifyImage(bitmap)
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load image: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun classifyImage(bitmap: Bitmap) {
        repository.classifyImage(bitmap)
            .onSuccess { result ->
                _uiState.update {
                    it.copy(
                        predictionResult = "${result.label} (${(result.confidence * 100).toInt()}%)",
                        allPredictions = result.allPredictions,
                        isLoading = false
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Classification failed: ${error.message}"
                    )
                }
            }
    }

    private suspend fun decodeSampledBitmapFromUri(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("Failed to decode bitmap")

        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up bitmap when ViewModel is destroyed
        _uiState.value.selectedImage?.recycle()
    }
}