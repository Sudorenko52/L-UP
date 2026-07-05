package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

@Composable
fun ProgressScreenContent(
    userStats: UserStats,
    priorities: List<PriorityTask>,
    habits: List<Habit>,
    quests: List<Quest>,
    todayXp: Int,
    todayCoins: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03040B))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. TITLE & HEADER BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Progress",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Track. Grow. Level Up.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins Pill
                Surface(
                    color = Color(0xFF11121E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1D1F30))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = String.format("%,d", userStats.coins),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coins",
                                color = Color(0xFF9CA3AF),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // XP Pill
                Surface(
                    color = Color(0xFF11121E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1D1F30))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP",
                            tint = Color(0xFF9F75FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = String.format("%,d", userStats.xp),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "XP",
                                color = Color(0xFF9CA3AF),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Bell Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF11121E), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .background(Color(0xFF9F75FF), CircleShape)
                    )
                }
            }
        }

        // --- 2. LEVEL & XP PROGRESS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Hexagon Badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.width(84.dp)
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val size = size.minDimension
                            val radius = size / 2f
                            val centerX = size / 2f
                            val centerY = size / 2f
                            
                            val path = Path().apply {
                                for (i in 0 until 6) {
                                    val angle = (i * 60 - 30) * Math.PI / 180f
                                    val x = centerX + radius * kotlin.math.cos(angle).toFloat()
                                    val y = centerY + radius * kotlin.math.sin(angle).toFloat()
                                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                                }
                                close()
                            }
                            // Fill
                            drawPath(
                                path = path,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF2C1E5C), Color(0xFF130D2E))
                                )
                            )
                            // Outline
                            drawPath(
                                path = path,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF9F75FF), Color(0xFF5E3BE1))
                                ),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                        Text(
                            text = userStats.level.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                    Text(
                        text = "Level ${userStats.level}",
                        color = Color(0xFF9F75FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Keep going!",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // XP details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP Progress",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${userStats.xp} / ${userStats.maxXp} XP",
                            color = Color(0xFF9F75FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Rounded Progress Bar
                    val progressFactor = (userStats.xp.toFloat() / userStats.maxXp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF1E2030))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFactor)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                                    )
                                )
                        )
                    }

                    Text(
                        text = "${userStats.maxXp - userStats.xp} XP to Level ${userStats.level + 1}",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 3. THREE CORE STATS CARDS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val cumulativeXp = (userStats.level - 1) * 1000 + userStats.xp
            
            // Coins card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = String.format("%,d", userStats.coins),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Coins",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Streak card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Streak",
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "${userStats.streak}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Day Streak",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Total XP card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Total XP",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = String.format("%,d", cumulativeXp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Total XP",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- 4. STATISTICS BLOCK ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statistics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Surface(
                        color = Color(0xFF1E2030),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "This Week",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Grid 2x2 of statistic tiles
                val completedTasks = priorities.count { it.completed }
                val completedHabits = habits.count { it.completed }
                val completedQuests = quests.count { it.status == QuestStatus.COMPLETED }
                
                val totalMins = completedTasks * 25
                val hours = totalMins / 60
                val mins = totalMins % 60
                val focusTimeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tile 1: Tasks Done
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF090A11), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF064E3B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "$completedTasks",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Tasks Done",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+12% vs last week",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Tile 2: Habits Done
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF090A11), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF1E3A8A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "$completedHabits",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Habits Done",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+20% vs last week",
                            color = Color(0xFF3B82F6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tile 3: Quests Done
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF090A11), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF4C1D95), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "$completedQuests",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Quests Done",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+50% vs last week",
                            color = Color(0xFF8B5CF6),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Tile 4: Focus Time
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF090A11), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFF7C2D12), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = focusTimeText,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Focus Time",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+15% vs last week",
                            color = Color(0xFFF97316),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- 5. ACHIEVEMENTS SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achievements",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "View All",
                        color = Color(0xFF9F75FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { }
                    )
                }

                // Dynamic achievements evaluator
                val totalTasksCompleted = priorities.count { it.completed }
                val hasFirstTask = totalTasksCompleted >= 1
                val hasTenTasks = totalTasksCompleted >= 10
                val hasSevenStreak = userStats.streak >= 7

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Achievement 1: First Steps
                    AchievementCard(
                        title = "First Steps",
                        description = "Complete your first task",
                        date = "12 May 2024",
                        isUnlocked = hasFirstTask,
                        color = Color(0xFF10B981)
                    )

                    // Achievement 2: 10 Tasks
                    AchievementCard(
                        title = "10 Tasks",
                        description = "Complete 10 tasks",
                        date = "15 May 2024",
                        isUnlocked = hasTenTasks,
                        color = Color(0xFF3B82F6)
                    )

                    // Achievement 3: 7 Day Streak
                    AchievementCard(
                        title = "7 Day Streak",
                        description = "Maintain a streak for 7 days",
                        date = "20 May 2024",
                        isUnlocked = hasSevenStreak,
                        color = Color(0xFF8B5CF6)
                    )
                }
            }
        }

        // --- 6. STREAK CALENDAR CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Streak Calendar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "This Week",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    days.forEachIndexed { index, day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = day,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            // Render Mon-Fri as checked, Sat as hollow, Sun as fiery
                            when {
                                index < 5 -> { // Mon-Fri
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFF064E3B), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                index == 5 -> { // Sat
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .border(1.dp, Color(0xFF1D1F30), CircleShape)
                                            .background(Color.Transparent)
                                    )
                                }
                                else -> { // Sun (Fire!)
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFF7C2D12), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Whatshot,
                                            contentDescription = null,
                                            tint = Color(0xFFF97316),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 7. RECENT ACTIVITY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "View All",
                        color = Color(0xFF9F75FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { }
                    )
                }

                // Construct a dynamic log of completed items
                val completedTasksList = priorities.filter { it.completed }.take(2)
                val completedHabitsList = habits.filter { it.completed }.take(1)

                if (completedTasksList.isEmpty() && completedHabitsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity yet",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        completedTasksList.forEach { task ->
                            ActivityRowItem(
                                title = "Completed Task",
                                description = task.title,
                                rewardText = "+${task.xpReward} XP",
                                timeText = "2h ago",
                                iconColor = Color(0xFF10B981)
                            )
                        }
                        completedHabitsList.forEach { habit ->
                            ActivityRowItem(
                                title = "Completed Habit",
                                description = habit.title,
                                rewardText = "+${habit.xpReward} XP",
                                timeText = "4h ago",
                                iconColor = Color(0xFF3B82F6)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}


@Composable
fun AchievementCard(
    title: String,
    description: String,
    date: String,
    isUnlocked: Boolean,
    color: Color
) {
    val opacity = if (isUnlocked) 1.0f else 0.35f
    
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(110.dp)
            .graphicsLayer { alpha = opacity },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090A11)),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) color.copy(alpha = 0.5f) else Color(0xFF1D1F30)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hexagonal badge illustration
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = size.minDimension
                    val radius = size / 2f
                    val centerX = size / 2f
                    val centerY = size / 2f
                    
                    val path = Path().apply {
                        for (i in 0 until 6) {
                            val angle = (i * 60 - 30) * Math.PI / 180f
                            val x = centerX + radius * kotlin.math.cos(angle).toFloat()
                            val y = centerY + radius * kotlin.math.sin(angle).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.15f)
                    )
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isUnlocked) date else "Locked",
                    color = if (isUnlocked) color else Color(0xFF6B7280),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun ActivityRowItem(
    title: String,
    description: String,
    rewardText: String,
    timeText: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = description,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = rewardText,
                color = iconColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = timeText,
                color = Color(0xFF6B7280),
                fontSize = 11.sp
            )
        }
    }
}



