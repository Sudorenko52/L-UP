package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.DatabaseRepository
import com.example.database.ActivityLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class UserStats(
    val name: String = "Anton",
    val level: Int = 12,
    val xp: Int = 840,
    val maxXp: Int = 1000,
    val coins: Int = 425,
    val streak: Int = 17,
    val selectedTitle: String = "Shadow Runner",
    val unlockedTitles: List<String> = listOf("Shadow Runner", "Novice Tasker", "Bug Hunter", "Code Ninja", "Quest Master"),
    val unlockedBadges: List<String> = listOf("First Step", "7-Day Warrior", "Coin Collector"),
    val totalXp: Int = 25680,
    val avatarId: Int = 0,
    val todayXpEarned: Int = 65,
    val todayCoinsEarned: Int = 18
)

data class MainQuest(
    val title: String = "Become a Flutter Developer",
    val progress: Float = 0.65f,
    val xpReward: Int = 500,
    val coinReward: Int = 200
)

enum class QuestType(val displayName: String) {
    MAIN("Main Quest"),
    WEEKLY("Weekly Quest"),
    MONTHLY("Monthly Quest"),
    SPECIAL("Special Quest")
}

enum class QuestStatus {
    ACTIVE, COMPLETED, EXPIRED
}

enum class RewardChest {
    NONE, SILVER, GOLD, EPIC
}

data class Quest(
    val id: String,
    val title: String,
    val type: QuestType,
    val description: String = "",
    val targetValue: Float,
    val currentValue: Float = 0f,
    val xpReward: Int,
    val coinReward: Int,
    val chest: RewardChest = RewardChest.NONE,
    val status: QuestStatus = QuestStatus.ACTIVE,
    val targetType: String = "Tasks", // "Tasks", "Habits", "Water", "Sleep", "Reading", "XP Gained", "Level", "Streak"
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val durationText: String = "No limit"
)

enum class PriorityLevel(val displayName: String, val colorHex: Long) {
    CRITICAL("Critical", 0xFFE53935),
    IMPORTANT("Important", 0xFFFFB300),
    NORMAL("Normal", 0xFF4CAF50)
}

data class PriorityTask(
    val id: String,
    val title: String,
    val level: PriorityLevel,
    val iconName: String, // "Gym", "Work", "Study", "General", "Book", "Clean", "Code"
    val completed: Boolean = false,
    val description: String = "",
    val xpReward: Int = 10,
    val coinReward: Int = 2,
    val time: String = "",
    val date: String = "Today", // "Today", "Tomorrow", "Upcoming"
    val progress: Float = 0f, // 0f to 1f representing percentage
    val tags: List<String> = emptyList(),
    val repeat: String = "Once",
    val status: String = "Active"
)

