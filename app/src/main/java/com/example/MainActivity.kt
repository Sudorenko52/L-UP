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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: GamifiedTaskViewModel = viewModel()) {
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()
    val mainQuest by viewModel.mainQuest.collectAsStateWithLifecycle()
    val priorities by viewModel.priorities.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val todayXpEarned by viewModel.todayXpEarned.collectAsStateWithLifecycle()
    val todayCoinsEarned by viewModel.todayCoinsEarned.collectAsStateWithLifecycle()

    var showTaskDialog by remember { mutableStateOf(false) }
    var showMainQuestDialog by remember { mutableStateOf(false) }
    var showMiniProfile by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<PriorityTask?>(null) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var isDeleteMode by remember { mutableStateOf(false) }

    var currentSubScreen by remember { mutableStateOf<String?>(null) }
    var habitToEditId by remember { mutableStateOf<String?>(null) }
    var selectedQuestId by remember { mutableStateOf<String?>(null) }

    // Calculate dynamic stats
    val completedPrioritiesCount = priorities.count { it.completed }
    val completedHabitsCount = habits.count { it.completed }
    val totalCompletedTasks = completedPrioritiesCount + completedHabitsCount
    val totalTasksCount = priorities.size + habits.size
    
    val todayProgressPercent = if (totalTasksCount > 0) {
        ((totalCompletedTasks.toFloat() / totalTasksCount.toFloat()) * 100).toInt()
    } else {
        0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090A0F),
        bottomBar = {
            if (currentSubScreen == null || currentSubScreen == "habits" || currentSubScreen == "weekly_list" || currentSubScreen == "monthly_list" || currentSubScreen == "special_list") {
                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        currentSubScreen = null
                        viewModel.selectTab(it)
                    }
                )
            }
        },
        floatingActionButton = {
            if ((selectedTab == AppTab.HOME || selectedTab == AppTab.TASKS) && currentSubScreen == null) {
                FloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        showTaskDialog = true
                    },
                    modifier = Modifier
                        .testTag("add_task_fab")
                        .padding(bottom = 76.dp, end = 8.dp),
                    shape = CircleShape,
                    containerColor = Color.Transparent,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentSubScreen != null) {
                androidx.activity.compose.BackHandler {
                    if (currentSubScreen == "edit_habit" || currentSubScreen == "add_habit") {
                        currentSubScreen = "habits"
                    } else if (currentSubScreen == "habits") {
                        currentSubScreen = null
                    }
                }
            }

            when (selectedTab) {
                AppTab.HOME -> {
                    when (currentSubScreen) {
                        "habits" -> {
                            HabitsManagerScreen(
                                habits = habits,
                                onBack = { currentSubScreen = null },
                                onAddHabit = { currentSubScreen = "add_habit" },
                                onEditHabit = { id ->
                                    habitToEditId = id
                                    currentSubScreen = "edit_habit"
                                },
                                onToggleHabit = { id, completed ->
                                    viewModel.toggleHabitCompletion(id, completed)
                                },
                                onMoveUp = { viewModel.moveHabitUp(it) },
                                onMoveDown = { viewModel.moveHabitDown(it) }
                            )
                        }
                        "add_habit" -> {
                            AddHabitScreen(
                                onBack = { currentSubScreen = "habits" },
                                onConfirm = { title, icon, color, goal, unit, step, desc, repeat, xp, coins, penalty, tags ->
                                    viewModel.addHabit(title, icon, color, goal, unit, step, desc, repeat, xp, coins, penalty, tags)
                                    currentSubScreen = "habits"
                                }
                            )
                        }
                        "edit_habit" -> {
                            val habit = habits.find { it.id == habitToEditId }
                            if (habit != null) {
                                EditHabitScreen(
                                    habit = habit,
                                    onBack = { currentSubScreen = "habits" },
                                    onConfirm = { title, icon, color, currentValue, goal, unit, step, desc, repeat, xp, coins, penalty, tags, completed ->
                                        viewModel.updateHabit(habit.id, title, icon, color, currentValue, goal, unit, step, desc, repeat, xp, coins, penalty, tags, completed)
                                        currentSubScreen = "habits"
                                    },
                                    onDelete = {
                                        viewModel.deleteHabit(habit.id)
                                        currentSubScreen = "habits"
                                    }
                                )
                            } else {
                                currentSubScreen = "habits"
                            }
                        }
                        else -> {
                            HomeScreenContent(
                                userStats = userStats,
                                mainQuest = mainQuest,
                                priorities = priorities.filter { !it.completed },
                                habits = habits,
                                todayXpEarned = todayXpEarned,
                                todayCoinsEarned = todayCoinsEarned,
                                completedCount = totalCompletedTasks,
                                totalCount = totalTasksCount,
                                progressPercent = todayProgressPercent,
                                isDeleteMode = isDeleteMode,
                                onPriorityToggle = { viewModel.togglePriority(it) },
                                onHabitClick = { id ->
                                    viewModel.incrementHabit(id)
                                },
                                onDeletePriority = { viewModel.deletePriorityTask(it) },
                                onToggleDeleteMode = { isDeleteMode = !isDeleteMode },
                                onClaimQuest = { viewModel.claimMainQuest() },
                                onAddHabitClick = { currentSubScreen = "habits" },
                                onEditMainQuestClick = { showMainQuestDialog = true },
                                onProfileClick = { showMiniProfile = true }
                            )
                        }
                    }
                }
                AppTab.TASKS -> {
                    TasksScreenContent(
                        priorities = priorities,
                        onTaskClick = { task ->
                            taskToEdit = task
                            showTaskDialog = true
                        },
                        onAddTaskClick = {
                            taskToEdit = null
                            showTaskDialog = true
                        },
                        onProgressUpdate = { id, progress ->
                            viewModel.updateTaskProgress(id, progress)
                        }
                    )
                }
                AppTab.QUESTS -> {
                    QuestsScreenContent(
                        viewModel = viewModel,
                        currentSubScreen = currentSubScreen,
                        onSubScreenChanged = { currentSubScreen = it },
                        selectedQuestId = selectedQuestId,
                        onSelectedQuestIdChanged = { selectedQuestId = it },
                        onEditMainQuestClick = { showMainQuestDialog = true }
                    )
                }
                AppTab.PROGRESS -> {
                    ProgressScreenContent(
                        userStats = userStats,
                        priorities = priorities,
                        habits = habits,
                        quests = quests,
                        todayXp = todayXpEarned,
                        todayCoins = todayCoinsEarned
                    )
                }
                AppTab.PROFILE -> {
                    ProfileScreenContent(
                        userStats = userStats,
                        onResetAll = { viewModel.resetAll() }
                    )
                }
            }

            // Dialog for adding or editing a task
            if (showTaskDialog) {
                TaskEditorDialog(
                    task = taskToEdit,
                    onDismiss = { showTaskDialog = false },
                    onConfirm = { title, level, icon, desc, xp, coins, time, date, progress, tags, repeat ->
                        if (taskToEdit == null) {
                            viewModel.addPriorityTask(title, level, icon, desc, xp, coins, time, date, progress, tags, repeat)
                        } else {
                            viewModel.updatePriorityTask(taskToEdit!!.id, title, level, icon, desc, xp, coins, time, date, progress, tags, repeat)
                        }
                        showTaskDialog = false
                    },
                    onDelete = taskToEdit?.let { task ->
                        {
                            viewModel.deletePriorityTask(task.id)
                            showTaskDialog = false
                        }
                    }
                )
            }

            // Dialog for adding a new habit
            if (showAddHabitDialog) {
                AddHabitDialog(
                    onDismiss = { showAddHabitDialog = false },
                    onConfirm = { title, icon, color, goal, unit, step ->
                        // Dynamically append new habit (mock-added into list in ViewModel)
                        showAddHabitDialog = false
                    }
                )
            }

            // Dialog for editing the main quest
            if (showMainQuestDialog) {
                val mq = quests.find { it.type == QuestType.MAIN }
                MainQuestEditorDialog(
                    quest = mq,
                    onDismiss = { showMainQuestDialog = false },
                    onConfirm = { title, description, targetValue, currentValue, xpReward, coinReward, chest ->
                        viewModel.updateMainQuest(title, description, targetValue, currentValue, xpReward, coinReward, chest)
                        showMainQuestDialog = false
                    }
                )
            }

            // Mini Profile Bottom Sheet Overlay
            AnimatedVisibility(
                visible = showMiniProfile,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                MiniProfileBottomSheet(
                    userStats = userStats,
                    onDismiss = { showMiniProfile = false },
                    onUpdateName = { viewModel.updateProfileName(it) },
                    onUpdateAvatar = { viewModel.updateAvatar(it) },
                    onUpdateTitle = { viewModel.updateSelectedTitle(it) },
                    onResetStats = {
                        viewModel.resetAll()
                        showMiniProfile = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// HOME CONTENT
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// PROFILE HEADER
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// TODAY PROGRESS CARD
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// MAIN QUEST CARD
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// TOP PRIORITIES SECTION
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// HABITS SECTION
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// QUICK STATS SECTION
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// DIALOGS
// ---------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------
// DIALOGS
// ---------------------------------------------------------------------------------
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

// ---------------------------------------------------------------------------------
// TAB SCREENS (TASKS, QUESTS, PROGRESS, PROFILE)
// ---------------------------------------------------------------------------------
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
fun QuestsScreenContent(
    viewModel: GamifiedTaskViewModel,
    currentSubScreen: String?,
    onSubScreenChanged: (String?) -> Unit,
    selectedQuestId: String?,
    onSelectedQuestIdChanged: (String?) -> Unit,
    onEditMainQuestClick: () -> Unit
) {
    val quests by viewModel.quests.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    when (currentSubScreen) {
        "weekly_list" -> {
            QuestListScreen(
                title = "Weekly Quests",
                type = QuestType.WEEKLY,
                quests = quests.filter { it.type == QuestType.WEEKLY },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "monthly_list" -> {
            QuestListScreen(
                title = "Monthly Quests",
                type = QuestType.MONTHLY,
                quests = quests.filter { it.type == QuestType.MONTHLY },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "special_list" -> {
            QuestListScreen(
                title = "Special Quests",
                type = QuestType.SPECIAL,
                quests = quests.filter { it.type == QuestType.SPECIAL },
                onBack = { onSubScreenChanged(null) },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onQuestClick = { id ->
                    onSelectedQuestIdChanged(id)
                    onSubScreenChanged("quest_details")
                }
            )
        }
        "quest_details" -> {
            val quest = quests.find { it.id == selectedQuestId }
            if (quest != null) {
                QuestDetailsScreen(
                    quest = quest,
                    onBack = { onSubScreenChanged(quest.type.name.lowercase() + "_list") },
                    onEdit = { onSubScreenChanged("edit_quest") },
                    onDelete = {
                        viewModel.deleteQuest(quest.id)
                        onSubScreenChanged(quest.type.name.lowercase() + "_list")
                    },
                    onComplete = {
                        viewModel.completeManualQuest(quest.id)
                    }
                )
            } else {
                onSubScreenChanged(null)
            }
        }
        "create_quest" -> {
            QuestCreatorScreen(
                quest = null,
                onBack = { onBackTarget ->
                    onSubScreenChanged(null)
                },
                onConfirm = { title, type, desc, target, xp, coins, chest, targetType, tags, duration ->
                    viewModel.addQuest(title, type, desc, target, xp, coins, chest, targetType, tags, duration)
                    if (type == QuestType.MAIN) {
                        onSubScreenChanged(null)
                    } else {
                        onSubScreenChanged(type.name.lowercase() + "_list")
                    }
                }
            )
        }
        "edit_quest" -> {
            val quest = quests.find { it.id == selectedQuestId }
            if (quest != null) {
                QuestCreatorScreen(
                    quest = quest,
                    onBack = { onBackTarget ->
                        onSubScreenChanged("quest_details")
                    },
                    onConfirm = { title, type, desc, target, xp, coins, chest, targetType, tags, duration ->
                        viewModel.updateQuest(quest.id, title, type, desc, target, quest.currentValue, xp, coins, chest, quest.status, targetType, tags, duration)
                        onSubScreenChanged("quest_details")
                    }
                )
            } else {
                onSubScreenChanged(null)
            }
        }
        else -> {
            QuestsOverviewScreen(
                quests = quests,
                streak = userStats.streak,
                onClaimMainQuest = { viewModel.claimMainQuest() },
                onCategoryClick = { category ->
                    onSubScreenChanged(category + "_list")
                },
                onAddQuest = { onSubScreenChanged("create_quest") },
                onEditMainQuestClick = onEditMainQuestClick
            )
        }
    }
}

@Composable
fun QuestsOverviewScreen(
    quests: List<Quest>,
    streak: Int,
    onClaimMainQuest: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onAddQuest: () -> Unit,
    onEditMainQuestClick: () -> Unit
) {
    var selectedTabState by remember { mutableStateOf("Main") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quests",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("quests_screen_title")
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Text(
                        text = "$streak Streak",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Main", "Weekly", "Monthly", "Special")
            tabs.forEach { tab ->
                val isSelected = selectedTabState == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTabState = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        when (selectedTabState) {
            "Main" -> {
                val mainQuest = quests.find { it.type == QuestType.MAIN }
                if (mainQuest != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditMainQuestClick() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F5E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f))
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = "Main Quest Icon",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "MAIN QUEST",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = onEditMainQuestClick,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Main Quest",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    if (mainQuest.status == QuestStatus.COMPLETED) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Completed",
                                                color = Color(0xFF10B981),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = mainQuest.title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (mainQuest.description.isNotEmpty()) {
                                Text(
                                    text = mainQuest.description,
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 13.sp
                                )
                            }

                            val progressPercent = (mainQuest.currentValue / mainQuest.targetValue).coerceIn(0f, 1f)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${mainQuest.currentValue.toInt()} / ${mainQuest.targetValue.toInt()} XP",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(progressPercent * 100).toInt()}%",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF00F0FF),
                                trackColor = Color(0x22FFFFFF)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Reward:",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+${mainQuest.xpReward} XP",
                                            color = Color(0xFF8D6EFD),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .background(Color(0xFFFFD700), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "$", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "+${mainQuest.coinReward}",
                                                color = Color(0xFFFFB300),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (mainQuest.chest != RewardChest.NONE) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Redeem,
                                                    contentDescription = "Chest",
                                                    tint = when (mainQuest.chest) {
                                                        RewardChest.SILVER -> Color(0xFFC0C0C0)
                                                        RewardChest.GOLD -> Color(0xFFFFD700)
                                                        RewardChest.EPIC -> Color(0xFF9333EA)
                                                        else -> Color.White
                                                    },
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${mainQuest.chest.name} Chest",
                                                    color = when (mainQuest.chest) {
                                                        RewardChest.SILVER -> Color(0xFFC0C0C0)
                                                        RewardChest.GOLD -> Color(0xFFFFD700)
                                                        RewardChest.EPIC -> Color(0xFFA855F7)
                                                        else -> Color.White
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                if (mainQuest.status == QuestStatus.ACTIVE) {
                                    Button(
                                        onClick = onClaimMainQuest,
                                        enabled = progressPercent >= 1.0f,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8D6EFD),
                                            disabledContainerColor = Color(0xFF2C1E5C)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (progressPercent >= 1.0f) "Завершити квест" else "В процесі",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                            disabledContentColor = Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Завершено",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Create your Main Quest",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Quests are long-term goals. Set a primary focus to track your growth.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onAddQuest,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Text(text = "New Main Quest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACTIVE QUESTS",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val weeklyCount = quests.filter { it.type == QuestType.WEEKLY }
                val weeklyCompleted = weeklyCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Weekly Quests",
                    subtitle = "$weeklyCompleted / ${weeklyCount.size} completed",
                    icon = Icons.Default.CalendarToday,
                    iconColor = Color(0xFF00F0FF),
                    onClick = { onCategoryClick("weekly") }
                )

                val monthlyCount = quests.filter { it.type == QuestType.MONTHLY }
                val monthlyCompleted = monthlyCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Monthly Quests",
                    subtitle = "$monthlyCompleted / ${monthlyCount.size} completed",
                    icon = Icons.Default.DateRange,
                    iconColor = Color(0xFFC084FC),
                    onClick = { onCategoryClick("monthly") }
                )

                val specialCount = quests.filter { it.type == QuestType.SPECIAL }
                val specialCompleted = specialCount.count { it.status == QuestStatus.COMPLETED }
                CategoryCard(
                    title = "Special Quests",
                    subtitle = "$specialCompleted / ${specialCount.size} completed",
                    icon = Icons.Default.Diamond,
                    iconColor = Color(0xFFFBBF24),
                    onClick = { onCategoryClick("special") }
                )
            }
            "Weekly" -> {
                QuestListTabContent(
                    title = "Weekly Quests",
                    type = QuestType.WEEKLY,
                    quests = quests.filter { it.type == QuestType.WEEKLY },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
            "Monthly" -> {
                QuestListTabContent(
                    title = "Monthly Quests",
                    type = QuestType.MONTHLY,
                    quests = quests.filter { it.type == QuestType.MONTHLY },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
            "Special" -> {
                QuestListTabContent(
                    title = "Special Quests",
                    type = QuestType.SPECIAL,
                    quests = quests.filter { it.type == QuestType.SPECIAL },
                    onCategoryClick = onCategoryClick,
                    onAddQuest = onAddQuest
                )
            }
        }
        
        Spacer(modifier = Modifier.height(76.dp))
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun QuestListTabContent(
    title: String,
    type: QuestType,
    quests: List<Quest>,
    onCategoryClick: (String) -> Unit,
    onAddQuest: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Quests",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onAddQuest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD).copy(alpha = 0.2f), contentColor = Color(0xFF8D6EFD)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                    Text(text = "Add Quest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCategoryClick(type.name.lowercase()) },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Open $title List",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage quests, view detail trackers, and claim milestones.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color(0xFF8D6EFD)
                )
            }
        }

        if (quests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No quests added yet.", color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        } else {
            quests.forEach { quest ->
                QuestItemRow(
                    quest = quest,
                    onClick = { onCategoryClick(type.name.lowercase()) }
                )
            }
        }
    }
}

@Composable
fun QuestItemRow(
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

@Composable
fun QuestListScreen(
    title: String,
    type: QuestType,
    quests: List<Quest>,
    onBack: () -> Unit,
    onAddQuest: () -> Unit,
    onQuestClick: (String) -> Unit
) {
    val totalCount = quests.size
    val completedCount = quests.count { it.status == QuestStatus.COMPLETED }
    val progressPercent = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onAddQuest) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Quest", tint = Color(0xFF00F0FF))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (type == QuestType.WEEKLY || type == QuestType.MONTHLY) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F5E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (type == QuestType.WEEKLY) "WEEKLY REWARD" else "MONTHLY REWARD",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Complete all active quests of this category to claim a Silver Chest of bonus XP & Coins!",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$completedCount / $totalCount completed",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${(progressPercent * 100).toInt()}%",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { progressPercent },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF8D6EFD),
                                    trackColor = Color(0x22FFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Redeem,
                                        contentDescription = "Chest",
                                        tint = if (progressPercent >= 1.0f) Color(0xFFFFD700) else Color(0xFFC0C0C0),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = if (progressPercent >= 1.0f) "CLAIMABLE" else "LOCKED",
                                    color = if (progressPercent >= 1.0f) Color(0xFFFFD700) else Color(0xFF9CA3AF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (quests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No quests in this category. Click '+' to add one!", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                    }
                }
            } else {
                items(quests) { quest ->
                    QuestItemRow(
                        quest = quest,
                        onClick = { onQuestClick(quest.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }
    }
}

@Composable
fun QuestDetailsScreen(
    quest: Quest,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    val progressPercent = (quest.currentValue / quest.targetValue).coerceIn(0f, 1f)
    val isCompleted = quest.status == QuestStatus.COMPLETED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "Quest Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Quest", tint = Color(0xFF00F0FF))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quest.type.displayName,
                            color = Color(0xFFC084FC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val statusColor = when (quest.status) {
                        QuestStatus.ACTIVE -> Color(0xFF38BDF8)
                        QuestStatus.COMPLETED -> Color(0xFF34D399)
                        QuestStatus.EXPIRED -> Color(0xFFF87171)
                    }
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quest.status.name,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = quest.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (quest.description.isNotEmpty()) {
                    Text(
                        text = quest.description,
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROGRESS",
                            color = Color(0xFF00F0FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${quest.currentValue.toInt()} / ${quest.targetValue.toInt()}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = if (isCompleted) Color(0xFF10B981) else Color(0xFF8D6EFD),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "PROGRESS RULE",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quest Engine tracks completed actions of type '${quest.targetType}'. Progress updates automatically without manual check-offs.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TAGS COUNTED",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (quest.tags.isEmpty()) {
                    Text(
                        text = "Matches any ${quest.targetType} completion.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quest.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Color(0xFFA78BFA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REWARD",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = "XP", tint = Color(0xFF8D6EFD), modifier = Modifier.size(16.dp))
                        Text(text = "+${quest.xpReward} XP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "+${quest.coinReward} Coins", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    if (quest.chest != RewardChest.NONE) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redeem,
                                contentDescription = "Chest",
                                tint = when (quest.chest) {
                                    RewardChest.SILVER -> Color(0xFFC0C0C0)
                                    RewardChest.GOLD -> Color(0xFFFFD700)
                                    RewardChest.EPIC -> Color(0xFF9333EA)
                                    else -> Color.White
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${quest.chest.name} Chest",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DURATION LIMIT",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = quest.durationText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (quest.type != QuestType.SPECIAL && quest.status == QuestStatus.ACTIVE && quest.currentValue >= quest.targetValue) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("complete_quest_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Завершити квест", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onDelete,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = "Delete Quest", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun QuestCreatorScreen(
    quest: Quest?,
    onBack: (String) -> Unit,
    onConfirm: (
        title: String,
        type: QuestType,
        description: String,
        targetValue: Float,
        xpReward: Int,
        coinReward: Int,
        chest: RewardChest,
        targetType: String,
        tags: List<String>,
        durationText: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(quest?.title ?: "") }
    var type by remember { mutableStateOf(quest?.type ?: QuestType.WEEKLY) }
    var description by remember { mutableStateOf(quest?.description ?: "") }
    var targetValueString by remember { mutableStateOf(quest?.targetValue?.toInt()?.toString() ?: "10") }
    var xpRewardString by remember { mutableStateOf(quest?.xpReward?.toString() ?: "50") }
    var coinRewardString by remember { mutableStateOf(quest?.coinReward?.toString() ?: "15") }
    var chest by remember { mutableStateOf(quest?.chest ?: RewardChest.NONE) }
    var targetType by remember { mutableStateOf(quest?.targetType ?: "Tasks") }
    var durationText by remember { mutableStateOf(quest?.durationText ?: "This week") }

    var currentTagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(quest?.tags ?: emptyList()) }

    val targetTypes = listOf("Tasks", "Habits", "Water", "Sleep", "Reading", "XP Gained", "Level", "Streak")
    var showTargetDropdown by remember { mutableStateOf(false) }

    val chests = listOf(RewardChest.NONE, RewardChest.SILVER, RewardChest.GOLD, RewardChest.EPIC)
    var showChestDropdown by remember { mutableStateOf(false) }

    val durations = listOf("This week", "This month", "No limit")
    var showDurationDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack(if (quest == null) "overview" else "details") }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }

            Text(
                text = if (quest == null) "Create Quest" else "Edit Quest",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    val finalTarget = targetValueString.toFloatOrNull() ?: 10f
                    val finalXp = xpRewardString.toIntOrNull() ?: 50
                    val finalCoins = coinRewardString.toIntOrNull() ?: 15
                    if (title.isNotEmpty()) {
                        onConfirm(title, type, description, finalTarget, finalXp, finalCoins, chest, targetType, tags, durationText)
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text(
                    text = "Save",
                    color = if (title.isNotEmpty()) Color(0xFF00F0FF) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Quest Name", color = Color(0xFF9CA3AF)) },
            placeholder = { Text("e.g. Read 1000 Pages") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF8D6EFD),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TYPE", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val types = QuestType.values()
                types.forEach { t ->
                    val isSelected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color(0xFF8D6EFD) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                type = t
                                durationText = when (t) {
                                    QuestType.MAIN -> "No limit"
                                    QuestType.WEEKLY -> "This week"
                                    QuestType.MONTHLY -> "This month"
                                    QuestType.SPECIAL -> "No limit"
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = t.name,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)", color = Color(0xFF9CA3AF)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF8D6EFD),
                unfocusedBorderColor = Color(0xFF334155),
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TARGET CONDITIONS", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = targetValueString,
                    onValueChange = { targetValueString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Target Value", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .clickable { showTargetDropdown = !showTargetDropdown }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = targetType, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showTargetDropdown,
                        onDismissRequest = { showTargetDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        targetTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(text = t, color = Color.White) },
                                onClick = {
                                    targetType = t
                                    showTargetDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "TAGS COUNTED (Match Tasks/Habits tags)", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF8D6EFD).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = tag, color = Color.White, fontSize = 12.sp)
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Tag",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { tags = tags.filter { it != tag } }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentTagInput,
                    onValueChange = { currentTagInput = it },
                    placeholder = { Text("e.g. Fitness") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        if (currentTagInput.trim().isNotEmpty() && !tags.contains(currentTagInput.trim())) {
                            tags = tags + currentTagInput.trim()
                            currentTagInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "REWARD AMOUNTS", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = xpRewardString,
                    onValueChange = { xpRewardString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("XP Reward", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = coinRewardString,
                    onValueChange = { coinRewardString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Coins Reward", color = Color(0xFF9CA3AF)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8D6EFD),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f),
                        unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "CHEST REWARD", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .clickable { showChestDropdown = !showChestDropdown }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chest != RewardChest.NONE) {
                            Icon(
                                imageVector = Icons.Default.Redeem,
                                contentDescription = "Chest",
                                tint = when (chest) {
                                    RewardChest.SILVER -> Color(0xFFC0C0C0)
                                    RewardChest.GOLD -> Color(0xFFFFD700)
                                    RewardChest.EPIC -> Color(0xFF9333EA)
                                    else -> Color.White
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(text = "${chest.name} Chest", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showChestDropdown,
                    onDismissRequest = { showChestDropdown = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    chests.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(text = "${c.name} Chest", color = Color.White) },
                            onClick = {
                                chest = c
                                showChestDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "DURATION", color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                    .clickable { showDurationDropdown = !showDurationDropdown }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = durationText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showDurationDropdown,
                    onDismissRequest = { showDurationDropdown = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    durations.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(text = d, color = Color.White) },
                            onClick = {
                                durationText = d
                                showDurationDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

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

@Composable
fun ProfileScreenContent(userStats: UserStats, onResetAll: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Settings",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFF2C1E5C), CircleShape)
                .border(2.dp, Color(0xFF9F75FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Text(
            text = userStats.name,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Level ${userStats.level} Game Master",
            color = Color(0xFF8D6EFD),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Coins Accumulated", color = Color(0xFF9CA3AF))
                    Text("${userStats.coins} Coins", color = Color.White, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(color = Color(0xFF1D1F30))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active Streak", color = Color(0xFF9CA3AF))
                    Text("${userStats.streak} Days", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onResetAll,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Game & Stats", color = Color.White)
        }
    }
}

// ---------------------------------------------------------------------------------
// BOTTOM NAVIGATION BAR
// ---------------------------------------------------------------------------------
@Composable
fun BottomNavBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
                .height(64.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // List of bottom tabs, with Profile completely removed as requested
                val tabs = listOf(
                    Triple(AppTab.HOME, "Home", Icons.Default.Home),
                    Triple(AppTab.TASKS, "Tasks", Icons.Default.List),
                    Triple(AppTab.QUESTS, "Quests", Icons.Default.Star),
                    Triple(AppTab.PROGRESS, "Progress", Icons.Default.TrendingUp)
                )

                tabs.forEach { (tab, label, icon) ->
                    val isSelected = selectedTab == tab
                    val activeColor = Color(0xFF8D6EFD)
                    val inactiveColor = Color(0xFF6B7280)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                color = if (isSelected) activeColor else inactiveColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Rounded rectangle active indicator underneath the text
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(3.dp)
                                    .background(
                                        color = if (isSelected) activeColor else Color.Transparent,
                                        shape = RoundedCornerShape(1.5.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// HABITS SUBSCREENS (CHAPTER 5)
// ---------------------------------------------------------------------------------

data class PresetOption(
    val iconName: String,
    val name: String,
    val icon: ImageVector,
    val colorHex: Long,
    val goal: Float,
    val unit: String,
    val step: Float,
    val xp: Int,
    val coins: Int
)

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

// ---------------------------------------------------------------------------------
// MINI PROFILE DATA CLASS & VIEWS
// ---------------------------------------------------------------------------------

data class CustomAvatar(
    val id: Int,
    val icon: ImageVector,
    val name: String,
    val startColor: Color,
    val endColor: Color
)

@Composable
fun AvatarView(
    avatarId: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp,
    borderColor: Color = Color(0xFF8D6EFD)
) {
    val avatars = listOf(
        CustomAvatar(0, Icons.Default.Person, "Gamer", Color(0xFF6366F1), Color(0xFF3B82F6)),
        CustomAvatar(1, Icons.Default.Code, "Coder", Color(0xFF06B6D4), Color(0xFF0891B2)),
        CustomAvatar(2, Icons.Default.Whatshot, "Warrior", Color(0xFFF97316), Color(0xFFEA580C)),
        CustomAvatar(3, Icons.Default.FitnessCenter, "Athlete", Color(0xFF10B981), Color(0xFF059669)),
        CustomAvatar(4, Icons.Default.School, "Scholar", Color(0xFFEC4899), Color(0xFFD946EF)),
        CustomAvatar(5, Icons.Default.Star, "Champion", Color(0xFFF59E0B), Color(0xFFD97706)),
        CustomAvatar(6, Icons.Default.Favorite, "Guardian", Color(0xFFEF4444), Color(0xFFDC2626)),
        CustomAvatar(7, Icons.Default.Lightbulb, "Inventor", Color(0xFF14B8A6), Color(0xFF0D9488))
    )
    val avatar = avatars.getOrElse(avatarId) { avatars[0] }

    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(colors = listOf(avatar.startColor, avatar.endColor)),
                shape = CircleShape
            )
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = avatar.icon,
            contentDescription = avatar.name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    label: String,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (textColor == Color.White) Color(0xFF8D6EFD) else textColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(16.dp)
            )
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
fun AboutSubDialog(onDismiss: () -> Unit) {
    var selectedChapterId by remember { mutableStateOf<Int?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12131F)),
            border = BorderStroke(1.dp, Color(0xFF1D1F30)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedChapterId == null) {
                    // Title Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.linearGradient(colors = listOf(Color(0xFF8D6EFD), Color(0xFF00F0FF))),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "LevelUp Bible",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Версія 1.0 (MVP)",
                                    color = Color(0xFF8D6EFD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Game Your Tasks, Rule Your Life!\nLevelUp Bible turns your daily routines and critical goals into real RPG-style quests. Earn experience, gain levels, collect gold, unlock epic chests, equip custom titles, and unleash your ultimate developer power.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color(0xFF1D1F30))

                    Text(
                        text = "ЗМІСТ КНИГИ",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    // Scrollable List of Chapters
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Chapter 1
                        ChapterItem(
                            number = 1,
                            title = "Основи LevelUp",
                            subtitle = "RPG концепція у реальному житті",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 1 }
                        )

                        // Chapter 2
                        ChapterItem(
                            number = 2,
                            title = "Звички та Серії",
                            subtitle = "Формування сили волі та серій",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 2 }
                        )

                        // Chapter 3
                        ChapterItem(
                            number = 3,
                            title = "Квести та Нагороди",
                            subtitle = "Як правильно балансувати XP та золото",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 3 }
                        )

                        // Chapter 10 (Featured Flutter Architecture!)
                        ChapterItem(
                            number = 10,
                            title = "Flutter Architecture",
                            subtitle = "Clean Architecture, структури та правила",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 10 }
                        )

                        // Chapter 11 (Featured Design System!)
                        ChapterItem(
                            number = 11,
                            title = "Design System",
                            subtitle = "Стиль, кольори, типографіка та компоненти",
                            isUnlocked = true,
                            isFeatured = false,
                            onClick = { selectedChapterId = 11 }
                        )

                        // Chapter 12 (Featured Release Plan!)
                        ChapterItem(
                            number = 12,
                            title = "Release Plan",
                            subtitle = "План розробки, тестування та релізу MVP",
                            isUnlocked = true,
                            isFeatured = true,
                            onClick = { selectedChapterId = 12 }
                        )

                        // Lock chapters in between
                        LockedChapterItem(number = 4, title = "Гейм-баланс екранів")
                        LockedChapterItem(number = 5, title = "Економіка та Скрині")
                        LockedChapterItem(number = 6, title = "Магазин та Титули")
                        LockedChapterItem(number = 7, title = "Звуковий супровід")
                        LockedChapterItem(number = 8, title = "Локальне збереження Room")
                        LockedChapterItem(number = 9, title = "Керування станом StateFlow")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8D6EFD)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Закрити", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // CHAPTER READING VIEW
                    val chapterId = selectedChapterId!!
                    
                    // Header with back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { selectedChapterId = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад до змісту",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "LevelUp Bible",
                                color = Color(0xFF8D6EFD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (chapterId) {
                                    1 -> "Розділ 1 — Основи LevelUp"
                                    2 -> "Розділ 2 — Звички та Серії"
                                    3 -> "Розділ 3 — Квести та Нагороди"
                                    10 -> "Розділ 10 — Flutter Architecture"
                                    11 -> "Розділ 11 — Design System"
                                    12 -> "Розділ 12 — Release Plan"
                                    else -> "Розділ $chapterId"
                                },
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1D1F30))

                    // Scrollable Chapter Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (chapterId) {
                            1 -> ChapterOneContent()
                            2 -> ChapterTwoContent()
                            3 -> ChapterThreeContent()
                            10 -> ChapterTenContent()
                            11 -> ChapterElevenContent()
                            12 -> ChapterTwelveContent()
                        }
                    }

                    Button(
                        onClick = { selectedChapterId = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D1F30)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("До змісту", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterItem(
    number: Int,
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    isFeatured: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) Color(0xFF1A153B) else Color(0xFF1A1B2E)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF1D1F30)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF2E3147),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isFeatured) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00F0FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "НОВИНКА",
                                color = Color(0xFF00F0FF),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isFeatured) Color(0xFF8D6EFD) else Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LockedChapterItem(number: Int, title: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161724).copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, Color(0xFF1D1F30).copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1F202E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Розділ $number: $title",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Заблоковано — у розробці",
                    color = Color(0xFF9CA3AF).copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ChapterOneContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Призначення гри") {
            Text(
                text = "Перетвори свої щоденні справи у захоплюючу пригоду! Наша місія — допомогти розробникам та ентузіастам підтримувати дисципліну через ігрові механіки RPG.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Головні показники") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "XP (Досвід) — відображає твій загальний прогрес. Отримуй XP за кожну виконану задачу.")
                BulletPoint(text = "Coins (Золото) — валюта для покупки нагород та відкриття легендарних скринь.")
                BulletPoint(text = "Streak (Серія) — показник регулярності. Чим більше днів поспіль ти виконуєш звички, тим вищий множник XP!")
            }
        }
    }
}

@Composable
fun ChapterTwoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Сила Звичок") {
            Text(
                text = "Звички — це твої постійні вміння. Регулярне їх виконання гартує волю твого героя.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Правила серій (Streaks)") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Кожне послідовне виконання звички збільшує лічильник серії на +1.")
                BulletPoint(text = "Якщо пропустити день, серія згасає, а разом з нею і додаткові бонуси.")
                BulletPoint(text = "Утримання серій понад 7 днів відкриває унікальні досягнення та особливі титули!")
            }
        }
    }
}

@Composable
fun ChapterThreeContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BibleSectionCard(title = "Епічні Квести") {
            Text(
                text = "Квести — це твої головні життєві цілі. Вони поділяються на щоденні місії та масштабні епічні квести.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
        BibleSectionCard(title = "Баланс нагород") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Легкі квести приносять 10 XP та 5 монет.")
                BulletPoint(text = "Середні квести приносять 25 XP та 12 монет.")
                BulletPoint(text = "Складні / Епічні квести дарують понад 100 XP, 50 монет та шанс знайти ключ від рідкісної скрині!")
            }
        }
    }
}

@Composable
fun ChapterTenContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 10 — Flutter Architecture",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "10.1 Призначення") {
            Text(
                text = "Опис архітектури Flutter-проєкту, структури папок та взаємодії між шарами.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "10.2 Архітектурний підхід") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Clean Architecture (спрощена)")
                BulletPoint(text = "Feature-first")
                BulletPoint(text = "Repository Pattern")
                BulletPoint(text = "Service Layer")
                BulletPoint(text = "Dependency Injection")
            }
        }

        BibleSectionCard(title = "10.3 Структура") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "lib/core", desc = "спільне ядро, константи, базові класи")
                CodePathItem(path = "lib/features", desc = "фічі (функціональні модулі)")
                CodePathItem(path = "lib/shared", desc = "перевикористовувані компоненти та віджети")
                CodePathItem(path = "lib/services", desc = "глобальні сервіси та бізнес-логіка")
                CodePathItem(path = "lib/database", desc = "налаштування локальної БД")
                CodePathItem(path = "lib/models", desc = "глобальні моделі даних")
                CodePathItem(path = "lib/theme", desc = "стилі та теми оформлення")
                CodePathItem(path = "lib/utils", desc = "допоміжні утиліти")
                CodePathItem(path = "lib/widgets", desc = "глобальні UI компоненти")
            }
        }

        BibleSectionCard(title = "10.4 Feature") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "presentation/", desc = "екрани та віджети відображення")
                CodePathItem(path = "domain/", desc = "сутності та бізнес-правила фічі")
                CodePathItem(path = "data/", desc = "джерела даних та репозиторії")
                CodePathItem(path = "widgets/", desc = "локальні віджети фічі")
                CodePathItem(path = "controllers/", desc = "управління станом фічі")
            }
        }

        BibleSectionCard(title = "10.5 State Management") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Riverpod — основне управління станом")
                BulletPoint(text = "Notifier/StateNotifier — реактивна логіка")
                BulletPoint(text = "Один Provider = одна відповідальність")
            }
        }

        BibleSectionCard(title = "10.6 Навігація") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "GoRouter")
                BulletPoint(text = "4 Bottom Tabs")
                BulletPoint(text = "Bottom Sheet для Create і Mini Profile")
            }
        }

        BibleSectionCard(title = "10.7 Repository") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "TaskRepository", desc = "управління задачами")
                CodePathItem(path = "HabitRepository", desc = "управління звичками")
                CodePathItem(path = "QuestRepository", desc = "управління квестами")
                CodePathItem(path = "UserRepository", desc = "профіль користувача")
                CodePathItem(path = "StatisticsRepository", desc = "статистика гравця")
            }
        }

        BibleSectionCard(title = "10.8 Services") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CodePathItem(path = "XPService", desc = "нарахування досвіду та рівні")
                CodePathItem(path = "CoinService", desc = "ігрова валюта та монети")
                CodePathItem(path = "QuestEngine", desc = "обробка та запуск квестів")
                CodePathItem(path = "LevelService", desc = "управління рівнями")
                CodePathItem(path = "AchievementService", desc = "досягнення та нагороди")
                CodePathItem(path = "StreakService", desc = "підрахунок серій та стріків")
            }
        }

        BibleSectionCard(title = "10.9 Бізнес-правила") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "UI не працює напряму з БД.")
                NumberedPoint(number = 2, text = "Логіка знаходиться у Service Layer.")
                NumberedPoint(number = 3, text = "Repository відповідає лише за дані.")
                NumberedPoint(number = 4, text = "Компоненти перевикористовуються.")
            }
        }

        BibleSectionCard(title = "10.10 Definition of Done") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Структура створена.")
                BulletPoint(text = "Riverpod налаштований.")
                BulletPoint(text = "GoRouter налаштований.")
                BulletPoint(text = "Repository і Services створені.")
                BulletPoint(text = "Архітектура готова до MVP.")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B161A)),
            border = BorderStroke(1.dp, Color(0xFF5C2F34)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Developer Notes:\nНе розміщувати бізнес-логіку у віджетах.",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ChapterElevenContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 11 — Design System",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "11.1 Призначення") {
            Text(
                text = "Design System визначає єдиний стиль застосунку та правила використання всіх UI-компонентів.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "11.2 Кольори") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorPreviewItem("Primary", Color(0xFF8D6EFD))
                ColorPreviewItem("Secondary", Color(0xFF00F0FF))
                ColorPreviewItem("Success", Color(0xFF10B981))
                ColorPreviewItem("Warning", Color(0xFFF59E0B))
                ColorPreviewItem("Danger", Color(0xFFEF4444))
                ColorPreviewItem("Background", Color(0xFF0D0E15))
                ColorPreviewItem("Surface", Color(0xFF1E2030))
                ColorPreviewItem("Card", Color(0xFF1A1B2E))
                ColorPreviewItem("Divider", Color(0xFF1D1F30))
                ColorPreviewItem("Text Primary", Color(0xFFFFFFFF))
                ColorPreviewItem("Text Secondary", Color(0xFF9CA3AF))
            }
        }

        BibleSectionCard(title = "11.3 Типографіка") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TypographyPreviewItem("Display", "Epic Title", 26.sp, FontWeight.ExtraBold)
                TypographyPreviewItem("H1", "Header Level 1", 20.sp, FontWeight.Bold)
                TypographyPreviewItem("H2", "Header Level 2", 17.sp, FontWeight.SemiBold)
                TypographyPreviewItem("H3", "Header Level 3", 14.sp, FontWeight.Medium)
                TypographyPreviewItem("Body", "Standard readable text content.", 13.sp, FontWeight.Normal)
                TypographyPreviewItem("Caption", "Additional small meta information.", 11.sp, FontWeight.Light)
                TypographyPreviewItem("Button", "CLICK ME", 12.sp, FontWeight.Bold)
            }
        }

        BibleSectionCard(title = "11.4 Відступи") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpacingPreviewItem("4 px", 4.dp)
                SpacingPreviewItem("8 px", 8.dp)
                SpacingPreviewItem("12 px", 12.dp)
                SpacingPreviewItem("16 px", 16.dp)
                SpacingPreviewItem("24 px", 24.dp)
                SpacingPreviewItem("32 px", 32.dp)
            }
        }

        BibleSectionCard(title = "11.5 Радіуси") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RadiusPreviewItem("Small — 8 px", 8.dp)
                RadiusPreviewItem("Medium — 12 px", 12.dp)
                RadiusPreviewItem("Large — 16 px", 16.dp)
                RadiusPreviewItem("Extra Large — 24 px", 24.dp)
            }
        }

        BibleSectionCard(title = "11.6 Компоненти") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Primary Button — основні CTA кнопки")
                BulletPoint("Secondary Button — додаткові кнопки")
                BulletPoint("Task Card — картка задачі")
                BulletPoint("Habit Card — картка звички")
                BulletPoint("Quest Card — картка квесту")
                BulletPoint("Statistic Card — картка статистики")
                BulletPoint("Progress Bar — смужка прогресу")
                BulletPoint("XP Badge — бейдж досвіду")
                BulletPoint("Coin Badge — бейдж монет")
                BulletPoint("Priority Chip — чіп пріоритету")
                BulletPoint("Bottom Navigation — нижня навігація")
                BulletPoint("Bottom Sheet — нижня шторка")
                BulletPoint("Dialog — діалогове вікно")
                BulletPoint("Snackbar — спливаюче повідомлення")
            }
        }

        BibleSectionCard(title = "11.7 Іконки") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Material Symbols — єдине джерело іконок")
                BulletPoint("Єдиний стиль іконок у всьому застосунку")
            }
        }

        BibleSectionCard(title = "11.8 Анімації") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Тривалість: 200–300 ms")
                BulletPoint("Крива: Ease In Out (плавні переходи)")
                BulletPoint("Без надлишкових ефектів")
            }
        }

        BibleSectionCard(title = "11.9 Бізнес-правила") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "Усі екрани використовують спільні компоненти.")
                NumberedPoint(number = 2, text = "Не створювати дублікати компонентів.")
                NumberedPoint(number = 3, text = "Нові компоненти додаються лише після затвердження.")
                NumberedPoint(number = 4, text = "Усі кольори беруться лише з Theme.")
            }
        }

        BibleSectionCard(title = "11.10 Definition of Done") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint("Створено Theme.")
                BulletPoint("Створено базові компоненти.")
                BulletPoint("Усі екрани використовують Design System.")
                BulletPoint("Відсутні локальні стилі.")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B161A)),
            border = BorderStroke(1.dp, Color(0xFF5C2F34)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Developer Notes:\nБудь-які зміни дизайну виконуються через Design System, а не окремо в кожному екрані.",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ColorPreviewItem(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
        )
        Text(text = name, color = Color(0xFFD1D5DB), fontSize = 12.sp)
    }
}

