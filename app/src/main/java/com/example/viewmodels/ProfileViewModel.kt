package com.example

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userStats: UserStats = UserStats(),
    val showMiniProfile: Boolean = false
)

typealias UserUiState = ProfileUiState

class ProfileViewModel(application: Application) : BaseViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.userStatsFlow.collect { stats ->
                if (stats != null) {
                    _uiState.update { it.copy(userStats = stats) }
                }
            }
        }
    }

    fun updateProfileName(newName: String) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(name = newName.ifBlank { "Player" })
            userRepository.saveUserStats(updated)
        }
    }

    fun updateAvatar(newAvatarId: Int) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(avatarId = newAvatarId)
            userRepository.saveUserStats(updated)
        }
    }

    fun updateSelectedTitle(newTitle: String) {
        viewModelScope.launch {
            val current = _uiState.value.userStats
            val updated = current.copy(selectedTitle = newTitle)
            userRepository.saveUserStats(updated)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            db.clearAllTables()
            userRepository.saveUserStats(UserStats())
        }
    }

    fun openMiniProfile() {
        _uiState.update { it.copy(showMiniProfile = true) }
    }

    fun closeMiniProfile() {
        _uiState.update { it.copy(showMiniProfile = false) }
    }
}

typealias UserViewModel = ProfileViewModel
