package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.database.AppDatabase
import com.example.database.TaskRepository
import com.example.database.HabitRepository
import com.example.database.QuestRepository
import com.example.database.UserRepository
import com.example.database.StatisticsRepository
import com.example.services.XPService
import com.example.services.CoinService
import com.example.services.LevelService
import com.example.services.StreakService
import com.example.services.AchievementService
import com.example.services.QuestEngine
import com.example.services.GamificationService

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val db = AppDatabase.getDatabase(application)
    protected val taskRepository = TaskRepository(db)
    protected val habitRepository = HabitRepository(db)
    protected val questRepository = QuestRepository(db)
    protected val userRepository = UserRepository(db)
    protected val statisticsRepository = StatisticsRepository(db)

    // Services
    protected val xpService = XPService()
    protected val coinService = CoinService()
    protected val levelService = LevelService()
    protected val streakService = StreakService()
    protected val achievementService = AchievementService()
    protected val questEngine = QuestEngine(
        questRepository = questRepository,
        userRepository = userRepository,
        statisticsRepository = statisticsRepository,
        xpService = xpService,
        coinService = coinService,
        levelService = levelService
    )
    protected val gamificationService = GamificationService(
        taskRepository = taskRepository,
        habitRepository = habitRepository,
        questRepository = questRepository,
        userRepository = userRepository,
        statisticsRepository = statisticsRepository,
        xpService = xpService,
        coinService = coinService,
        levelService = levelService,
        streakService = streakService,
        achievementService = achievementService,
        questEngine = questEngine
    )
}
