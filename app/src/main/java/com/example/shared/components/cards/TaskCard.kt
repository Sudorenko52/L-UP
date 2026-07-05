package com.example.shared.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.PriorityTask

@Composable
fun TaskCard(
    task: PriorityTask,
    onClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    val priorityColor = Color(task.level.colorHex)
    val displayPercent = (task.progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D17)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Priority Indicator Vertical Accent Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(108.dp)
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Category Icon Container
                        val icon = when (task.iconName) {
                            "Gym" -> Icons.Default.FitnessCenter
                            "Book" -> Icons.Default.Book
                            "Clean" -> Icons.Default.Computer
                            "Code" -> Icons.Default.Code
                            "Water" -> Icons.Default.Opacity
                            "Work" -> Icons.Default.Work
                            "Study" -> Icons.Default.School
                            else -> Icons.Default.Star
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(priorityColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = task.iconName,
                                tint = priorityColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = task.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Priority Label Badge
                                Box(
                                    modifier = Modifier
                                        .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.level.displayName,
                                        color = priorityColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Time Info
                                if (task.time.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Time",
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = task.time,
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // XP and Coins Reward Display (on the top right of the card)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "+${task.xpReward} XP",
                            color = Color(0xFF8D6EFD),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Gold Coin badge
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color(0xFFFFD700), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "+${task.coinReward}",
                                color = Color(0xFFFBC02D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Progress Bar Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(Color(0xFF161726), CircleShape)
                            .clickable { onProgressClick() }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(task.progress)
                                .fillMaxHeight()
                                .background(priorityColor, CircleShape)
                        )
                    }
                    Text(
                        text = "$displayPercent%",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
