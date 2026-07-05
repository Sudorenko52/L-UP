package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
