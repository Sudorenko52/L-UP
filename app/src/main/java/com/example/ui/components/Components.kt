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
import com.example.features.profile.ProfileOptionItem
import com.example.features.profile.AvatarView

@Composable
fun HexagonLevelBadge(level: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val size = size.minDimension
            val radius = size / 2f
            val centerX = size / 2f
            val centerY = size / 2f
            
            val path = Path().apply {
                for (i in 0 until 6) {
                    // Offset angle by 30 deg so point is at top/bottom
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
            text = level.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}


@Composable
fun SmallHexagonXpBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(18.dp),
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
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Text(
            text = "XP",
            color = Color(0xFF9F75FF),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 6.5.sp
        )
    }
}


@Composable
fun SmallGoldCoinBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17))
                ),
                shape = CircleShape
            )
            .border(1.dp, Color(0xFFFFF59D), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            modifier = Modifier.offset(y = (-0.5).dp)
        )
    }
}


@Composable
fun MainQuestCard(
    quest: MainQuest,
    onClaimQuest: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(148.dp)
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F5E)),
        border = BorderStroke(1.dp, Color(0xFF26338C)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw Mountain/Flag custom vector design in background
            MountainGraphic(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Header with star icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "MAIN QUEST",
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Main Quest",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quest Title
                Text(
                    text = quest.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar and text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick dynamic progress bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(Color(0x3DFFFFFF), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(quest.progress)
                                .fillMaxHeight()
                                .background(Color(0xFF00F0FF), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(quest.progress * 100).toInt()}%",
                        color = Color(0xFF818CF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Reward label
                Text(
                    text = "Reward",
                    color = Color(0xFF9CA3AF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // XP Reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallHexagonXpBadge()
                        Text(
                            text = "+${quest.xpReward} XP",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    // Coin Reward
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SmallGoldCoinBadge()
                        Text(
                            text = "+${quest.coinReward} Coins",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MountainGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Background mountains
        val mountainPath1 = Path().apply {
            moveTo(width * 0.2f, height)
            lineTo(width * 0.6f, height * 0.45f)
            lineTo(width, height)
            close()
        }
        drawPath(
            path = mountainPath1,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x2A00F0FF), Color(0x00000000))
            )
        )

        // Foreground mountain
        val mountainPath2 = Path().apply {
            moveTo(width * 0.4f, height)
            lineTo(width * 0.85f, height * 0.25f)
            lineTo(width * 1.2f, height)
            close()
        }
        drawPath(
            path = mountainPath2,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x4000F0FF), Color(0x00000000))
            )
        )

        // Flagpole at peak (0.85f, 0.25f)
        val peakX = width * 0.85f
        val peakY = height * 0.25f
        drawLine(
            color = Color.White,
            start = Offset(peakX, peakY),
            end = Offset(peakX, peakY - 24.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        
        // Flag flapping left
        val flagPath = Path().apply {
            moveTo(peakX, peakY - 24.dp.toPx())
            lineTo(peakX - 14.dp.toPx(), peakY - 19.dp.toPx())
            lineTo(peakX, peakY - 14.dp.toPx())
            close()
        }
        drawPath(
            path = flagPath,
            color = Color(0xFF00F0FF)
        )

        // Climber silhouette dot climbing up the mountain slope
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(width * 0.72f, height * 0.48f)
        )
    }
}


@Composable
fun HexagonXpBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(32.dp),
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
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        Text(
            text = "XP",
            color = Color(0xFF9F75FF),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 9.sp
        )
    }
}


@Composable
fun GoldCoinBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFF57F17))
                ),
                shape = CircleShape
            )
            .border(1.2.dp, Color(0xFFFFF59D), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            modifier = Modifier.offset(y = (-0.5).dp)
        )
    }
}


@Composable
fun GreenCheckBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                color = Color(0xFF4ADE80),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF0D0E17),
            modifier = Modifier.size(16.dp)
        )
    }
}


