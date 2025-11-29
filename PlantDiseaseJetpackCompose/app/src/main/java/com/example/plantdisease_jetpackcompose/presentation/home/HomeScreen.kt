package com.example.plantdisease_jetpackcompose.presentation.home

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.plantdisease_jetpackcompose.presentation.components.ErrorBanner
import com.example.plantdisease_jetpackcompose.presentation.components.PredictionCard


//optimize ui design - modern ui
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(HomeUiEvent.ImageSelected(it)) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            viewModel.onEvent(HomeUiEvent.ImageCaptured(cameraImageUri!!))
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onGalleryClick = { galleryLauncher.launch("image/*") },
        onCameraClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
        onErrorDismiss = { viewModel.onEvent(HomeUiEvent.ClearError) },
        onReset = { viewModel.onEvent(HomeUiEvent.Reset) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    onReset: () -> Unit
) {
    Scaffold(
        topBar = {
            ModernHeader()
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD0F4DE),
                            Color(0xFFA9DEF9),
                            Color(0xFFE4C1F9)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.selectedImage == null) {
                    // Info Card
                    InfoCard()

                    // Upload Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UploadOption(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Image,
                            title = "Gallery",
                            description = "Choose photo",
                            gradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                            onClick = onGalleryClick
                        )

                        UploadOption(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CameraAlt,
                            title = "Camera",
                            description = "Take photo",
                            gradient = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
                            onClick = onCameraClick
                        )
                    }

                    // Disease Types
                    DiseaseTypesCard()
                } else {
                    // Image Analysis View
                    ImageAnalysisView(
                        uiState = uiState,
                        onErrorDismiss = onErrorDismiss,
                        onGalleryClick = onGalleryClick,
                        onCameraClick = onCameraClick,
                        onReset = onReset
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF10B981), Color(0xFF0EA5E9))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Plant Disease Classifier",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI-powered diagnosis",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEFFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "How it works",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload or capture a leaf photo. Our AI analyzes it and identifies diseases with confidence scores.",
                    fontSize = 13.sp,
                    color = Color(0xFF1E40AF),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun UploadOption(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(gradient),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun DiseaseTypesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detectable Diseases",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DiseaseIcon("B", "Blight", Color(0xFFEF4444))
                DiseaseIcon("C", "Cob Root", Color(0xFFF59E0B))
                DiseaseIcon("R", "Rust", Color(0xFFF97316))
                DiseaseIcon("G", "Gray Spot", Color(0xFF8B5CF6))
                DiseaseIcon("H", "Healthy", Color(0xFF10B981))
            }
        }
    }
}

@Composable
private fun DiseaseIcon(initial: String, name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
private fun ImageAnalysisView(
    uiState: HomeUiState,
    onErrorDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
                // Image with Loading Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    uiState.selectedImage?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected plant",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = Color(0xFF10B981),
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Analyzing image...",
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "AI is processing your plant",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }
                    }
                }

                // Results Section
                if (uiState.predictionResult.isNotEmpty() && !uiState.isLoading) {
                    PredictionCard(uiState)
                }

                // Error Message
                uiState.error?.let { error ->
                    ErrorBanner(error = error, onDismiss = onErrorDismiss)
                }
            }
        }

        // Action Buttons (moved outside the image card)
        if (!uiState.isLoading) {
            ActionButtons(
                onTryAnotherClick = {
                    onReset()
                    onGalleryClick()
                },
                onNewPhotoClick = {
                    onReset()
                    onCameraClick()
                }
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onTryAnotherClick: () -> Unit,
    onNewPhotoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Analyze Another Button
        Button(
            onClick = onTryAnotherClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Try Another",
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // Take New Photo Button
        Button(
            onClick = onNewPhotoClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Photo",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

private fun createImageUri(context: android.content.Context): Uri {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, "New Picture")
        put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
    }
    return context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IllegalStateException("Failed to create image URI")
}