package com.example.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PriorityLevel

@Composable
fun PriorityChip(
    level: PriorityLevel,
    modifier: Modifier = Modifier
) {
    val priorityColor = Color(level.colorHex)
    Box(
        modifier = modifier
            .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = level.displayName,
            color = priorityColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
