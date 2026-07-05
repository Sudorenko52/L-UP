package com.example.shared.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Quest
import com.example.QuestStatus

@Composable
fun QuestCard(
    quest: Quest,
    onClick: () -> Unit
) {
    val progressPercent = (quest.currentValue / quest.targetValue).coerceIn(0f, 1f)
    val isCompleted = quest.status == QuestStatus.COMPLETED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.05f) else Color(0xFF1E293B).copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF334155).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (quest.targetType.lowercase()) {
                        "water" -> Icons.Default.Opacity
                        "sleep" -> Icons.Default.Bedtime
                        "reading" -> Icons.Default.Book
                        "habits" -> Icons.Default.FitnessCenter
                        "xp gained" -> Icons.Default.OfflineBolt
                        "level" -> Icons.Default.WorkspacePremium
                        "streak" -> Icons.Default.LocalFireDepartment
                        else -> Icons.Default.Assignment
                    }
                    val iconColor = when (quest.targetType.lowercase()) {
                        "water" -> Color(0xFF0288D1)
                        "sleep" -> Color(0xFF818CF8)
                        "reading" -> Color(0xFFFBBF24)
                        "habits" -> Color(0xFF34D399)
                        "xp gained" -> Color(0xFFA78BFA)
                        "level" -> Color(0xFFFBBF24)
                        "streak" -> Color(0xFFF87171)
                        else -> Color(0xFF38BDF8)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = quest.targetType,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = quest.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (quest.description.isNotEmpty()) {
                            Text(
                                text = quest.description,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "+${quest.xpReward} XP",
                        color = Color(0xFF8D6EFD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "+${quest.coinReward}",
                            color = Color(0xFFFFB300),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .background(Color(0xFF0F172A), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercent)
                            .fillMaxHeight()
                            .background(
                                if (isCompleted) Color(0xFF10B981) else Color(0xFF8D6EFD),
                                CircleShape
                            )
                    )
                }

                Text(
                    text = if (isCompleted) "Completed" else "${quest.currentValue.toInt()} / ${quest.targetValue.toInt()}",
                    color = if (isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
