package com.example

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
