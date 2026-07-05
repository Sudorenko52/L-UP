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
fun TasksScreenContent(
    priorities: List<PriorityTask>,
    onTaskClick: (PriorityTask) -> Unit,
    onAddTaskClick: () -> Unit,
    onProgressUpdate: (String, Float) -> Unit
) {
    var selectedFilterTab by remember { mutableStateOf("Today") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchRow by remember { mutableStateOf(false) }

    // Dynamic calculation for the top progress card ("Today's Tasks")
    val todayTasks = priorities.filter { it.date == "Today" }
    val completedTodayCount = todayTasks.count { it.completed }
    val totalTodayCount = todayTasks.size
    val todayProgressPercent = if (totalTodayCount > 0) {
        ((completedTodayCount.toFloat() / totalTodayCount.toFloat()) * 100).toInt()
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- TITLE AND ACTION BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tasks",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search toggle button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(10.dp))
                        .background(Color(0xFF12131F), RoundedCornerShape(10.dp))
                        .clickable { showSearchRow = !showSearchRow }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (showSearchRow) Color(0xFF00F0FF) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Filter/Settings icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Color(0xFF1D1F30), RoundedCornerShape(10.dp))
                        .background(Color(0xFF12131F), RoundedCornerShape(10.dp))
                        .clickable { /* Toggles view layout or sorting */ }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- SEARCH INPUT ROW ---
        AnimatedVisibility(visible = showSearchRow) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks...", color = Color(0xFF6B7280), fontSize = 14.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8D6EFD),
                    unfocusedBorderColor = Color(0xFF1D1F30),
                    focusedContainerColor = Color(0xFF12131F),
                    unfocusedContainerColor = Color(0xFF12131F)
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Clear", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )
        }

        // --- TODAY'S PROGRESS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1D1F30))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular progress circle
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0x1F212338),
                            radius = size.minDimension / 2f,
                            style = Stroke(4.dp.toPx())
                        )
                        drawArc(
                            color = Color(0xFF8D6EFD),
                            startAngle = -90f,
                            sweepAngle = (todayProgressPercent.toFloat() / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF8D6EFD),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Today's Progress",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$completedTodayCount ",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "/ $totalTodayCount",
                            color = Color(0xFF6B7280),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(100.dp)
                ) {
                    // Linear progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color(0xFF161726), CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (totalTodayCount > 0) completedTodayCount.toFloat() / totalTodayCount.toFloat() else 0f)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                    ),
                                    CircleShape
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$todayProgressPercent%",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- FILTER CHIPS ROW ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val chips = listOf("Today", "Tomorrow", "Upcoming", "Completed")
            items(chips) { tab ->
                val isSelected = selectedFilterTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF12131F), Color(0xFF12131F))
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else Color(0xFF1D1F30),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedFilterTab = tab }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- FILTERED TASK LIST ---
        val filteredPriorities = remember(priorities, selectedFilterTab, searchQuery) {
            priorities.filter { task ->
                val matchesSearch = if (searchQuery.isNotEmpty()) {
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.description.contains(searchQuery, ignoreCase = true) ||
                    task.tags.any { it.contains(searchQuery, ignoreCase = true) }
                } else {
                    true
                }

                val matchesTab = when (selectedFilterTab) {
                    "Today" -> task.date == "Today" && !task.completed
                    "Tomorrow" -> task.date == "Tomorrow" && !task.completed
                    "Upcoming" -> task.date == "Upcoming" && !task.completed
                    "Completed" -> task.completed
                    else -> true
                }

                matchesSearch && matchesTab
            }
        }

        if (filteredPriorities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "No tasks",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No tasks found",
                        color = Color(0xFF9CA3AF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPriorities) { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) },
                        onProgressClick = {
                            val nextProgress = if (task.progress >= 1.0f) 0.0f else (task.progress + 0.1f).coerceIn(0f, 1f)
                            onProgressUpdate(task.id, nextProgress)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(84.dp))
                }
            }
        }
    }
}


