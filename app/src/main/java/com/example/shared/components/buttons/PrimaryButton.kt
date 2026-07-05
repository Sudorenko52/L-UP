package com.example.shared.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = if (enabled) {
        listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
    } else {
        listOf(Color(0xFF374151), Color(0xFF1F2937))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(
                brush = Brush.linearGradient(colors = colors),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFF9CA3AF),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