@Composable
fun TaskEditorDialog(
    task: PriorityTask?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        level: PriorityLevel,
        iconName: String,
        description: String,
        xpReward: Int,
        coinReward: Int,
        time: String,
        date: String,
        progress: Float,
        tags: List<String>,
        repeat: String
    ) -> Unit,
    onDelete: (() -> Unit)?
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedLevel by remember { mutableStateOf(task?.level ?: PriorityLevel.NORMAL) }
    var selectedIcon by remember { mutableStateOf(task?.iconName ?: "Gym") }
    var xpReward by remember { mutableStateOf(task?.xpReward ?: 20) }
    var coinReward by remember { mutableStateOf(task?.coinReward ?: 3) }
    var time by remember { mutableStateOf(task?.time ?: "") }
    var selectedDate by remember { mutableStateOf(task?.date ?: "Today") }
    var progressValue by remember { mutableStateOf(task?.progress ?: 0f) }
    var tagsInput by remember { mutableStateOf(task?.tags?.joinToString(", ") ?: "") }
    var selectedRepeat by remember { mutableStateOf(task?.repeat ?: "Once") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (task == null) "Create Task" else "Edit Task",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = Color(0xFF9CA3AF)) },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority Selection
                Text("Priority Level", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityLevel.values().forEach { level ->
                        val isSelected = selectedLevel == level
                        val levelColor = Color(level.colorHex)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) levelColor.copy(alpha = 0.25f)
                                    else Color(0xFF161726)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) levelColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedLevel = level }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.displayName,
                                color = levelColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Date Selection
                Text("Schedule Date", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dates = listOf("Today", "Tomorrow", "Upcoming")
                    dates.forEach { dateOption ->
                        val isSelected = selectedDate == dateOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF8D6EFD).copy(alpha = 0.25f)
                                    else Color(0xFF161726)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDate = dateOption }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateOption,
                                color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Progress Slider (0% to 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Progress: ${(progressValue * 100).toInt()}%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = progressValue,
                    onValueChange = { progressValue = it },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF8D6EFD),
                        activeTrackColor = Color(0xFF8D6EFD),
                        inactiveTrackColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon Selection
                Text("Category Icon", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                val icons = listOf(
                    "Gym" to Icons.Default.FitnessCenter,
                    "Book" to Icons.Default.Book,
                    "Clean" to Icons.Default.Computer,
                    "Code" to Icons.Default.Code,
                    "Water" to Icons.Default.Opacity,
                    "General" to Icons.Default.Star
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { (name, iconVector) ->
                        val isSelected = selectedIcon == name
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF8D6EFD).copy(alpha = 0.2f)
                                    else Color(0xFF161726)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedIcon = name },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = name,
                                tint = if (isSelected) Color(0xFF8D6EFD) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Reward Configuration (XP and Coins)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // XP Reward Input
                    OutlinedTextField(
                        value = xpReward.toString(),
                        onValueChange = { xpReward = it.toIntOrNull() ?: 0 },
                        label = { Text("XP Reward", color = Color(0xFF9CA3AF)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Coins Reward Input
                    OutlinedTextField(
                        value = coinReward.toString(),
                        onValueChange = { coinReward = it.toIntOrNull() ?: 0 },
                        label = { Text("Coins Reward", color = Color(0xFF9CA3AF)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Time and Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time Input
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time (e.g. 18:00)", color = Color(0xFF9CA3AF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Repeat Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedRepeat,
                            onValueChange = { selectedRepeat = it },
                            label = { Text("Repeat", color = Color(0xFF9CA3AF)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF8D6EFD),
                                unfocusedBorderColor = Color(0xFF1D1F30)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Tags Input
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated, e.g. Fitness, Health)", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions Column
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFF9CA3AF))
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val tagsList = tagsInput
                                        .split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                    onConfirm(
                                        title,
                                        selectedLevel,
                                        selectedIcon,
                                        description,
                                        xpReward,
                                        coinReward,
                                        time,
                                        selectedDate,
                                        progressValue,
                                        tagsList,
                                        selectedRepeat
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD))
                        ) {
                            Text(if (task == null) "Create" else "Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String, Long, Float, String, Float) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Habit Editor",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "To keep habits simple and clean, custom habit additions are coming soon. Tap anywhere to close.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK")
                }
            }
        }
    }
}


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


