package com.example.shared.components.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.PriorityLevel
import com.example.PriorityTask

@Composable
fun TaskDialog(
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
