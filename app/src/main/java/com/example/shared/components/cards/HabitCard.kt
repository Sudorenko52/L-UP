package com.example.shared.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Habit

@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    val categoryColor = when (habit.iconName) {
        "Water" -> Color(0xFF00F0FF)
        "Sleep" -> Color(0xFF8D6EFD)
        "Reading" -> Color(0xFFFFB100)
        "Creatine" -> Color(0xFF00E676)
        "Meditate" -> Color(0xFFE91E63)
        "Exercise" -> Color(0xFFFF3D00)
        "Vitamins" -> Color(0xFF00E676)
        else -> Color(habit.colorHex)
    }

    val icon = when (habit.iconName) {
        "Water" -> Icons.Default.WaterDrop
        "Sleep" -> Icons.Default.Bedtime
        "Reading" -> Icons.Default.Book
        "Creatine" -> Icons.Default.Medication
        "Meditate" -> Icons.Default.Spa
        "Exercise" -> Icons.Default.FitnessCenter
        "Vitamins" -> Icons.Default.Medication
        else -> Icons.Default.Star
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_row_${habit.id}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder controls & Drag indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (!isFirst) Color.White else Color(0xFF374151),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Reorder",
                    tint = Color(0xFF4B5563),
                    modifier = Modifier.size(16.dp)
                )

                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (!isLast) Color.White else Color(0xFF374151),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Category Icon in a circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(categoryColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = habit.title,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info & Progress bar
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = habit.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val progressText = if (habit.maxValue > 0) {
                    "${habit.currentValue} / ${habit.maxValue} ${habit.unit}".trim()
                } else {
                    habit.subtitle
                }
                
                val percentInt = if (habit.maxValue > 0) ((habit.currentValue / habit.maxValue) * 100).toInt() else 0
                val rightProgressText = if (habit.completed) "Completed" else "$percentInt%"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressText,
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                    Text(
                        text = rightProgressText,
                        color = categoryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                val progressValue = if (habit.maxValue > 0) (habit.currentValue / habit.maxValue).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = progressValue,
                    color = categoryColor,
                    trackColor = Color(0xFF1D1F30),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Rewards & Tags info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "+${habit.xpReward} XP",
                            color = Color(0xFF8D6EFD),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            color = Color(0xFF4B5563),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+${habit.coinReward}",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (habit.isPenaltyEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Penalty ON",
                                color = Color(0xFFE53935),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0xFFE53935).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (habit.tags.isNotEmpty()) {
                        Text(
                            text = habit.tags.first(),
                            color = Color(0xFF9CA3AF),
                            fontSize = 9.sp,
                            modifier = Modifier
                                .background(Color(0xFF1D1F30), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Completed Switch Toggle
            Switch(
                checked = habit.completed,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = categoryColor,
                    uncheckedThumbColor = Color(0xFF9CA3AF),
                    uncheckedTrackColor = Color(0xFF1D1F30)
                ),
                modifier = Modifier.testTag("habit_switch_${habit.id}")
            )
        }
    }
}
