package com.example.plantdisease_jetpackcompose.presentation.home

import android.net.Uri

sealed class HomeUiEvent {
    data class ImageSelected(val uri: Uri) : HomeUiEvent()
    data class ImageCaptured(val uri: Uri) : HomeUiEvent()
    object ClearError : HomeUiEvent()
}
