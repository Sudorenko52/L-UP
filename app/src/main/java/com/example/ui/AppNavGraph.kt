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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.navigation.Routes
import com.example.navigation.NavigationActions
import com.example.navigation.BottomBar
import com.example.shared.components.dialogs.TaskDialog
import com.example.shared.components.dialogs.HabitDialog
import com.example.shared.components.bottomsheet.MiniProfileBottomSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.features.profile.ProfileScreenContent

@Composable
fun AppNavGraph() {
    val homeViewModel: HomeViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()
    val habitViewModel: HabitViewModel = viewModel()
    val questViewModel: QuestViewModel = viewModel()
    val progressViewModel: ProgressViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val taskState by taskViewModel.uiState.collectAsStateWithLifecycle()
    val habitState by habitViewModel.uiState.collectAsStateWithLifecycle()
    val questState by questViewModel.uiState.collectAsStateWithLifecycle()
    val progressState by progressViewModel.uiState.collectAsStateWithLifecycle()
    val userState by userViewModel.uiState.collectAsStateWithLifecycle()

    val userStats = homeState.userStats
    val mainQuest = homeState.mainQuest
    val priorities = taskState.priorities
    val habits = habitState.habits
    val quests = questState.quests
    val todayXpEarned = homeState.todayXpEarned
    val todayCoinsEarned = homeState.todayCoinsEarned

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

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF090A0F),
        bottomBar = {
            if (currentSubScreen == null || currentSubScreen == "habits" || currentSubScreen == "weekly_list" || currentSubScreen == "monthly_list" || currentSubScreen == "special_list") {
                BottomBar(navController = navController)
            }
        },
        floatingActionButton = {
            if ((currentRoute == Routes.HOME || currentRoute == Routes.TASKS) && currentSubScreen == null) {
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

            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.HOME) {
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
                                    habitViewModel.toggleHabitCompletion(id, completed)
                                },
                                onMoveUp = { habitViewModel.moveHabitUp(it) },
                                onMoveDown = { habitViewModel.moveHabitDown(it) }
                            )
                        }
                        "add_habit" -> {
                            AddHabitScreen(
                                onBack = { currentSubScreen = "habits" },
                                onConfirm = { title, icon, color, goal, unit, step, desc, repeat, xp, coins, penalty, tags ->
                                    habitViewModel.addHabit(title, icon, color, goal, unit, step, desc, repeat, xp, coins, penalty, tags)
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
                                        habitViewModel.updateHabit(habit.id, title, icon, color, currentValue, goal, unit, step, desc, repeat, xp, coins, penalty, tags, completed)
                                        currentSubScreen = "habits"
                                    },
                                    onDelete = {
                                        habitViewModel.deleteHabit(habit.id)
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
                                onPriorityToggle = { homeViewModel.togglePriority(it) },
                                onHabitClick = { id ->
                                    homeViewModel.incrementHabit(id)
                                },
                                onDeletePriority = { homeViewModel.deletePriorityTask(it) },
                                onToggleDeleteMode = { isDeleteMode = !isDeleteMode },
                                onClaimQuest = { homeViewModel.claimMainQuest() },
                                onAddHabitClick = { currentSubScreen = "habits" },
                                onEditMainQuestClick = { showMainQuestDialog = true },
                                onProfileClick = { showMiniProfile = true }
                            )
                        }
                    }
                }
                composable(Routes.TASKS) {
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
                            taskViewModel.updateTaskProgress(id, progress)
                        }
                    )
                }
                composable(Routes.QUESTS) {
                    QuestsScreenContent(
                        viewModel = questViewModel,
                        currentSubScreen = currentSubScreen,
                        onSubScreenChanged = { currentSubScreen = it },
                        selectedQuestId = selectedQuestId,
                        onSelectedQuestIdChanged = { selectedQuestId = it },
                        onEditMainQuestClick = { showMainQuestDialog = true }
                    )
                }
                composable(Routes.PROGRESS) {
                    ProgressScreenContent(
                        userStats = userStats,
                        priorities = priorities,
                        habits = habits,
                        quests = quests,
                        todayXp = todayXpEarned,
                        todayCoins = todayCoinsEarned
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreenContent(
                        userStats = userStats,
                        onResetAll = { homeViewModel.resetAll() }
                    )
                }
            }

            // Dialog for adding or editing a task
            if (showTaskDialog) {
                TaskDialog(
                    task = taskToEdit,
                    onDismiss = { showTaskDialog = false },
                    onConfirm = { title, level, icon, desc, xp, coins, time, date, progress, tags, repeat ->
                        if (taskToEdit == null) {
                            taskViewModel.addPriorityTask(title, level, icon, desc, xp, coins, time, date, progress, tags, repeat)
                        } else {
                            taskViewModel.updatePriorityTask(taskToEdit!!.id, title, level, icon, desc, xp, coins, time, date, progress, tags, repeat)
                        }
                        showTaskDialog = false
                    },
                    onDelete = taskToEdit?.let { task ->
                        {
                            taskViewModel.deletePriorityTask(task.id)
                            showTaskDialog = false
                        }
                    }
                )
            }

            // Dialog for adding a new habit
            if (showAddHabitDialog) {
                HabitDialog(
                    onDismiss = { showAddHabitDialog = false },
                    onConfirm = { title, category, goal, currentValue, iconName, progress ->
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
                        questViewModel.updateMainQuest(title, description, targetValue, currentValue, xpReward, coinReward, chest)
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
                    onUpdateName = { userViewModel.updateProfileName(it) },
                    onUpdateAvatar = { userViewModel.updateAvatar(it) },
                    onUpdateTitle = { userViewModel.updateSelectedTitle(it) },
                    onResetStats = {
                        homeViewModel.resetAll()
                        showMiniProfile = false
                    }
                )
            }
        }
    }
}
