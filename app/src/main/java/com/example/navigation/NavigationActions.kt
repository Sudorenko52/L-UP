package com.example.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navController: NavHostController) {
    fun navigateToHome() {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun navigateToTasks() {
        navController.navigate(Routes.TASKS) {
            launchSingleTop = true
        }
    }

    fun navigateToQuests() {
        navController.navigate(Routes.QUESTS) {
            launchSingleTop = true
        }
    }

    fun navigateToProgress() {
        navController.navigate(Routes.PROGRESS) {
            launchSingleTop = true
        }
    }

    fun navigateToProfile() {
        navController.navigate(Routes.PROFILE) {
            launchSingleTop = true
        }
    }

    fun navigateToTaskDetails() {
        navController.navigate(Routes.TASK_DETAILS) {
            launchSingleTop = true
        }
    }

    fun navigateToQuestDetails() {
        navController.navigate(Routes.QUEST_DETAILS) {
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }
}
