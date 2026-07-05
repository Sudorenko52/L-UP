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
import com.example.features.profile.AvatarView

@Composable
fun AppBrandHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF14152A), Color(0xFF03040B))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "LevelUP Logo",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "LevelUP",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
        
        Surface(
            color = Color(0xFF11121E),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF1D1F30))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF00F0FF), CircleShape)
                )
                Text(
                    text = "GAMIFIED LIFE",
                    color = Color(0xFF00F0FF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}


@Composable
fun HomeScreenContent(
    userStats: UserStats,
    mainQuest: MainQuest,
    priorities: List<PriorityTask>,
    habits: List<Habit>,
    todayXpEarned: Int,
    todayCoinsEarned: Int,
    completedCount: Int,
    totalCount: Int,
    progressPercent: Int,
    isDeleteMode: Boolean,
    onPriorityToggle: (String) -> Unit,
    onHabitClick: (String) -> Unit,
    onDeletePriority: (String) -> Unit,
    onToggleDeleteMode: () -> Unit,
    onClaimQuest: () -> Unit,
    onAddHabitClick: () -> Unit,
    onEditMainQuestClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // App Brand Header with Logo
        AppBrandHeader()

        // 1. Header Profile & Status
        ProfileHeaderSection(userStats, onProfileClick)

        // 2. Today's Progress & Main Quest Card Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TodayProgressCard(
                percent = progressPercent,
                completed = completedCount,
                total = totalCount,
                modifier = Modifier.weight(1f)
            )
            MainQuestCard(
                quest = mainQuest,
                onClaimQuest = onClaimQuest,
                onEditClick = onEditMainQuestClick,
                modifier = Modifier.weight(1.3f)
            )
        }

        // 3. Top Priorities list
        TopPrioritiesSection(
            priorities = priorities,
            isDeleteMode = isDeleteMode,
            onToggle = onPriorityToggle,
            onDelete = onDeletePriority,
            onToggleDeleteMode = onToggleDeleteMode
        )

        // 4. Habits Section
        HabitsSection(
            habits = habits,
            onHabitClick = onHabitClick,
            onAddHabitClick = onAddHabitClick
        )

        // 5. Quick Stats Bottom Row
        Text(
            text = "QUICK STATS",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        QuickStatsSection(
            xp = todayXpEarned,
            coins = todayCoinsEarned,
            doneText = "$completedCount / $totalCount"
        )
        
        Spacer(modifier = Modifier.height(84.dp))
    }
}


@Composable
fun ProfileHeaderSection(userStats: UserStats, onProfileClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarView(
                    avatarId = userStats.avatarId,
                    size = 48.dp,
                    borderColor = Color(0xFF8D6EFD),
                    borderWidth = 1.5.dp
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Good morning,",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${userStats.name} \uD83D\uDC4B",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Open Profile",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Search & Notification icons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF161726), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF161726), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF8D6EFD), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Level stats line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Level ${userStats.level}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${userStats.xp} / ${userStats.maxXp} XP",
                    color = Color(0xFF8D6EFD),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Coins
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFFFB100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "${userStats.coins}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coins",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                    }
                }
                
                // Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Day Streak",
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "${userStats.streak}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Day Streak",
                            color = Color(0xFF9CA3AF),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // XP Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF161726), CircleShape)
        ) {
            val progressFactor = userStats.xp.toFloat() / userStats.maxXp.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFactor)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8D6EFD), Color(0xFFB593FF))
                        ),
                        CircleShape
                    )
            )
        }
    }
}


