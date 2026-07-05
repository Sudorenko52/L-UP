package com.example

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

enum class ActivityLogType {
    TASK, HABIT, QUEST, ACHIEVEMENT
}

enum class AppTab {
    HOME, TASKS, QUESTS, PROGRESS, PROFILE
}