@Composable
fun MainQuestEditorDialog(
    quest: Quest?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        targetValue: Float,
        currentValue: Float,
        xpReward: Int,
        coinReward: Int,
        chest: RewardChest
    ) -> Unit
) {
    var title by remember { mutableStateOf(quest?.title ?: "") }
    var description by remember { mutableStateOf(quest?.description ?: "") }
    var targetValueString by remember { mutableStateOf(quest?.targetValue?.toInt()?.toString() ?: "1") }
    var currentValueString by remember { mutableStateOf(quest?.currentValue?.toInt()?.toString() ?: "0") }
    var xpRewardString by remember { mutableStateOf(quest?.xpReward?.toString() ?: "100") }
    var coinRewardString by remember { mutableStateOf(quest?.coinReward?.toString() ?: "50") }
    var chest by remember { mutableStateOf(quest?.chest ?: RewardChest.EPIC) }

    val chests = listOf(RewardChest.NONE, RewardChest.SILVER, RewardChest.GOLD, RewardChest.EPIC)
    var showChestDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Редагувати Головний Квест",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Назва Квесту", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Опис Квесту", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Progress Inputs Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentValueString,
                        onValueChange = { currentValueString = it },
                        label = { Text("Поточний прогрес", color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = targetValueString,
                        onValueChange = { targetValueString = it },
                        label = { Text("Ціль", color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rewards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = xpRewardString,
                        onValueChange = { xpRewardString = it },
                        label = { Text("Нагорода XP", color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = coinRewardString,
                        onValueChange = { coinRewardString = it },
                        label = { Text("Нагорода Монети", color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF8D6EFD),
                            unfocusedBorderColor = Color(0xFF1D1F30)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Chest Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "СКРИНЯ З НАГОРОДОЮ", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showChestDropdown = true },
                            color = Color(0xFF1D1F30).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF1D1F30))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redeem,
                                        contentDescription = "Chest",
                                        tint = when (chest) {
                                            RewardChest.SILVER -> Color(0xFFC0C0C0)
                                            RewardChest.GOLD -> Color(0xFFFFD700)
                                            RewardChest.EPIC -> Color(0xFF9333EA)
                                            else -> Color.Gray
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (chest == RewardChest.NONE) "Без скрині" else "${chest.name} Скриня",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Open", tint = Color.White)
                            }
                        }

                        DropdownMenu(
                            expanded = showChestDropdown,
                            onDismissRequest = { showChestDropdown = false },
                            modifier = Modifier.background(Color(0xFF12131F))
                        ) {
                            chests.forEach { ch ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (ch == RewardChest.NONE) "Без скрині" else "${ch.name} Скриня",
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        chest = ch
                                        showChestDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Скасувати", color = Color.White.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                onConfirm(
                                    title,
                                    description,
                                    targetValueString.toFloatOrNull() ?: 1.0f,
                                    currentValueString.toFloatOrNull() ?: 0.0f,
                                    xpRewardString.toIntOrNull() ?: 100,
                                    coinRewardString.toIntOrNull() ?: 50,
                                    chest
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f),
                        enabled = title.isNotEmpty()
                    ) {
                        Text("Зберегти", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun MiniProfileBottomSheet(
    userStats: UserStats,
    onDismiss: () -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateAvatar: (Int) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onResetStats: () -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeAvatarDialog by remember { mutableStateOf(false) }
    var showTitlesDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    var soundEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var hapticFeedback by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks */ }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        if (dragAmount.y > 15f) {
                            onDismiss()
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D14)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, Color(0xFF1D1F30))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(Color(0xFF374151), CircleShape)
                        .clickable { onDismiss() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar and Profile Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(
                        avatarId = userStats.avatarId,
                        size = 64.dp,
                        borderColor = Color(0xFF8D6EFD),
                        borderWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userStats.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { showEditNameDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = Color(0xFF8D6EFD),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF8D6EFD),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = userStats.selectedTitle.ifBlank { "No titles yet" },
                                color = Color(0xFF8D6EFD),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Level indicator: Level 12, progress bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level ${userStats.level}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${userStats.xp} / ${userStats.maxXp} XP",
                            color = Color(0xFF8D6EFD),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }

                    // Progress bar
                    val progressRatio = (userStats.xp.toFloat() / userStats.maxXp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFF161726), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats row: Day Streak, Coins, Total XP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Day Streak
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Day Streak",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${userStats.streak}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Day Streak",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Coins
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "${userStats.coins}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coins",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Total XP
                    Card(
                        modifier = Modifier.weight(1.2f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11121E)),
                        border = BorderStroke(1.dp, Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HexagonXpBadge(modifier = Modifier.size(24.dp))
                            Text(
                                text = String.format("%,d", userStats.totalXp),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total XP",
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // List of options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        label = "Edit Profile",
                        onClick = { showEditNameDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Photo,
                        label = "Change Avatar",
                        onClick = { showChangeAvatarDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.EmojiEvents,
                        label = "Titles",
                        onClick = { showTitlesDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.MilitaryTech,
                        label = "Badges",
                        onClick = { showBadgesDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = { showSettingsDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.Info,
                        label = "About",
                        onClick = { showAboutDialog = true }
                    )
                    ProfileOptionItem(
                        icon = Icons.Default.ExitToApp,
                        label = "Log Out",
                        textColor = Color(0xFFEF4444),
                        onClick = { showLogoutConfirm = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Sub-dialogs
    if (showEditNameDialog) {
        EditProfileNameDialog(
            currentName = userStats.name,
            onDismiss = { showEditNameDialog = false },
            onConfirm = {
                onUpdateName(it)
                showEditNameDialog = false
            }
        )
    }

    if (showChangeAvatarDialog) {
        ChangeAvatarDialog(
            selectedAvatarId = userStats.avatarId,
            onDismiss = { showChangeAvatarDialog = false },
            onSelect = {
                onUpdateAvatar(it)
                showChangeAvatarDialog = false
            }
        )
    }

    if (showTitlesDialog) {
        TitlesDialog(
            selectedTitle = userStats.selectedTitle,
            userLevel = userStats.level,
            onDismiss = { showTitlesDialog = false },
            onEquip = {
                onUpdateTitle(it)
                showTitlesDialog = false
            }
        )
    }

    if (showBadgesDialog) {
        BadgesDialog(
            unlockedBadges = userStats.unlockedBadges,
            onDismiss = { showBadgesDialog = false }
        )
    }

    if (showSettingsDialog) {
        SettingsSubDialog(
            soundEnabled = soundEnabled,
            onSoundToggle = { soundEnabled = it },
            notificationsEnabled = notificationsEnabled,
            onNotificationsToggle = { notificationsEnabled = it },
            hapticFeedback = hapticFeedback,
            onHapticToggle = { hapticFeedback = it },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutSubDialog(onDismiss = { showAboutDialog = false })
    }

    if (showLogoutConfirm) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutConfirm = false },
            onConfirm = {
                onResetStats()
                showLogoutConfirm = false
            }
        )
    }
}


@Composable
fun EditProfileNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Edit Profile Name",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF1D1F30)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onConfirm(nameInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun ChangeAvatarDialog(
    selectedAvatarId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose Your Avatar",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val rows = listOf(0..3, 4..7)
                    rows.forEach { range ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            range.forEach { id ->
                                val isSelected = id == selectedAvatarId
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            onSelect(id)
                                        }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarView(
                                        avatarId = id,
                                        size = 56.dp,
                                        borderColor = if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                        borderWidth = if (isSelected) 3.dp else 0.dp
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(Color(0xFF00F0FF), CircleShape)
                                                .align(Alignment.BottomEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun TitlesDialog(
    selectedTitle: String,
    userLevel: Int,
    onDismiss: () -> Unit,
    onEquip: (String) -> Unit
) {
    val titlesList = listOf(
        Pair("Shadow Runner", 1),
        Pair("Novice Tasker", 1),
        Pair("Bug Hunter", 1),
        Pair("Code Ninja", 5),
        Pair("Quest Master", 8),
        Pair("Legendary Warrior", 12),
        Pair("Godlike Slayer", 15),
        Pair("Zen Master", 20)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Title",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    items(titlesList) { (titleName, requiredLevel) ->
                        val isLocked = userLevel < requiredLevel
                        val isEquipped = titleName == selectedTitle

                        Surface(
                            onClick = {
                                if (!isLocked && !isEquipped) {
                                    onEquip(titleName)
                                }
                            },
                            enabled = !isLocked,
                            color = if (isEquipped) Color(0xFF8D6EFD).copy(alpha = 0.15f) else Color(0xFF161726).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isEquipped) Color(0xFF8D6EFD) else Color(0xFF1D1F30)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = titleName,
                                        color = if (isLocked) Color.White.copy(alpha = 0.4f) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (isLocked) {
                                        Text(
                                            text = "Requires Level $requiredLevel",
                                            color = Color(0xFFEF4444),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isEquipped) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Equipped", color = Color(0xFF8D6EFD), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Equip",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun BadgesDialog(
    unlockedBadges: List<String>,
    onDismiss: () -> Unit
) {
    val badgesList = listOf(
        Triple("First Step", "Completed your first task", Icons.Default.CheckCircle),
        Triple("7-Day Warrior", "Maintained a 7-day streak", Icons.Default.Whatshot),
        Triple("Coin Collector", "Earned 100+ coins", Icons.Default.MonetizationOn),
        Triple("Main Quest Hero", "Complete a Main Quest", Icons.Default.EmojiEvents),
        Triple("Task Slayer", "Complete 10+ tasks", Icons.Default.FitnessCenter),
        Triple("Elite Explorer", "Complete all category quests", Icons.Default.Explore)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Your Badges",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    items(badgesList) { (badgeName, badgeDesc, icon) ->
                        val isUnlocked = unlockedBadges.contains(badgeName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF161726).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isUnlocked) Color(0xFF8D6EFD).copy(alpha = 0.2f) else Color(0xFF374151).copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = badgeName,
                                    tint = if (isUnlocked) Color(0xFF8D6EFD) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badgeName,
                                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = badgeDesc,
                                    color = if (isUnlocked) Color(0xFF9CA3AF) else Color.White.copy(alpha = 0.25f),
                                    fontSize = 11.sp
                                )
                            }

                            if (isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Unlocked", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun SettingsSubDialog(
    soundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    hapticFeedback: Boolean,
    onHapticToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "App Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Sound Effects", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFF1D1F30))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Reminders & Notifications", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFF1D1F30))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF8D6EFD))
                        Text("Haptic Feedback", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = onHapticToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8D6EFD)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Are you sure you want to log out?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Logging out will reset all your temporary local progress and game achievements.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Yes, Reset & Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}




data class PresetOption(
    val name: String,
    val iconName: String,
    val icon: ImageVector,
    val colorHex: Long,
    val goal: Float,
    val unit: String,
    val step: Float,
    val xp: Int,
    val coins: Int
)

data class CustomAvatar(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val startColor: Color,
    val endColor: Color
)