@Composable
fun TodayProgressCard(percent: Int, completed: Int, total: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(148.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TODAY'S PROGRESS",
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Circular progress
            Box(
                modifier = Modifier.size(62.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = size.minDimension
                    val strokeWidth = 5.dp.toPx()
                    val radius = (size - strokeWidth) / 2f
                    val center = Offset(size / 2f, size / 2f)

                    // Gray Track
                    drawCircle(
                        color = Color(0x1F212338),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFF00F0FF), Color(0xFF8D6EFD), Color(0xFF00F0FF))
                        ),
                        startAngle = -90f,
                        sweepAngle = (percent.toFloat() / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Text(
                    text = "$percent%",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = "$completed / $total completed",
                color = Color(0xFF9CA3AF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun TopPrioritiesSection(
    priorities: List<PriorityTask>,
    isDeleteMode: Boolean,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onToggleDeleteMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP PRIORITIES",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                
                IconButton(
                    onClick = onToggleDeleteMode,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isDeleteMode) Icons.Default.Check else Icons.Default.MoreVert,
                        contentDescription = "Manage",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Priority Rows
            priorities.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF161726))
                        .testTag("priority_item_${task.id}")
                        .clickable { onToggle(task.id) }
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored Left Edge Border
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(42.dp)
                            .background(Color(task.level.colorHex))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Priority Icon
                    val icon = when (task.iconName) {
                        "Gym" -> Icons.Default.FitnessCenter
                        "Work" -> Icons.Default.Work
                        "Study" -> Icons.Default.School
                        else -> Icons.Default.Star
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(task.level.colorHex).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = task.iconName,
                            tint = Color(task.level.colorHex),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = if (task.completed) Color(0xFF9CA3AF) else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.level.displayName,
                            color = Color(task.level.colorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Checkbox / Delete Button
                    if (isDeleteMode) {
                        IconButton(
                            onClick = { onDelete(task.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .border(
                                    width = 1.2.dp,
                                    color = if (task.completed) Color(task.level.colorHex) else Color(0xFF4B5563),
                                    shape = CircleShape
                                )
                                .background(
                                    color = if (task.completed) Color(task.level.colorHex) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.completed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.Black,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HabitsSection(
    habits: List<Habit>,
    onHabitClick: (String) -> Unit,
    onAddHabitClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HABITS",
                color = Color(0xFF9CA3AF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Edit",
                color = Color(0xFF8D6EFD),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onAddHabitClick() }
            )
        }

        // Display 5 habits perfectly side-by-side without horizontal scrolling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            habits.take(5).forEach { habit ->
                Box(modifier = Modifier.weight(1f)) {
                    HabitTile(habit = habit, onClick = { onHabitClick(habit.id) })
                }
            }
        }
    }
}


@Composable
fun HabitTile(habit: Habit, onClick: () -> Unit) {
    val categoryColor = when (habit.iconName) {
        "Water" -> Color(0xFF00F0FF)
        "Sleep" -> Color(0xFF8D6EFD)
        "Reading" -> Color(0xFFFFB100)
        "Creatine" -> Color(0xFF00E676)
        "Meditate" -> Color(0xFFE91E63)
        else -> Color(habit.colorHex)
    }

    val icon = when (habit.iconName) {
        "Water" -> Icons.Default.WaterDrop
        "Sleep" -> Icons.Default.Bedtime
        "Reading" -> Icons.Default.Book
        "Creatine" -> Icons.Default.Medication
        "Meditate" -> Icons.Default.Spa
        else -> Icons.Default.Star
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .testTag("habit_tile_${habit.id}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Habit Icon
            Icon(
                imageVector = icon,
                contentDescription = habit.title,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )

            // Habit Name
            Text(
                text = habit.title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Circular progress indicator centered with checkmark
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val size = size.minDimension
                    val strokeWidth = 2.5.dp.toPx()
                    val radius = (size - strokeWidth) / 2f
                    val center = Offset(size / 2f, size / 2f)

                    // Track
                    drawCircle(
                        color = Color(0x1F212338),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Arc progress
                    val sweep = if (habit.maxValue > 0) (habit.currentValue / habit.maxValue) else 0f
                    drawArc(
                        color = categoryColor,
                        startAngle = -90f,
                        sweepAngle = sweep * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                if (habit.completed) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color(0xFF2E7D32), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Value text in Category Color
            val displayProgressStr = when (habit.iconName) {
                "Water" -> "${habit.currentValue}L"
                "Sleep" -> "7h 20m"
                "Reading" -> "${habit.currentValue.toInt()}m"
                "Creatine" -> "Done"
                "Meditate" -> "${habit.currentValue.toInt()}m"
                else -> "${habit.currentValue}/${habit.maxValue}"
            }

            Text(
                text = displayProgressStr,
                color = categoryColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Status label e.g., "70%", "Good", "Today"
            Text(
                text = habit.subtitle,
                color = Color(0xFF9CA3AF),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun QuickStatsSection(xp: Int, coins: Int, doneText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // XP Column
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HexagonXpBadge()
                Column {
                    Text(
                        text = "$xp",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today's XP",
                        color = Color(0xFF9CA3AF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(Color(0xFF1D1F30))
            )

            // Coin Column
            Row(
                modifier = Modifier.weight(1.1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GoldCoinBadge()
                Column {
                    Text(
                        text = "$coins",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Coins Earned",
                        color = Color(0xFF9CA3AF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(Color(0xFF1D1F30))
            )

            // Check Column
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GreenCheckBadge()
                Column {
                    Text(
                        text = doneText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tasks Done",
                        color = Color(0xFF9CA3AF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


