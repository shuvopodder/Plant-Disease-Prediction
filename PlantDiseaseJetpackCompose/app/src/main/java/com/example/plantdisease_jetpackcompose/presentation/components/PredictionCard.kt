package com.example.plantdisease_jetpackcompose.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantdisease_jetpackcompose.presentation.home.HomeUiState
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
public fun PredictionCard(uiState: HomeUiState) {
    Column(modifier = Modifier.padding(20.dp)) {
        // Main Result
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diagnosis Result",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Icon(
                imageVector = if (uiState.predictionResult.contains("Healthy"))
                    Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (uiState.predictionResult.contains("Healthy"))
                    Color(0xFF10B981) else Color(0xFFF59E0B),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFD1FAE5)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = uiState.predictionResult,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF065F46)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar (you can enhance this with actual percentage)
                LinearProgressIndicator(
                    progress = 0.85f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFFBCFBD6)
                )
            }
        }

        if (uiState.allPredictions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Detailed Analysis",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(8.dp))

            uiState.allPredictions.entries.sortedByDescending { it.value }.forEach { (label, confidence) ->
                PredictionItem(label = label, confidence = confidence)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // **** Added Disclaimer Note ****
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "⚠️ Note: This is a preliminary, knowledge-based assessment. Please consult a specialist before making any final decisions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}