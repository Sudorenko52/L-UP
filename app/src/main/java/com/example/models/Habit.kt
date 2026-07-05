package com.example

enum class HabitStatus {
    ACTIVE, INACTIVE
}

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
