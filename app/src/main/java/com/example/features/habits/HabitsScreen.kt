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
fun HabitsManagerScreen(
    habits: List<Habit>,
    onBack: () -> Unit,
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    onToggleHabit: (String, Boolean) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit
) {
    val completedCount = habits.count { it.completed }
    val totalCount = habits.size
    val dailyScore = if (totalCount > 0) ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(horizontal = 16.dp)
    ) {
        // Top row: Back button, Title, Add (+) button
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Habits",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage your habits",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }

            // Circular Add Button
            IconButton(
                onClick = onAddHabit,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Habit",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Habit Score card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular progress (78% style)
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 5.dp.toPx()
                        val sizeMin = size.minDimension
                        val radius = (sizeMin - strokeWidth) / 2f
                        val center = Offset(sizeMin / 2f, sizeMin / 2f)

                        // Track
                        drawCircle(
                            color = Color(0x1F212338),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth)
                        )

                        // Progress
                        drawArc(
                            color = Color(0xFF8D6EFD),
                            startAngle = -90f,
                            sweepAngle = (dailyScore.toFloat() / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$dailyScore%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Daily Habit Score",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (dailyScore >= 70) "Great progress! Keep building your routine." else "Consistency is key. Keep taking small steps!",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "No habits",
                        tint = Color(0xFF1D1F30),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Create your first habit",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(habits) { habit ->
                    HabitRowItem(
                        habit = habit,
                        onClick = { onEditHabit(habit.id) },
                        onToggle = { onToggleHabit(habit.id, it) },
                        onMoveUp = { onMoveUp(habit.id) },
                        onMoveDown = { onMoveDown(habit.id) },
                        isFirst = habits.first().id == habit.id,
                        isLast = habits.last().id == habit.id
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Gradient + Add Habit Button at bottom
                    Button(
                        onClick = onAddHabit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_habit_bottom_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Habit",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Composable
fun HabitRowItem(
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


@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onConfirm: (
        title: String,
        icon: String,
        color: Long,
        goal: Float,
        unit: String,
        step: Float,
        desc: String,
        repeat: String,
        xp: Int,
        coins: Int,
        penalty: Boolean,
        tags: List<String>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Custom") }
    var colorHex by remember { mutableStateOf(0xFF8D6EFD) }
    var goal by remember { mutableStateOf(1f) }
    var unit by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1f) }
    var description by remember { mutableStateOf("") }
    var repeatPattern by remember { mutableStateOf("Every Day") }
    var xpReward by remember { mutableStateOf(10) }
    var coinReward by remember { mutableStateOf(2) }
    var isPenaltyEnabled by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf<String?>(null) }

    // Presets mapping
    val presets = listOf(
        PresetOption("Custom", "Custom", Icons.Default.Add, 0xFF8D6EFD, 1f, "", 1f, 10, 2),
        PresetOption("Water", "Water", Icons.Default.WaterDrop, 0xFF00F0FF, 2.0f, "L", 0.2f, 3, 1),
        PresetOption("Sleep", "Sleep", Icons.Default.Bedtime, 0xFF8D6EFD, 8.0f, "h", 1.0f, 5, 1),
        PresetOption("Exercise", "Exercise", Icons.Default.FitnessCenter, 0xFFFF3D00, 30f, "min", 5.0f, 10, 3),
        PresetOption("Reading", "Reading", Icons.Default.Book, 0xFFFFB100, 30f, "min", 5.0f, 5, 1),
        PresetOption("Meditate", "Meditate", Icons.Default.Spa, 0xFFE91E63, 15f, "min", 5.0f, 5, 1),
        PresetOption("Vitamins", "Vitamins", Icons.Default.Medication, 0xFF00E676, 1f, "", 1.0f, 2, 1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back arrow & title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Habit",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Icons Grid
        Text(
            text = "PRESET ICON",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Grid of presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = selectedIcon == preset.iconName
                    Card(
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                selectedIcon = preset.iconName
                                title = if (preset.name == "Custom") "" else preset.name
                                colorHex = preset.colorHex
                                goal = preset.goal
                                unit = preset.unit
                                step = preset.step
                                xpReward = preset.xp
                                coinReward = preset.coins
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(preset.colorHex).copy(alpha = 0.15f) else Color(0xFF12131F)
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(preset.colorHex) else Color(0xFF1D1F30)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = preset.name,
                                tint = if (isSelected) Color(preset.colorHex) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.name,
                                color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Habit Name TextField
        Text(
            text = "HABIT NAME",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Enter habit name...", color = Color(0xFF4B5563)) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12131F),
                unfocusedContainerColor = Color(0xFF12131F),
                focusedBorderColor = Color(colorHex),
                unfocusedBorderColor = Color(0xFF1D1F30)
            ),
            modifier = Modifier.fillMaxWidth().testTag("add_habit_name_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Optional Description TextField
        Text(
            text = "DESCRIPTION (OPTIONAL)",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("What is this habit for?", color = Color(0xFF4B5563)) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12131F),
                unfocusedContainerColor = Color(0xFF12131F),
                focusedBorderColor = Color(colorHex),
                unfocusedBorderColor = Color(0xFF1D1F30)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Goal counter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Goal",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target amount to complete",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { goal = (goal - step).coerceAtLeast(0.1f) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format("%.1f", goal)} $unit".trim(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { goal += step },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Unit selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unit (e.g. L, h, min, times)",
                color = Color(0xFF9CA3AF),
                fontSize = 12.sp
            )
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                placeholder = { Text("times", color = Color(0xFF4B5563)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF12131F),
                    unfocusedContainerColor = Color(0xFF12131F),
                    focusedBorderColor = Color(colorHex),
                    unfocusedBorderColor = Color(0xFF1D1F30)
                ),
                modifier = Modifier.width(100.dp).height(44.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Repeat / Schedule Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(colorHex), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Repeat",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selected schedule",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }

            // Simple Dropdown/Selection row of patterns
            val patterns = listOf("Every Day", "Mon, Wed, Fri", "Weekends", "Weekdays")
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12131F)),
                    border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(repeatPattern, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF12131F)).border(BorderStroke(1.dp, Color(0xFF1D1F30)))
                ) {
                    patterns.forEach { pattern ->
                        DropdownMenuItem(
                            text = { Text(pattern, color = Color.White) },
                            onClick = {
                                repeatPattern = pattern
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Coins penalty toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Coins penalty if missed",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lose coins if you miss this habit",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = isPenaltyEnabled,
                onCheckedChange = { isPenaltyEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(colorHex),
                    uncheckedThumbColor = Color(0xFF9CA3AF),
                    uncheckedTrackColor = Color(0xFF1D1F30)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rewards customization and config (FR-006)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Rewards & Penalty Settings",
                    color = Color(colorHex),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("XP Reward:", color = Color.White, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { xpReward = (xpReward - 1).coerceAtLeast(0) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text("$xpReward XP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { xpReward += 1 }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Coins Reward:", color = Color.White, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { coinReward = (coinReward - 1).coerceAtLeast(0) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text("$coinReward Coins", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { coinReward += 1 }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tags Input (FR-009)
        Text(
            text = "TAGS",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            placeholder = { Text("e.g. Health, Daily, Routine", color = Color(0xFF4B5563)) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12131F),
                unfocusedContainerColor = Color(0xFF12131F),
                focusedBorderColor = Color(colorHex),
                unfocusedBorderColor = Color(0xFF1D1F30)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showError != null) {
            Text(
                text = showError!!,
                color = Color(0xFFE53935),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Create Habit Button
        Button(
            onClick = {
                val validatedName = title.trim()
                if (validatedName.isEmpty()) {
                    showError = "Name is required (1-100 characters)."
                } else if (validatedName.length > 100) {
                    showError = "Name is too long (maximum 100 characters)."
                } else {
                    val tagList = tagInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    onConfirm(
                        validatedName,
                        selectedIcon,
                        colorHex,
                        goal,
                        unit,
                        step,
                        description,
                        repeatPattern,
                        xpReward,
                        coinReward,
                        isPenaltyEnabled,
                        tagList
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("create_habit_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create Habit",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
fun EditHabitScreen(
    habit: Habit,
    onBack: () -> Unit,
    onConfirm: (
        title: String,
        icon: String,
        color: Long,
        currentValue: Float,
        goal: Float,
        unit: String,
        step: Float,
        desc: String,
        repeat: String,
        xp: Int,
        coins: Int,
        penalty: Boolean,
        tags: List<String>,
        completed: Boolean
    ) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(habit.title) }
    var selectedIcon by remember { mutableStateOf(habit.iconName) }
    var colorHex by remember { mutableStateOf(habit.colorHex) }
    var goal by remember { mutableStateOf(habit.maxValue) }
    var currentValue by remember { mutableStateOf(habit.currentValue) }
    var unit by remember { mutableStateOf(habit.unit) }
    var step by remember { mutableStateOf(habit.incrementStep) }
    var description by remember { mutableStateOf(habit.description) }
    var repeatPattern by remember { mutableStateOf(habit.repeat) }
    var xpReward by remember { mutableStateOf(habit.xpReward) }
    var coinReward by remember { mutableStateOf(habit.coinReward) }
    var isPenaltyEnabled by remember { mutableStateOf(habit.isPenaltyEnabled) }
    var tagInput by remember { mutableStateOf(habit.tags.joinToString(", ")) }
    var completedState by remember { mutableStateOf(habit.completed) }

    var showError by remember { mutableStateOf<String?>(null) }

    val categoryColor = when (selectedIcon) {
        "Water" -> Color(0xFF00F0FF)
        "Sleep" -> Color(0xFF8D6EFD)
        "Reading" -> Color(0xFFFFB100)
        "Creatine" -> Color(0xFF00E676)
        "Meditate" -> Color(0xFFE91E63)
        "Exercise" -> Color(0xFFFF3D00)
        "Vitamins" -> Color(0xFF00E676)
        else -> Color(colorHex)
    }

    val icon = when (selectedIcon) {
        "Water" -> Icons.Default.WaterDrop
        "Sleep" -> Icons.Default.Bedtime
        "Reading" -> Icons.Default.Book
        "Creatine" -> Icons.Default.Medication
        "Meditate" -> Icons.Default.Spa
        "Exercise" -> Icons.Default.FitnessCenter
        "Vitamins" -> Icons.Default.Medication
        else -> Icons.Default.Star
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back, Title, and Delete button on top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Habit",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.testTag("edit_habit_delete_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", color = Color(0xFFE53935), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Category Icon & Habit Name TextField
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge with small Edit Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(categoryColor.copy(alpha = 0.15f), CircleShape)
                    .border(BorderStroke(1.dp, categoryColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(28.dp)
                )
                // Small overlay Edit pencil icon
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF12131F), CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Habit name", color = Color(0xFF4B5563)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF12131F),
                    unfocusedContainerColor = Color(0xFF12131F),
                    focusedBorderColor = categoryColor,
                    unfocusedBorderColor = Color(0xFF1D1F30)
                ),
                modifier = Modifier.weight(1f).testTag("edit_habit_name_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Goal selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Goal",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Target amount to complete",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { goal = (goal - step).coerceAtLeast(0.1f) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Goal", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format("%.1f", goal)} $unit".trim(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { goal += step },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Goal", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Amount selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Amount",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your current progress today",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        currentValue = (currentValue - step).coerceAtLeast(0f)
                        completedState = currentValue >= goal
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease Value", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${String.format("%.1f", currentValue)} $unit".trim(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        currentValue = (currentValue + step).coerceAtMost(goal)
                        completedState = currentValue >= goal
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF1D1F30), CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase Value", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Repeat / Schedule Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Repeat",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selected schedule",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp
                    )
                }
            }

            val patterns = listOf("Every Day", "Mon, Wed, Fri", "Weekends", "Weekdays")
            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12131F)),
                    border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(repeatPattern, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF12131F)).border(BorderStroke(1.dp, Color(0xFF1D1F30)))
                ) {
                    patterns.forEach { pattern ->
                        DropdownMenuItem(
                            text = { Text(pattern, color = Color.White) },
                            onClick = {
                                repeatPattern = pattern
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description TextField
        Text(
            text = "DESCRIPTION (OPTIONAL)",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("What is this habit for?", color = Color(0xFF4B5563)) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12131F),
                unfocusedContainerColor = Color(0xFF12131F),
                focusedBorderColor = categoryColor,
                unfocusedBorderColor = Color(0xFF1D1F30)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Coins penalty toggle row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Coins penalty if missed",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lose coins if you miss this habit",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = isPenaltyEnabled,
                onCheckedChange = { isPenaltyEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = categoryColor,
                    uncheckedThumbColor = Color(0xFF9CA3AF),
                    uncheckedTrackColor = Color(0xFF1D1F30)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rewards Customize and Info Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Rewards & Penalty Settings",
                    color = categoryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("XP Reward:", color = Color.White, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { xpReward = (xpReward - 1).coerceAtLeast(0) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text("$xpReward XP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { xpReward += 1 }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Coins Reward:", color = Color.White, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { coinReward = (coinReward - 1).coerceAtLeast(0) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Text("$coinReward Coins", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { coinReward += 1 }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tags (FR-009)
        Text(
            text = "TAGS",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            placeholder = { Text("e.g. Health, Daily, Routine", color = Color(0xFF4B5563)) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF12131F),
                unfocusedContainerColor = Color(0xFF12131F),
                focusedBorderColor = categoryColor,
                unfocusedBorderColor = Color(0xFF1D1F30)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (showError != null) {
            Text(
                text = showError!!,
                color = Color(0xFFE53935),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Save Changes Button
        Button(
            onClick = {
                val validatedName = title.trim()
                if (validatedName.isEmpty()) {
                    showError = "Name is required (1-100 characters)."
                } else if (validatedName.length > 100) {
                    showError = "Name is too long (maximum 100 characters)."
                } else {
                    val tagList = tagInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    onConfirm(
                        validatedName,
                        selectedIcon,
                        colorHex,
                        currentValue,
                        goal,
                        unit,
                        step,
                        description,
                        repeatPattern,
                        xpReward,
                        coinReward,
                        isPenaltyEnabled,
                        tagList,
                        completedState
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_habit_submit_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save Changes",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}


