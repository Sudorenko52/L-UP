package com.example

enum class TaskStatus {
    ACTIVE, COMPLETED
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