@Composable
fun TypographyPreviewItem(name: String, sample: String, size: TextUnit, weight: FontWeight) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$name ($size):",
            color = Color(0xFF00F0FF),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = sample,
            color = Color.White,
            fontSize = size,
            fontWeight = weight,
            maxLines = 1
        )
    }
}

@Composable
fun SpacingPreviewItem(name: String, widthDp: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            color = Color(0xFF8D6EFD),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Box(
            modifier = Modifier
                .height(8.dp)
                .width(widthDp)
                .background(Color(0xFF8D6EFD).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        Text(
            text = "${widthDp.value.toInt()} dp",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp
        )
    }
}

@Composable
fun RadiusPreviewItem(name: String, radiusDp: Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            color = Color(0xFF8D6EFD),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF1E2030), RoundedCornerShape(radiusDp))
                .border(1.5.dp, Color(0xFF00F0FF), RoundedCornerShape(radiusDp))
        )
        Text(
            text = "${radiusDp.value.toInt()} dp",
            color = Color(0xFF9CA3AF),
            fontSize = 11.sp
        )
    }
}

@Composable
fun ChapterTwelveContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
            border = BorderStroke(1.dp, Color(0xFF3B2F5C)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Розділ 12 — Release Plan",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF8D6EFD).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Версія 1.0",
                            color = Color(0xFF8D6EFD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Статус: MVP",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        BibleSectionCard(title = "12.1 Мета") {
            Text(
                text = "План релізу визначає порядок розробки, тестування та публікації MVP у Google Play.",
                color = Color(0xFFD1D5DB),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        BibleSectionCard(title = "12.2 Етапи") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberedPoint(number = 1, text = "Налаштування проєкту")
                NumberedPoint(number = 2, text = "Реалізація бази даних")
                NumberedPoint(number = 3, text = "Реалізація сервісів")
                NumberedPoint(number = 4, text = "Реалізація UI-компонентів")
                NumberedPoint(number = 5, text = "Реалізація екранів")
                NumberedPoint(number = 6, text = "Тестування")
                NumberedPoint(number = 7, text = "Публікація MVP")
            }
        }

        BibleSectionCard(title = "12.3 Пріоритет реалізації") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "1. Home (Головна панель)")
                BulletPoint(text = "2. Tasks (Задачі та квести)")
                BulletPoint(text = "3. Habits (Корисні звички)")
                BulletPoint(text = "4. Quests (Масштабні пригоди)")
                BulletPoint(text = "5. Progress (Рівень, досвід та золото)")
                BulletPoint(text = "6. Mini Profile (Картка профілю та титули)")
            }
        }

        BibleSectionCard(title = "12.4 MVP Checklist") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CheckedBulletPoint(text = "Усі екрани реалізовані")
                CheckedBulletPoint(text = "База даних працює")
                CheckedBulletPoint(text = "Quest Engine працює")
                CheckedBulletPoint(text = "XP та Coins працюють")
                CheckedBulletPoint(text = "Навігація працює")
                CheckedBulletPoint(text = "Відсутні критичні помилки")
            }
        }

        BibleSectionCard(title = "12.5 Тестування") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Перевірка бізнес-логіки")
                BulletPoint(text = "Перевірка UI")
                BulletPoint(text = "Перевірка продуктивності")
                BulletPoint(text = "Перевірка збереження даних")
                BulletPoint(text = "Регресійне тестування")
            }
        }

        BibleSectionCard(title = "12.6 Критерії готовності") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BulletPoint(text = "Стабільна робота без падінь")
                BulletPoint(text = "Усі функції MVP реалізовані")
                BulletPoint(text = "Інтерфейс відповідає Design System")
                BulletPoint(text = "Документація актуальна")
            }
        }
    }
}

@Composable
fun CheckedBulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Checked",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun BibleSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2030)),
        border = BorderStroke(1.dp, Color(0xFF2E3147)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = Color(0xFF00F0FF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            content()
        }
    }
}

@Composable
fun BulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "•", color = Color(0xFF8D6EFD), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun NumberedPoint(number: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "$number.", color = Color(0xFF8D6EFD), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFFD1D5DB), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun CodePathItem(path: String, desc: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF0D0E15), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = path,
                color = Color(0xFF00F0FF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Text(text = "— $desc", color = Color(0xFF9CA3AF), fontSize = 11.sp, lineHeight = 14.sp)
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

