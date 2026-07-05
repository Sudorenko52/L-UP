package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.database.ActivityLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val userStats: UserStats = UserStats(),
    val allLogs: List<ActivityLogEntity> = emptyList(),
    val priorities: List<PriorityTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val quests: List<Quest> = emptyList()
)

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
