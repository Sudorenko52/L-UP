package com.example.shared.components.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CoinBadge(
    amount: Int,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val size = if (isLarge) 18.dp else 14.dp
    val fontSize = if (isLarge) 11.sp else 9.sp
    val textPadding = if (isLarge) 4.dp else 3.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(Color(0xFFFFD700), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = " $amount",
            color = Color(0xFFFFD700),
            fontSize = if (isLarge) 13.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = textPadding)
        )
    }
}
