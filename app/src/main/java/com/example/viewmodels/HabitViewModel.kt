package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val showHabitDialog: Boolean = false,
    val habitToEditId: String? = null,
    val showDeleteDialog: Boolean = false,
    val habitToDeleteId: String? = null
)

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
