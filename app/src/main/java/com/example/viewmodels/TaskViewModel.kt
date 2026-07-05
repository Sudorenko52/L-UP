package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskUiState(
    val priorities: List<PriorityTask> = emptyList(),
    val showTaskDialog: Boolean = false,
    val taskToEdit: PriorityTask? = null,
    val showDeleteDialog: Boolean = false,
    val taskToDeleteId: String? = null
)

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
