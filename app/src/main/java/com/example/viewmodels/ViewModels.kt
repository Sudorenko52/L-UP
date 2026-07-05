package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.UserStats
import com.example.PriorityTask
import com.example.Habit
import com.example.Quest
import com.example.MainQuest
import com.example.AppTab
import com.example.PriorityLevel
import com.example.QuestType
import com.example.QuestStatus
import com.example.RewardChest
import com.example.database.AppDatabase
import com.example.database.TaskRepository
import com.example.database.HabitRepository
import com.example.database.QuestRepository
import com.example.database.UserRepository
import com.example.database.StatisticsRepository
import com.example.database.ActivityLogEntity
import com.example.services.XPService
import com.example.services.CoinService
import com.example.services.LevelService
import com.example.services.StreakService
import com.example.services.AchievementService
import com.example.services.QuestEngine
import com.example.services.GamificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================================
// UI STATES
// =====================================================================================

data class HomeUiState(
    val userStats: UserStats = UserStats(),
    val mainQuest: MainQuest = MainQuest(),
    val priorities: List<PriorityTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val quests: List<Quest> = emptyList(),
    val selectedTab: AppTab = AppTab.HOME,
    val todayXpEarned: Int = 65,
    val todayCoinsEarned: Int = 18
)

data class TaskUiState(
    val priorities: List<PriorityTask> = emptyList(),
    val showTaskDialog: Boolean = false,
    val taskToEdit: PriorityTask? = null,
    val showDeleteDialog: Boolean = false,
    val taskToDeleteId: String? = null
)

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val showHabitDialog: Boolean = false,
    val habitToEditId: String? = null,
    val showDeleteDialog: Boolean = false,
    val habitToDeleteId: String? = null
)

data class QuestUiState(
    val quests: List<Quest> = emptyList(),
    val userStats: UserStats = UserStats(),
    val showMainQuestDialog: Boolean = false
)

data class ProgressUiState(
    val userStats: UserStats = UserStats(),
    val allLogs: List<ActivityLogEntity> = emptyList(),
    val priorities: List<PriorityTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val quests: List<Quest> = emptyList()
)

data class UserUiState(
    val userStats: UserStats = UserStats(),
    val showMiniProfile: Boolean = false
)

// =====================================================================================
// BASE SERVICE CONFIGURATION HELPER
// =====================================================================================
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val db = AppDatabase.getDatabase(application)
    protected val taskRepository = TaskRepository(db)
    protected val habitRepository = HabitRepository(db)
    protected val questRepository = QuestRepository(db)
    protected val userRepository = UserRepository(db)
    protected val statisticsRepository = StatisticsRepository(db)

    // Services
    protected val xpService = XPService()
    protected val coinService = CoinService()
    protected val levelService = LevelService()
    protected val streakService = StreakService()
    protected val achievementService = AchievementService()
    protected val questEngine = QuestEngine(
        questRepository = questRepository,
        userRepository = userRepository,
        statisticsRepository = statisticsRepository,
        xpService = xpService,
        coinService = coinService,
        levelService = levelService
    )
    protected val gamificationService = GamificationService(
        taskRepository = taskRepository,
        habitRepository = habitRepository,
        questRepository = questRepository,
        userRepository = userRepository,
        statisticsRepository = statisticsRepository,
        xpService = xpService,
        coinService = coinService,
        levelService = levelService,
        streakService = streakService,
        achievementService = achievementService,
        questEngine = questEngine
    )
}

// =====================================================================================
// VIEWMODELS
// =====================================================================================

class HomeViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(
                        userStats = stats,
                        todayXpEarned = stats.todayXpEarned,
                        todayCoinsEarned = stats.todayCoinsEarned
                    )}
                } else {
                    userRepository.saveUserStats(UserStats())
                }
            }
        }
        viewModelScope.launch {
            taskRepository.allTasksFlow.collect { list ->
                _uiState.update { it.copy(priorities = list) }
            }
        }
        viewModelScope.launch {
            habitRepository.allHabitsFlow.collect { list ->
                _uiState.update { it.copy(habits = list) }
            }
        }
        viewModelScope.launch {
            questRepository.allQuestsFlow.collect { list ->
                _uiState.update { it.copy(quests = list) }
                list.find { it.type == QuestType.MAIN }?.let { mq ->
                    _uiState.update { it.copy(
                        mainQuest = MainQuest(
                            title = mq.title,
                            progress = if (mq.targetValue > 0) mq.currentValue / mq.targetValue else 0f,
                            xpReward = mq.xpReward,
                            coinReward = mq.coinReward
                        )
                    )}
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun togglePriority(id: String) {
        viewModelScope.launch {
            gamificationService.toggleTask(id)
        }
    }

    fun incrementHabit(id: String) {
        viewModelScope.launch {
            gamificationService.incrementHabit(id)
        }
    }

    fun toggleHabitCompletion(id: String, completed: Boolean) {
        viewModelScope.launch {
            gamificationService.toggleHabitCompletion(id, completed)
        }
    }

    fun deletePriorityTask(id: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }

    fun claimMainQuest() {
        viewModelScope.launch {
            val quest = _uiState.value.quests.find { it.type == QuestType.MAIN && it.status == QuestStatus.ACTIVE } ?: return@launch
            gamificationService.claimQuest(quest.id)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            db.clearAllTables()
            userRepository.saveUserStats(UserStats())
            _uiState.update { HomeUiState() }
        }
    }
}

class TaskViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var hasSeededTasks = false

    init {
        viewModelScope.launch {
            taskRepository.allTasksFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededTasks) {
                    _uiState.update { it.copy(priorities = list) }
                } else {
                    seedDefaultTasks()
                }
            }
        }
    }

    private fun seedDefaultTasks() {
        if (hasSeededTasks) return
        hasSeededTasks = true
        viewModelScope.launch {
            val defaults = listOf(
                PriorityTask(
                    id = "c1",
                    title = "Drink Water",
                    level = PriorityLevel.NORMAL,
                    iconName = "Water",
                    completed = true,
                    xpReward = 10,
                    coinReward = 2,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Hydration", "Daily")
                ),
                PriorityTask(
                    id = "c2",
                    title = "Stretch",
                    level = PriorityLevel.NORMAL,
                    iconName = "Gym",
                    completed = true,
                    xpReward = 10,
                    coinReward = 2,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Fitness")
                ),
                PriorityTask(
                    id = "c3",
                    title = "Take Vitamins",
                    level = PriorityLevel.NORMAL,
                    iconName = "General",
                    completed = true,
                    xpReward = 10,
                    coinReward = 2,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Health")
                ),
                PriorityTask(
                    id = "c4",
                    title = "Healthy Breakfast",
                    level = PriorityLevel.NORMAL,
                    iconName = "General",
                    completed = true,
                    xpReward = 15,
                    coinReward = 3,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Food")
                ),
                PriorityTask(
                    id = "c5",
                    title = "Review Daily Goals",
                    level = PriorityLevel.IMPORTANT,
                    iconName = "Work",
                    completed = true,
                    xpReward = 15,
                    coinReward = 3,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Focus")
                ),
                PriorityTask(
                    id = "c6",
                    title = "Read Morning News",
                    level = PriorityLevel.NORMAL,
                    iconName = "Book",
                    completed = true,
                    xpReward = 15,
                    coinReward = 3,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Learning")
                ),
                PriorityTask(
                    id = "c7",
                    title = "Quick Walk",
                    level = PriorityLevel.NORMAL,
                    iconName = "Gym",
                    completed = true,
                    xpReward = 20,
                    coinReward = 4,
                    date = "Today",
                    progress = 1.0f,
                    tags = listOf("Fitness")
                ),
                PriorityTask(
                    id = "1",
                    title = "Gym",
                    level = PriorityLevel.CRITICAL,
                    iconName = "Gym",
                    completed = false,
                    xpReward = 30,
                    coinReward = 5,
                    time = "18:00",
                    date = "Today",
                    progress = 0.67f,
                    tags = listOf("Fitness", "Health")
                ),
                PriorityTask(
                    id = "2",
                    title = "Read 30 pages",
                    level = PriorityLevel.IMPORTANT,
                    iconName = "Book",
                    completed = false,
                    xpReward = 20,
                    coinReward = 3,
                    time = "",
                    date = "Today",
                    progress = 0.50f,
                    tags = listOf("Reading", "Learning")
                ),
                PriorityTask(
                    id = "3",
                    title = "Clean Desk",
                    level = PriorityLevel.NORMAL,
                    iconName = "Clean",
                    completed = false,
                    xpReward = 10,
                    coinReward = 2,
                    time = "",
                    date = "Today",
                    progress = 0.80f,
                    tags = listOf("Home")
                ),
                PriorityTask(
                    id = "4",
                    title = "Study Flutter",
                    level = PriorityLevel.NORMAL,
                    iconName = "Code",
                    completed = false,
                    xpReward = 15,
                    coinReward = 3,
                    time = "20:00",
                    date = "Tomorrow",
                    progress = 0.35f,
                    tags = listOf("Learning", "Work")
                )
            )
            for (t in defaults) {
                taskRepository.insertTask(t)
            }
        }
    }

    fun togglePriority(id: String) {
        viewModelScope.launch {
            gamificationService.toggleTask(id)
        }
    }

    fun updateTaskProgress(id: String, newProgress: Float) {
        viewModelScope.launch {
            gamificationService.updateTaskProgress(id, newProgress)
        }
    }

    fun addPriorityTask(
        title: String,
        level: PriorityLevel,
        iconName: String,
        description: String = "",
        xpReward: Int = 10,
        coinReward: Int = 2,
        time: String = "",
        date: String = "Today",
        progress: Float = 0f,
        tags: List<String> = emptyList(),
        repeat: String = "Once"
    ) {
        val newId = System.currentTimeMillis().toString()
        val isCompleted = progress >= 1.0f
        val newTask = PriorityTask(
            id = newId,
            title = title,
            level = level,
            iconName = iconName,
            completed = isCompleted,
            description = description,
            xpReward = xpReward,
            coinReward = coinReward,
            time = time,
            date = date,
            progress = progress,
            tags = tags,
            repeat = repeat
        )
        viewModelScope.launch {
            gamificationService.addTask(newTask)
        }
    }

    fun updatePriorityTask(
        id: String,
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
    ) {
        viewModelScope.launch {
            val task = _uiState.value.priorities.find { it.id == id } ?: return@launch
            val isNowCompleted = progress >= 1.0f
            val updatedTask = task.copy(
                title = title,
                level = level,
                iconName = iconName,
                description = description,
                xpReward = xpReward,
                coinReward = coinReward,
                time = time,
                date = date,
                progress = progress,
                tags = tags,
                repeat = repeat,
                completed = isNowCompleted
            )
            gamificationService.updateTask(updatedTask, task)
        }
    }

    fun deletePriorityTask(id: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }

    fun openTaskDialog(task: PriorityTask? = null) {
        _uiState.update { it.copy(showTaskDialog = true, taskToEdit = task) }
    }

    fun closeTaskDialog() {
        _uiState.update { it.copy(showTaskDialog = false, taskToEdit = null) }
    }

    fun openDeleteDialog(id: String) {
        _uiState.update { it.copy(showDeleteDialog = true, taskToDeleteId = id) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, taskToDeleteId = null) }
    }
}

class HabitViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private var hasSeededHabits = false

    init {
        viewModelScope.launch {
            habitRepository.allHabitsFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededHabits) {
                    _uiState.update { it.copy(habits = list) }
                } else {
                    seedDefaultHabits()
                }
            }
        }
    }

    private fun seedDefaultHabits() {
        if (hasSeededHabits) return
        hasSeededHabits = true
        viewModelScope.launch {
            val defaults = listOf(
                Habit(
                    id = "h1",
                    title = "Water intake",
                    iconName = "Water",
                    colorHex = 0xFF00F0FF,
                    currentValue = 1200f,
                    maxValue = 2500f,
                    unit = "ml",
                    subtitle = "48%",
                    completed = false,
                    incrementStep = 250f,
                    description = "Stay hydrated, 2.5L daily target",
                    repeat = "Every Day",
                    xpReward = 15,
                    coinReward = 3,
                    isPenaltyEnabled = true,
                    tags = listOf("Health", "Hydration"),
                    createdAt = System.currentTimeMillis()
                ),
                Habit(
                    id = "h2",
                    title = "Daily reading",
                    iconName = "Book",
                    colorHex = 0xFF8D6EFD,
                    currentValue = 15f,
                    maxValue = 30f,
                    unit = "mins",
                    subtitle = "50%",
                    completed = false,
                    incrementStep = 5f,
                    description = "Expand your horizons with self-growth reading",
                    repeat = "Every Day",
                    xpReward = 20,
                    coinReward = 4,
                    isPenaltyEnabled = false,
                    tags = listOf("Learning"),
                    createdAt = System.currentTimeMillis() - 1000
                ),
                Habit(
                    id = "h3",
                    title = "Sleeping hours",
                    iconName = "Sleep",
                    colorHex = 0xFFFF70A6,
                    currentValue = 7.5f,
                    maxValue = 8.0f,
                    unit = "hours",
                    subtitle = "Good",
                    completed = false,
                    incrementStep = 0.5f,
                    description = "Aim for consistent 8h sleep schedule",
                    repeat = "Every Day",
                    xpReward = 20,
                    coinReward = 5,
                    isPenaltyEnabled = true,
                    tags = listOf("Health", "Consistency"),
                    createdAt = System.currentTimeMillis() - 2000
                )
            )
            for (h in defaults) {
                habitRepository.insertHabit(h)
            }
        }
    }

    fun incrementHabit(id: String) {
        viewModelScope.launch {
            gamificationService.incrementHabit(id)
        }
    }

    fun toggleHabitCompletion(id: String, completed: Boolean) {
        viewModelScope.launch {
            gamificationService.toggleHabitCompletion(id, completed)
        }
    }

    fun addHabit(
        title: String,
        iconName: String,
        colorHex: Long,
        maxValue: Float,
        unit: String,
        incrementStep: Float,
        description: String,
        repeat: String,
        xpReward: Int,
        coinReward: Int,
        isPenaltyEnabled: Boolean,
        tags: List<String>
    ) {
        val newId = "h_" + System.currentTimeMillis() + "_" + (1..1000).random()
        val newHabit = Habit(
            id = newId,
            title = title,
            iconName = iconName,
            colorHex = colorHex,
            currentValue = 0f,
            maxValue = maxValue,
            unit = unit,
            subtitle = "0%",
            completed = false,
            incrementStep = incrementStep,
            description = description,
            repeat = repeat,
            xpReward = xpReward,
            coinReward = coinReward,
            isPenaltyEnabled = isPenaltyEnabled,
            tags = tags,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            gamificationService.addHabit(newHabit)
        }
    }

    fun updateHabit(
        id: String,
        title: String,
        iconName: String,
        colorHex: Long,
        currentValue: Float,
        maxValue: Float,
        unit: String,
        incrementStep: Float,
        description: String,
        repeat: String,
        xpReward: Int,
        coinReward: Int,
        isPenaltyEnabled: Boolean,
        tags: List<String>,
        completed: Boolean
    ) {
        viewModelScope.launch {
            val habit = _uiState.value.habits.find { it.id == id } ?: return@launch
            val isNowCompleted = completed || (currentValue >= maxValue)
            val percent = if (maxValue > 0) ((currentValue / maxValue) * 100).toInt() else 0
            val nextSubtitle = when (iconName) {
                "Sleep" -> if (currentValue >= 7.0f) "Good" else "Tired"
                "Creatine" -> if (isNowCompleted) "Done" else "Missed"
                else -> if (isNowCompleted) "Done" else "$percent%"
            }

            val updated = habit.copy(
                title = title,
                iconName = iconName,
                colorHex = colorHex,
                currentValue = currentValue,
                maxValue = maxValue,
                unit = unit,
                incrementStep = incrementStep,
                subtitle = nextSubtitle,
                completed = isNowCompleted,
                description = description,
                repeat = repeat,
                xpReward = xpReward,
                coinReward = coinReward,
                isPenaltyEnabled = isPenaltyEnabled,
                tags = tags
            )
            gamificationService.updateHabitDetails(updated, habit)
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(id)
        }
    }

    fun moveHabitUp(id: String) {
        val list = _uiState.value.habits
        val index = list.indexOfFirst { it.id == id }
        if (index <= 0) return
        val itemA = list[index]
        val itemB = list[index - 1]

        viewModelScope.launch {
            habitRepository.updateHabit(itemA.copy(createdAt = itemB.createdAt))
            habitRepository.updateHabit(itemB.copy(createdAt = itemA.createdAt))
        }
    }

    fun moveHabitDown(id: String) {
        val list = _uiState.value.habits
        val index = list.indexOfFirst { it.id == id }
        if (index < 0 || index >= list.size - 1) return
        val itemA = list[index]
        val itemB = list[index + 1]

        viewModelScope.launch {
            habitRepository.updateHabit(itemA.copy(createdAt = itemB.createdAt))
            habitRepository.updateHabit(itemB.copy(createdAt = itemA.createdAt))
        }
    }

    fun openHabitDialog(habitId: String? = null) {
        _uiState.update { it.copy(showHabitDialog = true, habitToEditId = habitId) }
    }

    fun closeHabitDialog() {
        _uiState.update { it.copy(showHabitDialog = false, habitToEditId = null) }
    }

    fun openDeleteDialog(id: String) {
        _uiState.update { it.copy(showDeleteDialog = true, habitToDeleteId = id) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false, habitToDeleteId = null) }
    }
}

class QuestViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState: StateFlow<QuestUiState> = _uiState.asStateFlow()

    private var hasSeededQuests = false

    init {
        viewModelScope.launch {
            questRepository.allQuestsFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededQuests) {
                    _uiState.update { it.copy(quests = list) }
                } else {
                    seedDefaultQuests()
                }
            }
        }
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(userStats = stats) }
                }
            }
        }
    }

    private fun seedDefaultQuests() {
        if (hasSeededQuests) return
        hasSeededQuests = true
        viewModelScope.launch {
            val defaults = listOf(
                Quest(
                    id = "q_main",
                    title = "Become a Flutter Developer",
                    type = QuestType.MAIN,
                    description = "Learn basic widgets, state management, and build 3 full apps",
                    targetValue = 100f,
                    currentValue = 65f,
                    xpReward = 500,
                    coinReward = 200,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Tasks",
                    tags = listOf("Learning"),
                    durationText = "2 weeks remaining"
                ),
                Quest(
                    id = "q1",
                    title = "The Waterboy",
                    type = QuestType.WEEKLY,
                    description = "Log Water intake habit 7 times this week",
                    targetValue = 7f,
                    currentValue = 3f,
                    xpReward = 100,
                    coinReward = 30,
                    chest = RewardChest.SILVER,
                    status = QuestStatus.ACTIVE,
                    targetType = "Water",
                    tags = emptyList(),
                    durationText = "3 days remaining"
                ),
                Quest(
                    id = "q2",
                    title = "Study Monster",
                    type = QuestType.WEEKLY,
                    description = "Complete 5 Study tasks",
                    targetValue = 5f,
                    currentValue = 2f,
                    xpReward = 150,
                    coinReward = 50,
                    chest = RewardChest.GOLD,
                    status = QuestStatus.ACTIVE,
                    targetType = "Study",
                    tags = emptyList(),
                    durationText = "4 days remaining"
                ),
                Quest(
                    id = "q3",
                    title = "Gym Rat Workout",
                    type = QuestType.SPECIAL,
                    description = "Complete Gym tasks or Fitness habits",
                    targetValue = 15f,
                    currentValue = 6f,
                    xpReward = 300,
                    coinReward = 100,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Gym",
                    tags = emptyList(),
                    durationText = "No limit"
                ),
                Quest(
                    id = "q4",
                    title = "Scholarship Journey",
                    type = QuestType.MONTHLY,
                    description = "Read 30 books (pages log)",
                    targetValue = 30f,
                    currentValue = 12f,
                    xpReward = 400,
                    coinReward = 150,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Book",
                    tags = emptyList(),
                    durationText = "19 days remaining"
                )
            )
            for (q in defaults) {
                questRepository.insertQuest(q)
            }
        }
    }

    fun addQuest(
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
    ) {
        val id = "q_" + System.currentTimeMillis() + "_" + (1..1000).random()
        val newQuest = Quest(
            id = id,
            title = title,
            type = type,
            description = description,
            targetValue = targetValue,
            currentValue = 0f,
            xpReward = xpReward,
            coinReward = coinReward,
            chest = chest,
            status = QuestStatus.ACTIVE,
            targetType = targetType,
            tags = tags,
            durationText = durationText
        )
        viewModelScope.launch {
            gamificationService.addQuest(newQuest, _uiState.value.quests)
        }
    }

    fun updateQuest(
        id: String,
        title: String,
        type: QuestType,
        description: String,
        targetValue: Float,
        currentValue: Float,
        xpReward: Int,
        coinReward: Int,
        chest: RewardChest,
        status: QuestStatus,
        targetType: String,
        tags: List<String>,
        durationText: String
    ) {
        viewModelScope.launch {
            val quest = _uiState.value.quests.find { it.id == id } ?: return@launch
            val isNowCompleted = status == QuestStatus.COMPLETED || currentValue >= targetValue
            val finalStatus = if (isNowCompleted) QuestStatus.COMPLETED else status
            val updated = quest.copy(
                title = title,
                type = type,
                description = description,
                targetValue = targetValue,
                currentValue = currentValue,
                xpReward = xpReward,
                coinReward = coinReward,
                chest = chest,
                status = finalStatus,
                targetType = targetType,
                tags = tags,
                durationText = durationText,
                completedAt = if (isNowCompleted && quest.status != QuestStatus.COMPLETED) System.currentTimeMillis() else quest.completedAt
            )
            gamificationService.updateQuestDetails(updated, quest, _uiState.value.quests)
        }
    }

    fun deleteQuest(id: String) {
        viewModelScope.launch {
            questRepository.deleteQuest(id)
        }
    }

    fun completeManualQuest(id: String) {
        viewModelScope.launch {
            gamificationService.claimQuest(id)
        }
    }

    fun updateMainQuest(
        title: String,
        description: String,
        targetValue: Float,
        currentValue: Float,
        xpReward: Int,
        coinReward: Int,
        chest: RewardChest
    ) {
        val existingMainQuest = _uiState.value.quests.find { it.type == QuestType.MAIN }
        if (existingMainQuest != null) {
            updateQuest(
                id = existingMainQuest.id,
                title = title,
                type = QuestType.MAIN,
                description = description,
                targetValue = targetValue,
                currentValue = currentValue,
                xpReward = xpReward,
                coinReward = coinReward,
                chest = chest,
                status = if (currentValue >= targetValue) QuestStatus.COMPLETED else QuestStatus.ACTIVE,
                targetType = existingMainQuest.targetType,
                tags = existingMainQuest.tags,
                durationText = existingMainQuest.durationText
            )
        } else {
            addQuest(
                title = title,
                type = QuestType.MAIN,
                description = description,
                targetValue = targetValue,
                xpReward = xpReward,
                coinReward = coinReward,
                chest = chest,
                targetType = "Tasks",
                tags = emptyList(),
                durationText = "No limit"
            )
        }
    }

    fun claimMainQuest() {
        viewModelScope.launch {
            val quest = _uiState.value.quests.find { it.type == QuestType.MAIN && it.status == QuestStatus.ACTIVE } ?: return@launch
            if (quest.currentValue >= quest.targetValue) {
                completeManualQuest(quest.id)
            }
        }
    }

    fun openMainQuestDialog() {
        _uiState.update { it.copy(showMainQuestDialog = true) }
    }

    fun closeMainQuestDialog() {
        _uiState.update { it.copy(showMainQuestDialog = false) }
    }
}

class ProgressViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(userStats = stats) }
                }
            }
        }
        viewModelScope.launch {
            statisticsRepository.allLogsFlow.collect { logs ->
                _uiState.update { it.copy(allLogs = logs) }
            }
        }
        viewModelScope.launch {
            taskRepository.allTasksFlow.collect { list ->
                _uiState.update { it.copy(priorities = list) }
            }
        }
        viewModelScope.launch {
            habitRepository.allHabitsFlow.collect { list ->
                _uiState.update { it.copy(habits = list) }
            }
        }
        viewModelScope.launch {
            questRepository.allQuestsFlow.collect { list ->
                _uiState.update { it.copy(quests = list) }
            }
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            db.clearAllTables()
            userRepository.saveUserStats(UserStats())
            _uiState.update { ProgressUiState() }
        }
    }
}

class UserViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(userStats = stats) }
                }
            }
        }
    }

    fun updateProfileName(newName: String) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(name = newName.ifBlank { "Player" })
            userRepository.saveUserStats(updated)
        }
    }

    fun updateAvatar(newAvatarId: Int) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(avatarId = newAvatarId)
            userRepository.saveUserStats(updated)
        }
    }

    fun updateSelectedTitle(newTitle: String) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(selectedTitle = newTitle)
            userRepository.saveUserStats(updated)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            db.clearAllTables()
            userRepository.saveUserStats(UserStats())
        }
    }

    fun openMiniProfile() {
        _uiState.update { it.copy(showMiniProfile = true) }
    }

    fun closeMiniProfile() {
        _uiState.update { it.copy(showMiniProfile = false) }
    }
}