data class Habit(
    val id: String,
    val title: String,
    val iconName: String, // "Water", "Sleep", "Reading", "Creatine", "Meditate", "Exercise", "Vitamins", "Custom"
    val colorHex: Long,
    val currentValue: Float,
    val maxValue: Float,
    val unit: String,
    val subtitle: String, // "70%", "Good", "Today", "Done"
    val completed: Boolean = false,
    val incrementStep: Float = 1.0f,
    val description: String = "",
    val repeat: String = "Every Day",
    val xpReward: Int = 10,
    val coinReward: Int = 2,
    val isPenaltyEnabled: Boolean = false,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class AppTab {
    HOME, TASKS, QUESTS, PROGRESS, PROFILE
}

class GamifiedTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DatabaseRepository(AppDatabase.getDatabase(application))

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _mainQuest = MutableStateFlow(MainQuest(title = "Become a Flutter Developer", progress = 0.65f, xpReward = 500, coinReward = 200))
    val mainQuest: StateFlow<MainQuest> = _mainQuest.asStateFlow()

    private val _quests = MutableStateFlow<List<Quest>>(emptyList())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _priorities = MutableStateFlow<List<PriorityTask>>(emptyList())
    val priorities: StateFlow<List<PriorityTask>> = _priorities.asStateFlow()

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Interactive stats
    private val _todayXpEarned = MutableStateFlow(65)
    val todayXpEarned: StateFlow<Int> = _todayXpEarned.asStateFlow()

    private val _todayCoinsEarned = MutableStateFlow(18)
    val todayCoinsEarned: StateFlow<Int> = _todayCoinsEarned.asStateFlow()

    private var hasSeededTasks = false
    private var hasSeededHabits = false
    private var hasSeededQuests = false

    init {
        // Collect reactive state flows from Repository and update the UI states
        viewModelScope.launch {
            repository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _userStats.value = stats
                    _todayXpEarned.value = stats.todayXpEarned
                    _todayCoinsEarned.value = stats.todayCoinsEarned
                } else {
                    repository.saveUserStats(UserStats())
                }
            }
        }

        viewModelScope.launch {
            repository.allTasksFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededTasks) {
                    _priorities.value = list
                } else {
                    seedDefaultTasks()
                }
            }
        }

        viewModelScope.launch {
            repository.allHabitsFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededHabits) {
                    _habits.value = list
                } else {
                    seedDefaultHabits()
                }
            }
        }

        viewModelScope.launch {
            repository.allQuestsFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededQuests) {
                    _quests.value = list
                    list.find { it.type == QuestType.MAIN }?.let { mq ->
                        _mainQuest.value = MainQuest(
                            title = mq.title,
                            progress = if (mq.targetValue > 0) mq.currentValue / mq.targetValue else 0f,
                            xpReward = mq.xpReward,
                            coinReward = mq.coinReward
                        )
                    }
                } else {
                    seedDefaultQuests()
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
                repository.insertTask(t)
            }
        }
    }

    private fun seedDefaultHabits() {
        if (hasSeededHabits) return
        hasSeededHabits = true
        viewModelScope.launch {
            val defaults = listOf(
                Habit("1", "Water", "Water", 0xFF0288D1, 1.4f, 2.0f, "L", "70%", false, 0.2f),
                Habit("2", "Sleep", "Sleep", 0xFF5E35B1, 7.33f, 8.0f, "h", "Good", false, 1.0f),
                Habit("3", "Reading", "Reading", 0xFFFBC02D, 22f, 30f, "min", "Today", false, 5f),
                Habit("4", "Creatine", "Creatine", 0xFF2E7D32, 1f, 1f, "", "Done", true, 1f),
                Habit("5", "Meditate", "Meditate", 0xFFD81B60, 10f, 15f, "min", "Today", false, 5f)
            )
            for (h in defaults) {
                repository.insertHabit(h)
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
                    description = "Learn Flutter and build a portfolio of apps",
                    targetValue = 1000f,
                    currentValue = 650f,
                    xpReward = 500,
                    coinReward = 200,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "XP Gained",
                    durationText = "No limit"
                ),
                Quest(
                    id = "q_w1",
                    title = "Complete 25 Tasks",
                    type = QuestType.WEEKLY,
                    description = "Complete any tasks",
                    targetValue = 25f,
                    currentValue = 17f,
                    xpReward = 80,
                    coinReward = 30,
                    chest = RewardChest.SILVER,
                    status = QuestStatus.ACTIVE,
                    targetType = "Tasks",
                    tags = listOf("Tasks"),
                    durationText = "This week"
                ),
                Quest(
                    id = "q_w2",
                    title = "Train 4 Times",
                    type = QuestType.WEEKLY,
                    description = "Any gym or workout activity",
                    targetValue = 4f,
                    currentValue = 3f,
                    xpReward = 60,
                    coinReward = 20,
                    chest = RewardChest.NONE,
                    status = QuestStatus.ACTIVE,
                    targetType = "Habits",
                    tags = listOf("Fitness", "Gym"),
                    durationText = "This week"
                ),
                Quest(
                    id = "q_w3",
                    title = "Read 200 Pages",
                    type = QuestType.WEEKLY,
                    description = "Any reading activity",
                    targetValue = 200f,
                    currentValue = 120f,
                    xpReward = 60,
                    coinReward = 20,
                    chest = RewardChest.NONE,
                    status = QuestStatus.ACTIVE,
                    targetType = "Reading",
                    tags = listOf("Reading", "Learning"),
                    durationText = "This week"
                ),
                Quest(
                    id = "q_w4",
                    title = "Drink 10L Water",
                    type = QuestType.WEEKLY,
                    description = "Stay hydrated",
                    targetValue = 10f,
                    currentValue = 10f,
                    xpReward = 50,
                    coinReward = 15,
                    chest = RewardChest.NONE,
                    status = QuestStatus.COMPLETED,
                    targetType = "Water",
                    tags = listOf("Hydration", "Water"),
                    durationText = "This week",
                    completedAt = System.currentTimeMillis() - 86400000
                ),
                Quest(
                    id = "q_w5",
                    title = "Sleep 7h per day",
                    type = QuestType.WEEKLY,
                    description = "Average sleep duration this week",
                    targetValue = 7f,
                    currentValue = 5f,
                    xpReward = 50,
                    coinReward = 15,
                    chest = RewardChest.NONE,
                    status = QuestStatus.ACTIVE,
                    targetType = "Sleep",
                    tags = listOf("Sleep", "Rest"),
                    durationText = "This week"
                ),
                Quest(
                    id = "q_m1",
                    title = "Complete 100 Tasks",
                    type = QuestType.MONTHLY,
                    description = "Consistency is key",
                    targetValue = 100f,
                    currentValue = 45f,
                    xpReward = 300,
                    coinReward = 100,
                    chest = RewardChest.GOLD,
                    status = QuestStatus.ACTIVE,
                    targetType = "Tasks",
                    durationText = "This month"
                ),
                Quest(
                    id = "q_m2",
                    title = "Perfect Week of Habits",
                    type = QuestType.MONTHLY,
                    description = "Complete all daily habits for 7 days",
                    targetValue = 7f,
                    currentValue = 4f,
                    xpReward = 200,
                    coinReward = 50,
                    chest = RewardChest.SILVER,
                    status = QuestStatus.ACTIVE,
                    targetType = "Habits",
                    durationText = "This month"
                ),
                Quest(
                    id = "q_s1",
                    title = "Century Club",
                    type = QuestType.SPECIAL,
                    description = "Reach Level 100",
                    targetValue = 100f,
                    currentValue = 12f,
                    xpReward = 1000,
                    coinReward = 500,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Level",
                    durationText = "No limit"
                ),
                Quest(
                    id = "q_s2",
                    title = "Streak Legend",
                    type = QuestType.SPECIAL,
                    description = "Maintain a 30-day streak",
                    targetValue = 30f,
                    currentValue = 17f,
                    xpReward = 500,
                    coinReward = 200,
                    chest = RewardChest.GOLD,
                    status = QuestStatus.ACTIVE,
                    targetType = "Streak",
                    durationText = "No limit"
                )
            )
            for (q in defaults) {
                repository.insertQuest(q)
            }
        }
    }

    private fun insertActivityLog(type: String, referenceId: String, xpChange: Int, coinChange: Int) {
        viewModelScope.launch {
            repository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    referenceId = referenceId,
                    xpChange = xpChange,
                    coinChange = coinChange,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun togglePriority(id: String) {
        val task = _priorities.value.find { it.id == id } ?: return
        val nextCompleted = !task.completed
        val nextProgress = if (nextCompleted) 1.0f else 0.0f
        
        val xpDiff = if (nextCompleted) task.xpReward else -task.xpReward
        val coinDiff = if (nextCompleted) task.coinReward else -task.coinReward
        
        addRewards(xpDiff, coinDiff)
        
        val updatedTask = task.copy(completed = nextCompleted, progress = nextProgress)
        
        viewModelScope.launch {
            repository.updateTask(updatedTask)
            if (nextCompleted) {
                insertActivityLog("TASK", id, task.xpReward, task.coinReward)
                triggerQuestEngineForTask(updatedTask)
            } else {
                insertActivityLog("TASK_UNDO", id, -task.xpReward, -task.coinReward)
            }
        }
    }

    fun updateTaskProgress(id: String, newProgress: Float) {
        val task = _priorities.value.find { it.id == id } ?: return
        val wasCompleted = task.completed
        val isNowCompleted = newProgress >= 1.0f
        val coercedProgress = newProgress.coerceIn(0f, 1f)
        
        val xpDiff = if (isNowCompleted && !wasCompleted) {
            task.xpReward
        } else if (!isNowCompleted && wasCompleted) {
            -task.xpReward
        } else {
            0
        }
        val coinDiff = if (isNowCompleted && !wasCompleted) {
            task.coinReward
        } else if (!isNowCompleted && wasCompleted) {
            -task.coinReward
        } else {
            0
        }
        if (xpDiff != 0 || coinDiff != 0) {
            addRewards(xpDiff, coinDiff)
        }
        val updatedTask = task.copy(progress = coercedProgress, completed = isNowCompleted)
        
        viewModelScope.launch {
            repository.updateTask(updatedTask)
            if (isNowCompleted && !wasCompleted) {
                insertActivityLog("TASK", id, task.xpReward, task.coinReward)
                triggerQuestEngineForTask(updatedTask)
            }
        }
    }

    fun incrementHabit(id: String) {
        val habit = _habits.value.find { it.id == id } ?: return
        if (habit.completed && habit.maxValue == habit.currentValue) {
            val updated = habit.copy(currentValue = 0f, completed = false, subtitle = "0%")
            viewModelScope.launch {
                repository.updateHabit(updated)
            }
        } else {
            val newValue = (habit.currentValue + habit.incrementStep).coerceAtMost(habit.maxValue)
            val isNowCompleted = newValue >= habit.maxValue
            
            if (isNowCompleted && !habit.completed) {
                addRewards(habit.xpReward, habit.coinReward)
            }
            
            val percent = if (habit.maxValue > 0) ((newValue / habit.maxValue) * 100).toInt() else 0
            val nextSubtitle = when (habit.iconName) {
                "Sleep" -> if (newValue >= 7.0f) "Good" else "Tired"
                "Creatine" -> if (isNowCompleted) "Done" else "Missed"
                else -> if (isNowCompleted) "Done" else "$percent%"
            }
            val updated = habit.copy(currentValue = newValue, completed = isNowCompleted, subtitle = nextSubtitle)
            
            viewModelScope.launch {
                repository.updateHabit(updated)
                if (isNowCompleted && !habit.completed) {
                    insertActivityLog("HABIT", id, habit.xpReward, habit.coinReward)
                }
                triggerQuestEngineForHabit(updated, habit.incrementStep)
            }
        }
    }

    fun toggleHabitCompletion(id: String, completed: Boolean) {
        val habit = _habits.value.find { it.id == id } ?: return
        if (habit.completed == completed) return
        
        val newValue = if (completed) habit.maxValue else 0f
        
        if (completed) {
            addRewards(habit.xpReward, habit.coinReward)
        } else {
            val xpToSubtract = -habit.xpReward
            val coinsToSubtract = if (habit.isPenaltyEnabled) -habit.coinReward else 0
            addRewards(xpToSubtract, coinsToSubtract)
        }
        
        val percent = if (habit.maxValue > 0) ((newValue / habit.maxValue) * 100).toInt() else 0
        val nextSubtitle = when (habit.iconName) {
            "Sleep" -> if (newValue >= 7.0f) "Good" else "Tired"
            "Creatine" -> if (completed) "Done" else "Missed"
            else -> if (completed) "Done" else "$percent%"
        }
        val updated = habit.copy(currentValue = newValue, completed = completed, subtitle = nextSubtitle)
        
        viewModelScope.launch {
            repository.updateHabit(updated)
            if (completed) {
                insertActivityLog("HABIT", id, habit.xpReward, habit.coinReward)
                triggerQuestEngineForHabit(updated, habit.maxValue)
            } else {
                insertActivityLog("HABIT_UNDO", id, -habit.xpReward, if (habit.isPenaltyEnabled) -habit.coinReward else 0)
            }
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
            repository.insertHabit(newHabit)
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
        val habit = _habits.value.find { it.id == id } ?: return
        val isNowCompleted = completed || (currentValue >= maxValue)
        
        if (isNowCompleted && !habit.completed) {
            addRewards(xpReward, coinReward)
        } else if (!isNowCompleted && habit.completed) {
            val xpToSubtract = -habit.xpReward
            val coinsToSubtract = if (habit.isPenaltyEnabled) -habit.coinReward else 0
            addRewards(xpToSubtract, coinsToSubtract)
        }
        
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
        
        viewModelScope.launch {
            repository.updateHabit(updated)
            if (isNowCompleted && !habit.completed) {
                insertActivityLog("HABIT", id, xpReward, coinReward)
                triggerQuestEngineForHabit(updated, currentValue - habit.currentValue)
            }
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    fun moveHabitUp(id: String) {
        val list = _habits.value
        val index = list.indexOfFirst { it.id == id }
        if (index <= 0) return
        val itemA = list[index]
        val itemB = list[index - 1]
        
        viewModelScope.launch {
            repository.updateHabit(itemA.copy(createdAt = itemB.createdAt))
            repository.updateHabit(itemB.copy(createdAt = itemA.createdAt))
        }
    }

    fun moveHabitDown(id: String) {
        val list = _habits.value
        val index = list.indexOfFirst { it.id == id }
        if (index < 0 || index >= list.size - 1) return
        val itemA = list[index]
        val itemB = list[index + 1]
        
        viewModelScope.launch {
            repository.updateHabit(itemA.copy(createdAt = itemB.createdAt))
            repository.updateHabit(itemB.copy(createdAt = itemA.createdAt))
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
        if (isCompleted) {
            addRewards(xpReward, coinReward)
        }
        viewModelScope.launch {
            repository.insertTask(newTask)
            if (isCompleted) {
                insertActivityLog("TASK", newId, xpReward, coinReward)
                triggerQuestEngineForTask(newTask)
            }
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
        val task = _priorities.value.find { it.id == id } ?: return
        val wasCompleted = task.completed
        val isNowCompleted = progress >= 1.0f
        
        val xpDiff = if (isNowCompleted && !wasCompleted) {
            xpReward
        } else if (!isNowCompleted && wasCompleted) {
            -task.xpReward
        } else {
            0
        }
        val coinDiff = if (isNowCompleted && !wasCompleted) {
            coinReward
        } else if (!isNowCompleted && wasCompleted) {
            -task.coinReward
        } else {
            0
        }
        if (xpDiff != 0 || coinDiff != 0) {
            addRewards(xpDiff, coinDiff)
        }
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
        viewModelScope.launch {
            repository.updateTask(updatedTask)
            if (isNowCompleted && !wasCompleted) {
                insertActivityLog("TASK", id, xpReward, coinReward)
                triggerQuestEngineForTask(updatedTask)
            }
        }
    }

    fun deletePriorityTask(id: String) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    private fun addRewards(xpGained: Int, coinsGained: Int, isQuestReward: Boolean = false) {
        _todayXpEarned.update { (it + xpGained).coerceAtLeast(0) }
        _todayCoinsEarned.update { (it + coinsGained).coerceAtLeast(0) }

        val current = _userStats.value
        var nextXp = current.xp + xpGained
        var nextLevel = current.level
        val maxXp = current.maxXp

        if (nextXp >= maxXp) {
            nextXp -= maxXp
            nextLevel += 1
        } else if (nextXp < 0) {
            if (nextLevel > 1) {
                nextLevel -= 1
                nextXp += maxXp
            } else {
                nextXp = 0
            }
        }

        val updatedStats = current.copy(
            level = nextLevel,
            xp = nextXp,
            coins = (current.coins + coinsGained).coerceAtLeast(0),
            totalXp = current.totalXp + xpGained,
            todayXpEarned = _todayXpEarned.value,
            todayCoinsEarned = _todayCoinsEarned.value
        )
        
        viewModelScope.launch {
            repository.saveUserStats(updatedStats)
        }

        if (xpGained > 0 && !isQuestReward) {
            _quests.value.forEach { quest ->
                if (quest.status != QuestStatus.ACTIVE) return@forEach
                
                var increment = 0f
                if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                    increment = xpGained.toFloat()
                } else if (quest.targetType.equals("Level", ignoreCase = true)) {
                    val nextLvl = nextLevel.toFloat()
                    val isCompleted = nextLvl >= quest.targetValue
                    val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                        quest.copy(currentValue = nextLvl, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                    } else {
                        quest.copy(currentValue = nextLvl)
                    }
                    viewModelScope.launch {
                        repository.updateQuest(nextQuest)
                        if (quest.type == QuestType.SPECIAL && isCompleted) {
                            addRewards(quest.xpReward, quest.coinReward, isQuestReward = true)
                            insertActivityLog("QUEST", quest.id, quest.xpReward, quest.coinReward)
                        }
                    }
                    return@forEach
                } else if (quest.targetType.equals("Streak", ignoreCase = true)) {
                    val nextStreak = current.streak.toFloat()
                    val isCompleted = nextStreak >= quest.targetValue
                    val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                        quest.copy(currentValue = nextStreak, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                    } else {
                        quest.copy(currentValue = nextStreak)
                    }
                    viewModelScope.launch {
                        repository.updateQuest(nextQuest)
                        if (quest.type == QuestType.SPECIAL && isCompleted) {
                            addRewards(quest.xpReward, quest.coinReward, isQuestReward = true)
                            insertActivityLog("QUEST", quest.id, quest.xpReward, quest.coinReward)
                        }
                    }
                    return@forEach
                }

                if (increment > 0f) {
                    val nextValue = (quest.currentValue + increment).coerceAtMost(quest.targetValue)
                    val isCompleted = nextValue >= quest.targetValue
                    val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                        quest.copy(currentValue = nextValue, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                    } else {
                        quest.copy(currentValue = nextValue)
                    }
                    viewModelScope.launch {
                        repository.updateQuest(nextQuest)
                        if (quest.type == QuestType.SPECIAL && isCompleted) {
                            addRewards(quest.xpReward, quest.coinReward, isQuestReward = true)
                            insertActivityLog("QUEST", quest.id, quest.xpReward, quest.coinReward)
                        }
                    }
                }
            }
        }
    }

    fun triggerQuestEngineForTask(task: PriorityTask) {
        if (!task.completed) return
        _quests.value.forEach { quest ->
            if (quest.status != QuestStatus.ACTIVE) return@forEach
            
            var increment = 0f
            if (quest.targetType.equals("Tasks", ignoreCase = true)) {
                if (quest.tags.isEmpty() || task.tags.any { tag -> quest.tags.any { it.equals(tag, ignoreCase = true) } }) {
                    increment = 1f
                }
            } else if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                increment = task.xpReward.toFloat()
            } else {
                if (quest.tags.any { tag -> task.tags.any { it.equals(tag, ignoreCase = true) } } || quest.targetType.equals(task.iconName, ignoreCase = true)) {
                    increment = 1f
                }
            }

            if (increment > 0f) {
                val nextValue = (quest.currentValue + increment).coerceAtMost(quest.targetValue)
                val isCompleted = nextValue >= quest.targetValue
                val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                    quest.copy(currentValue = nextValue, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                } else {
                    quest.copy(currentValue = nextValue)
                }
                viewModelScope.launch {
                    repository.updateQuest(nextQuest)
                    if (quest.type == QuestType.SPECIAL && isCompleted) {
                        addRewards(quest.xpReward, quest.coinReward, isQuestReward = true)
                        insertActivityLog("QUEST", quest.id, quest.xpReward, quest.coinReward)
                    }
                }
            }
        }
    }

    fun triggerQuestEngineForHabit(habit: Habit, incrementedAmount: Float) {
        _quests.value.forEach { quest ->
            if (quest.status != QuestStatus.ACTIVE) return@forEach
            
            var increment = 0f
            if (quest.targetType.equals("Habits", ignoreCase = true)) {
                if (habit.completed) {
                    increment = 1f
                }
            } else if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                if (habit.completed) {
                    increment = habit.xpReward.toFloat()
                }
            } else if (quest.targetType.equals(habit.iconName, ignoreCase = true) || quest.targetType.equals(habit.title, ignoreCase = true)) {
                increment = incrementedAmount
            } else if (quest.tags.any { tag -> habit.tags.any { it.equals(tag, ignoreCase = true) } }) {
                if (habit.completed) {
                    increment = 1f
                }
            }

            if (increment > 0f) {
                val nextValue = (quest.currentValue + increment).coerceAtMost(quest.targetValue)
                val isCompleted = nextValue >= quest.targetValue
                val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                    quest.copy(currentValue = nextValue, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                } else {
                    quest.copy(currentValue = nextValue)
                }
                viewModelScope.launch {
                    repository.updateQuest(nextQuest)
                    if (quest.type == QuestType.SPECIAL && isCompleted) {
                        addRewards(quest.xpReward, quest.coinReward, isQuestReward = true)
                        insertActivityLog("QUEST", quest.id, quest.xpReward, quest.coinReward)
                    }
                }
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
            if (type == QuestType.MAIN) {
                _quests.value.filter { it.type == QuestType.MAIN }.forEach {
                    repository.updateQuest(it.copy(type = QuestType.SPECIAL))
                }
            }
            repository.insertQuest(newQuest)
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
        val quest = _quests.value.find { it.id == id } ?: return
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
        viewModelScope.launch {
            if (type == QuestType.MAIN) {
                _quests.value.filter { it.type == QuestType.MAIN && it.id != id }.forEach {
                    repository.updateQuest(it.copy(type = QuestType.SPECIAL))
                }
            }
            repository.updateQuest(updated)
            if (isNowCompleted && quest.status != QuestStatus.COMPLETED) {
                insertActivityLog("QUEST", id, xpReward, coinReward)
            }
        }
    }

    fun deleteQuest(id: String) {
        viewModelScope.launch {
            repository.deleteQuest(id)
        }
    }

    fun completeManualQuest(id: String) {
        val quest = _quests.value.find { it.id == id } ?: return
        if (quest.status == QuestStatus.ACTIVE && quest.currentValue >= quest.targetValue) {
            addRewards(quest.xpReward, quest.coinReward)
            val updated = quest.copy(status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
            viewModelScope.launch {
                repository.updateQuest(updated)
                insertActivityLog("QUEST", id, quest.xpReward, quest.coinReward)
            }
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
        val existingMainQuest = _quests.value.find { it.type == QuestType.MAIN }
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
        val quest = _quests.value.find { it.type == QuestType.MAIN && it.status == QuestStatus.ACTIVE }
        if (quest != null && quest.currentValue >= quest.targetValue) {
            completeManualQuest(quest.id)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            val dbInstance = AppDatabase.getDatabase(getApplication())
            dbInstance.clearAllTables()
            hasSeededTasks = false
            hasSeededHabits = false
            hasSeededQuests = false
            
            repository.saveUserStats(UserStats())
            seedDefaultTasks()
            seedDefaultHabits()
            seedDefaultQuests()
            
            _todayXpEarned.value = 65
            _todayCoinsEarned.value = 18
        }
    }

    fun updateProfileName(newName: String) {
        val current = _userStats.value
        val updated = current.copy(name = newName.ifBlank { "Player" })
        viewModelScope.launch {
            repository.saveUserStats(updated)
        }
    }

    fun updateAvatar(newAvatarId: Int) {
        val current = _userStats.value
        val updated = current.copy(avatarId = newAvatarId)
        viewModelScope.launch {
            repository.saveUserStats(updated)
        }
    }

    fun updateSelectedTitle(newTitle: String) {
        val current = _userStats.value
        val updated = current.copy(selectedTitle = newTitle)
        viewModelScope.launch {
            repository.saveUserStats(updated)
        }
    }
}
