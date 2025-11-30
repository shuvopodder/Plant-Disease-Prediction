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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.plantdisease_jetpackcompose.ui.theme.GradientColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val isDarkTheme = isSystemInDarkTheme()

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
        isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    onReset: () -> Unit
) {
    val backgroundGradient = if (isDarkTheme) {
        GradientColors.DarkBackgroundGradient
    } else {
        GradientColors.LightBackgroundGradient
    }

    Scaffold(
        topBar = {
            ModernHeader(isDarkTheme = isDarkTheme)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backgroundGradient))
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
                    InfoCard(isDarkTheme = isDarkTheme)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UploadOption(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Image,
                            title = "Gallery",
                            description = "Choose photo",
                            gradient = if (isDarkTheme) {
                                GradientColors.DarkPrimaryGradient
                            } else {
                                GradientColors.LightPrimaryGradient
                            },
                            isDarkTheme = isDarkTheme,
                            onClick = onGalleryClick
                        )

                        UploadOption(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CameraAlt,
                            title = "Camera",
                            description = "Take photo",
                            gradient = if (isDarkTheme) {
                                GradientColors.DarkSecondaryGradient
                            } else {
                                GradientColors.LightSecondaryGradient
                            },
                            isDarkTheme = isDarkTheme,
                            onClick = onCameraClick
                        )
                    }

                    DiseaseTypesCard(isDarkTheme = isDarkTheme)
                } else {
                    ImageAnalysisView(
                        uiState = uiState,
                        isDarkTheme = isDarkTheme,
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
private fun ModernHeader(isDarkTheme: Boolean) {
    val headerGradient = if (isDarkTheme) {
        GradientColors.DarkHeaderGradient
    } else {
        GradientColors.LightHeaderGradient
    }

    val backgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    } else {
        Color.White.copy(alpha = 0.95f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
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
                        Brush.linearGradient(headerGradient),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI-powered diagnosis",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun InfoCard(isDarkTheme: Boolean) {
    val cardColor = if (isDarkTheme) {
        Color(0xFF1E3A5F)
    } else {
        Color(0xFFDCEFFF)
    }

    val iconColor = if (isDarkTheme) {
        Color(0xFF60A5FA)
    } else {
        Color(0xFF0EA5E9)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "How it works",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload or capture a leaf photo. Our AI analyzes it and identifies diseases with confidence scores.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
    isDarkTheme: Boolean,
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
private fun DiseaseTypesCard(isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detectable Diseases",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DiseaseIcon("B", "Blight", com.example.plantdisease_jetpackcompose.ui.theme.BlightColor)
                DiseaseIcon("C", "Cob Root", com.example.plantdisease_jetpackcompose.ui.theme.CobRootColor)
                DiseaseIcon("R", "Rust", com.example.plantdisease_jetpackcompose.ui.theme.RustColor)
                DiseaseIcon("G", "Gray Spot", com.example.plantdisease_jetpackcompose.ui.theme.GraySpotColor)
                DiseaseIcon("H", "Healthy", com.example.plantdisease_jetpackcompose.ui.theme.HealthyColor)
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ImageAnalysisView(
    uiState: HomeUiState,
    isDarkTheme: Boolean,
    onErrorDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column {
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
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 4.dp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Analyzing image...",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "AI is processing your plant",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.predictionResult.isNotEmpty() && !uiState.isLoading) {
                    PredictionCard(uiState)
                }

                uiState.error?.let { error ->
                    ErrorBanner(error = error, onDismiss = onErrorDismiss)
                }
            }
        }

        if (!uiState.isLoading) {
            ActionButtons(
                isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
    onTryAnotherClick: () -> Unit,
    onNewPhotoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onTryAnotherClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Try Another",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Button(
            onClick = onNewPhotoClick,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Photo",
                color = MaterialTheme.colorScheme.onPrimary,
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
