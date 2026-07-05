package com.example

import androidx.compose.ui.graphics.vector.ImageVector

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

enum class TaskStatus {
    ACTIVE, COMPLETED
}

enum class HabitStatus {
    ACTIVE, INACTIVE
}

enum class ActivityLogType {
    TASK, HABIT, QUEST, ACHIEVEMENT
}

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
    val status: TaskStatus = TaskStatus.ACTIVE
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
