package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuestUiState(
    val quests: List<Quest> = emptyList(),
    val userStats: UserStats = UserStats(),
    val showMainQuestDialog: Boolean = false
)

class QuestViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState: StateFlow<QuestUiState> = _uiState.asStateFlow()

    private var hasSeededQuests = false

    init {
        viewModelScope.launch {
            questRepository.allQuestsFlow.collect { list ->
                if (list.isNotEmpty() || hasSeededQuests) {
                    _uiState.update { it.copy(quests = list) }
                } else {
                    seedDefaultQuests()
                }
            }
        }
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(userStats = stats) }
                }
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
                    description = "Learn basic widgets, state management, and build 3 full apps",
                    targetValue = 100f,
                    currentValue = 65f,
                    xpReward = 500,
                    coinReward = 200,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Tasks",
                    tags = listOf("Learning"),
                    durationText = "2 weeks remaining"
                ),
                Quest(
                    id = "q1",
                    title = "The Waterboy",
                    type = QuestType.WEEKLY,
                    description = "Log Water intake habit 7 times this week",
                    targetValue = 7f,
                    currentValue = 3f,
                    xpReward = 100,
                    coinReward = 30,
                    chest = RewardChest.SILVER,
                    status = QuestStatus.ACTIVE,
                    targetType = "Water",
                    tags = emptyList(),
                    durationText = "3 days remaining"
                ),
                Quest(
                    id = "q2",
                    title = "Study Monster",
                    type = QuestType.WEEKLY,
                    description = "Complete 5 Study tasks",
                    targetValue = 5f,
                    currentValue = 2f,
                    xpReward = 150,
                    coinReward = 50,
                    chest = RewardChest.GOLD,
                    status = QuestStatus.ACTIVE,
                    targetType = "Study",
                    tags = emptyList(),
                    durationText = "4 days remaining"
                ),
                Quest(
                    id = "q3",
                    title = "Gym Rat Workout",
                    type = QuestType.SPECIAL,
                    description = "Complete Gym tasks or Fitness habits",
                    targetValue = 15f,
                    currentValue = 6f,
                    xpReward = 300,
                    coinReward = 100,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Gym",
                    tags = emptyList(),
                    durationText = "No limit"
                ),
                Quest(
                    id = "q4",
                    title = "Scholarship Journey",
                    type = QuestType.MONTHLY,
                    description = "Read 30 books (pages log)",
                    targetValue = 30f,
                    currentValue = 12f,
                    xpReward = 400,
                    coinReward = 150,
                    chest = RewardChest.EPIC,
                    status = QuestStatus.ACTIVE,
                    targetType = "Book",
                    tags = emptyList(),
                    durationText = "19 days remaining"
                )
            )
            for (q in defaults) {
                questRepository.insertQuest(q)
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
            gamificationService.addQuest(newQuest, _uiState.value.quests)
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
        viewModelScope.launch {
            val quest = _uiState.value.quests.find { it.id == id } ?: return@launch
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
            gamificationService.updateQuestDetails(updated, quest, _uiState.value.quests)
        }
    }

    fun deleteQuest(id: String) {
        viewModelScope.launch {
            questRepository.deleteQuest(id)
        }
    }

    fun completeManualQuest(id: String) {
        viewModelScope.launch {
            gamificationService.claimQuest(id)
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
        val existingMainQuest = _uiState.value.quests.find { it.type == QuestType.MAIN }
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
        viewModelScope.launch {
            val quest = _uiState.value.quests.find { it.type == QuestType.MAIN && it.status == QuestStatus.ACTIVE } ?: return@launch
            if (quest.currentValue >= quest.targetValue) {
                completeManualQuest(quest.id)
            }
        }
    }

    fun openMainQuestDialog() {
        _uiState.update { it.copy(showMainQuestDialog = true) }
    }

    fun closeMainQuestDialog() {
        _uiState.update { it.copy(showMainQuestDialog = false) }
    }
}
