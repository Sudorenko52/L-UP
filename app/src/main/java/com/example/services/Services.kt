package com.example.services

import com.example.UserStats
import com.example.PriorityTask
import com.example.Habit
import com.example.Quest
import com.example.QuestType
import com.example.QuestStatus
import com.example.RewardChest
import com.example.database.TaskRepository
import com.example.database.HabitRepository
import com.example.database.QuestRepository
import com.example.database.UserRepository
import com.example.database.StatisticsRepository
import com.example.database.ActivityLogEntity
import com.example.ActivityLogType
import java.util.UUID

// =====================================================================================
// 1. XP SERVICE
// =====================================================================================
class XPService {
    fun calculateTaskXp(task: PriorityTask): Int {
        return task.xpReward
    }

    fun calculateHabitXp(habit: Habit): Int {
        return habit.xpReward
    }
}

// =====================================================================================
// 2. COIN SERVICE
// =====================================================================================
class CoinService {
    fun calculateTaskCoins(task: PriorityTask): Int {
        return task.coinReward
    }

    fun calculateHabitCoins(habit: Habit): Int {
        return habit.coinReward
    }

    fun calculateHabitPenalty(habit: Habit): Int {
        return if (habit.isPenaltyEnabled) habit.coinReward else 0
    }
}

// =====================================================================================
// 3. LEVEL SERVICE
// =====================================================================================
class LevelService {
    fun checkLevelUp(currentLevel: Int, currentXp: Int, xpGained: Int): Pair<Int, Int> {
        var nextXp = currentXp + xpGained
        var nextLevel = currentLevel
        val maxXp = 1000

        if (nextXp >= maxXp) {
            nextXp -= maxXp
            nextLevel += 1
        } else if (nextXp < 0) {
            if (nextLevel > 1) {
                nextLevel -= 1
                nextXp += maxXp
            } else {
                nextXp = 0
            }
        }
        return Pair(nextLevel, nextXp)
    }
}

// =====================================================================================
// 4. STREAK SERVICE
// =====================================================================================
class StreakService {
    fun updateStreak(stats: UserStats, activityHappenedToday: Boolean): UserStats {
        // Simple streak logic: if an activity happened, we make sure streak is at least 1, or increment
        val currentStreak = stats.streak
        val nextStreak = if (activityHappenedToday) {
            if (currentStreak == 0) 1 else currentStreak
        } else {
            currentStreak
        }
        return stats.copy(streak = nextStreak)
    }

    fun incrementStreak(stats: UserStats): UserStats {
        return stats.copy(streak = stats.streak + 1)
    }
}

// =====================================================================================
// 5. ACHIEVEMENT SERVICE
// =====================================================================================
class AchievementService {
    fun checkAchievements(stats: UserStats, allTasks: List<PriorityTask>): UserStats {
        val completedCount = allTasks.count { it.completed }
        val titles = stats.unlockedTitles.toMutableList()
        val badges = stats.unlockedBadges.toMutableList()

        if (completedCount >= 1 && !badges.contains("First Steps")) {
            badges.add("First Steps")
        }
        if (completedCount >= 10 && !badges.contains("10 Tasks")) {
            badges.add("10 Tasks")
            if (!titles.contains("Task Master")) {
                titles.add("Task Master")
            }
        }
        if (stats.streak >= 7 && !badges.contains("7 Day Streak")) {
            badges.add("7 Day Streak")
            if (!titles.contains("Streak Warrior")) {
                titles.add("Streak Warrior")
            }
        }
        if (stats.level >= 5 && !titles.contains("High Leveler")) {
            titles.add("High Leveler")
        }

        return stats.copy(
            unlockedTitles = titles,
            unlockedBadges = badges
        )
    }
}

// =====================================================================================
// 6. QUEST ENGINE
// =====================================================================================
class QuestEngine(
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val statisticsRepository: StatisticsRepository,
    private val xpService: XPService,
    private val coinService: CoinService,
    private val levelService: LevelService
) {
    suspend fun triggerQuestEngineForTask(task: PriorityTask) {
        if (!task.completed) return
        val questsList = questRepository.getAllQuestsDirect()

        questsList.forEach { quest ->
            if (quest.status != QuestStatus.ACTIVE) return@forEach

            var increment = 0f
            if (quest.targetType.equals("Tasks", ignoreCase = true)) {
                if (quest.tags.isEmpty() || task.tags.any { tag -> quest.tags.any { it.equals(tag, ignoreCase = true) } }) {
                    increment = 1f
                }
            } else if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                increment = task.xpReward.toFloat()
            } else {
                if (quest.tags.any { tag -> task.tags.any { it.equals(tag, ignoreCase = true) } } || quest.targetType.equals(task.iconName, ignoreCase = true)) {
                    increment = 1f
                }
            }

            if (increment > 0f) {
                applyQuestIncrement(quest, increment)
            }
        }
    }

    suspend fun triggerQuestEngineForHabit(habit: Habit, incrementedAmount: Float) {
        val questsList = questRepository.getAllQuestsDirect()

        questsList.forEach { quest ->
            if (quest.status != QuestStatus.ACTIVE) return@forEach

            var increment = 0f
            if (quest.targetType.equals("Habits", ignoreCase = true)) {
                if (habit.completed) {
                    increment = 1f
                }
            } else if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                if (habit.completed) {
                    increment = habit.xpReward.toFloat()
                }
            } else if (quest.targetType.equals(habit.iconName, ignoreCase = true) || quest.targetType.equals(habit.title, ignoreCase = true)) {
                increment = incrementedAmount
            } else if (quest.tags.any { tag -> habit.tags.any { it.equals(tag, ignoreCase = true) } }) {
                if (habit.completed) {
                    increment = 1f
                }
            }

            if (increment > 0f) {
                applyQuestIncrement(quest, increment)
            }
        }
    }

    suspend fun triggerQuestEngineForXp(xpGained: Int, currentLevel: Int, currentStreak: Int) {
        if (xpGained <= 0) return
        val questsList = questRepository.getAllQuestsDirect()

        questsList.forEach { quest ->
            if (quest.status != QuestStatus.ACTIVE) return@forEach

            var increment = 0f
            if (quest.targetType.equals("XP Gained", ignoreCase = true)) {
                increment = xpGained.toFloat()
            } else if (quest.targetType.equals("Level", ignoreCase = true)) {
                val nextLvl = currentLevel.toFloat()
                val isCompleted = nextLvl >= quest.targetValue
                val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                    quest.copy(currentValue = nextLvl, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                } else {
                    quest.copy(currentValue = nextLvl)
                }
                questRepository.updateQuest(nextQuest)
                if (quest.type == QuestType.SPECIAL && isCompleted) {
                    awardQuestRewards(quest)
                }
                return@forEach
            } else if (quest.targetType.equals("Streak", ignoreCase = true)) {
                val nextStreak = currentStreak.toFloat()
                val isCompleted = nextStreak >= quest.targetValue
                val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
                    quest.copy(currentValue = nextStreak, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
                } else {
                    quest.copy(currentValue = nextStreak)
                }
                questRepository.updateQuest(nextQuest)
                if (quest.type == QuestType.SPECIAL && isCompleted) {
                    awardQuestRewards(quest)
                }
                return@forEach
            }

            if (increment > 0f) {
                applyQuestIncrement(quest, increment)
            }
        }
    }

    private suspend fun applyQuestIncrement(quest: Quest, increment: Float) {
        val nextValue = (quest.currentValue + increment).coerceAtMost(quest.targetValue)
        val isCompleted = nextValue >= quest.targetValue
        val nextQuest = if (quest.type == QuestType.SPECIAL && isCompleted) {
            quest.copy(currentValue = nextValue, status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
        } else {
            quest.copy(currentValue = nextValue)
        }
        questRepository.updateQuest(nextQuest)
        if (quest.type == QuestType.SPECIAL && isCompleted) {
            awardQuestRewards(quest)
        }
    }

    private suspend fun awardQuestRewards(quest: Quest) {
        val stats = userRepository.getUserStatsDirect() ?: return
        val (lvl, xp) = levelService.checkLevelUp(stats.level, stats.xp, quest.xpReward)
        val updatedStats = stats.copy(
            level = lvl,
            xp = xp,
            coins = stats.coins + quest.coinReward,
            totalXp = stats.totalXp + quest.xpReward,
            todayXpEarned = stats.todayXpEarned + quest.xpReward,
            todayCoinsEarned = stats.todayCoinsEarned + quest.coinReward
        )
        userRepository.saveUserStats(updatedStats)
        statisticsRepository.insertLog(
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                type = ActivityLogType.QUEST,
                referenceId = quest.id,
                xpChange = quest.xpReward,
                coinChange = quest.coinReward,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}

// =====================================================================================
// 7. GAMIFICATION SERVICE (COORDINATOR)
// =====================================================================================
class GamificationService(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val questRepository: QuestRepository,
    private val userRepository: UserRepository,
    private val statisticsRepository: StatisticsRepository,
    private val xpService: XPService,
    private val coinService: CoinService,
    private val levelService: LevelService,
    private val streakService: StreakService,
    private val achievementService: AchievementService,
    private val questEngine: QuestEngine
) {
    suspend fun toggleTask(id: String) {
        val task = taskRepository.getTaskById(id) ?: return
        val updatedCompleted = !task.completed
        val updatedProgress = if (updatedCompleted) 1.0f else 0.0f
        val updatedTask = task.copy(completed = updatedCompleted, progress = updatedProgress)
        taskRepository.updateTask(updatedTask)

        val stats = userRepository.getUserStatsDirect() ?: UserStats()
        
        val xpChange = if (updatedCompleted) xpService.calculateTaskXp(task) else -task.xpReward
        val coinChange = if (updatedCompleted) coinService.calculateTaskCoins(task) else -task.coinReward

        val (newLvl, newXp) = levelService.checkLevelUp(stats.level, stats.xp, xpChange)
        
        var tempStats = stats.copy(
            level = newLvl,
            xp = newXp,
            coins = (stats.coins + coinChange).coerceAtLeast(0),
            totalXp = stats.totalXp + xpChange,
            todayXpEarned = (stats.todayXpEarned + xpChange).coerceAtLeast(0),
            todayCoinsEarned = (stats.todayCoinsEarned + coinChange).coerceAtLeast(0)
        )

        if (updatedCompleted) {
            tempStats = streakService.updateStreak(tempStats, true)
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.TASK,
                    referenceId = task.id,
                    xpChange = xpChange,
                    coinChange = coinChange,
                    createdAt = System.currentTimeMillis()
                )
            )
            // Trigger quest progress updates
            questEngine.triggerQuestEngineForTask(updatedTask)
            questEngine.triggerQuestEngineForXp(xpChange, newLvl, tempStats.streak)
        }

        val finalStats = achievementService.checkAchievements(tempStats, emptyList()) // we can pass list if needed, or dynamically check inside service
        userRepository.saveUserStats(finalStats)
    }

    suspend fun updateTaskProgress(id: String, progress: Float) {
        val task = taskRepository.getTaskById(id) ?: return
        val wasCompleted = task.completed
        val isNowCompleted = progress >= 1.0f
        val coercedProgress = progress.coerceIn(0f, 1f)

        if (wasCompleted == isNowCompleted && task.progress == coercedProgress) return

        val xpChange = if (isNowCompleted && !wasCompleted) task.xpReward else if (!isNowCompleted && wasCompleted) -task.xpReward else 0
        val coinChange = if (isNowCompleted && !wasCompleted) task.coinReward else if (!isNowCompleted && wasCompleted) -task.coinReward else 0

        val updatedTask = task.copy(progress = coercedProgress, completed = isNowCompleted)
        taskRepository.updateTask(updatedTask)

        val stats = userRepository.getUserStatsDirect() ?: UserStats()
        val (newLvl, newXp) = levelService.checkLevelUp(stats.level, stats.xp, xpChange)

        var tempStats = stats.copy(
            level = newLvl,
            xp = newXp,
            coins = (stats.coins + coinChange).coerceAtLeast(0),
            totalXp = stats.totalXp + xpChange,
            todayXpEarned = (stats.todayXpEarned + xpChange).coerceAtLeast(0),
            todayCoinsEarned = (stats.todayCoinsEarned + coinChange).coerceAtLeast(0)
        )

        if (isNowCompleted && !wasCompleted) {
            tempStats = streakService.updateStreak(tempStats, true)
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.TASK,
                    referenceId = task.id,
                    xpChange = xpChange,
                    coinChange = coinChange,
                    createdAt = System.currentTimeMillis()
                )
            )
            questEngine.triggerQuestEngineForTask(updatedTask)
            questEngine.triggerQuestEngineForXp(xpChange, newLvl, tempStats.streak)
        }

        val finalStats = achievementService.checkAchievements(tempStats, emptyList())
        userRepository.saveUserStats(finalStats)
    }

    suspend fun incrementHabit(id: String) {
        val habit = habitRepository.getHabitById(id) ?: return
        val nextValue = (habit.currentValue + habit.incrementStep).coerceAtMost(habit.maxValue)
        val isCompleted = nextValue >= habit.maxValue
        val wasCompleted = habit.completed

        val updatedHabit = habit.copy(
            currentValue = nextValue,
            completed = isCompleted,
            subtitle = if (isCompleted) "Done" else "${(nextValue / habit.maxValue * 100).toInt()}%"
        )
        habitRepository.updateHabit(updatedHabit)

        val stats = userRepository.getUserStatsDirect() ?: UserStats()

        var xpChange = 0
        var coinChange = 0

        if (isCompleted && !wasCompleted) {
            xpChange = xpService.calculateHabitXp(habit)
            coinChange = coinService.calculateHabitCoins(habit)
        }

        val (newLvl, newXp) = levelService.checkLevelUp(stats.level, stats.xp, xpChange)
        var tempStats = stats.copy(
            level = newLvl,
            xp = newXp,
            coins = (stats.coins + coinChange).coerceAtLeast(0),
            totalXp = stats.totalXp + xpChange,
            todayXpEarned = (stats.todayXpEarned + xpChange).coerceAtLeast(0),
            todayCoinsEarned = (stats.todayCoinsEarned + coinChange).coerceAtLeast(0)
        )

        if (isCompleted && !wasCompleted) {
            tempStats = streakService.updateStreak(tempStats, true)
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.HABIT,
                    referenceId = habit.id,
                    xpChange = xpChange,
                    coinChange = coinChange,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        questEngine.triggerQuestEngineForHabit(updatedHabit, habit.incrementStep)
        if (xpChange > 0) {
            questEngine.triggerQuestEngineForXp(xpChange, newLvl, tempStats.streak)
        }

        val finalStats = achievementService.checkAchievements(tempStats, emptyList())
        userRepository.saveUserStats(finalStats)
    }

    suspend fun toggleHabitCompletion(id: String, completed: Boolean) {
        val habit = habitRepository.getHabitById(id) ?: return
        val wasCompleted = habit.completed
        if (wasCompleted == completed) return

        val updatedHabit = habit.copy(
            completed = completed,
            currentValue = if (completed) habit.maxValue else 0f,
            subtitle = if (completed) "Done" else "0%"
        )
        habitRepository.updateHabit(updatedHabit)

        val stats = userRepository.getUserStatsDirect() ?: UserStats()
        
        val xpChange = if (completed) xpService.calculateHabitXp(habit) else -habit.xpReward
        val coinScale = if (completed) coinService.calculateHabitCoins(habit) else -habit.coinReward
        val coinPenalty = if (!completed && habit.isPenaltyEnabled) coinService.calculateHabitPenalty(habit) else 0
        val coinChange = coinScale - coinPenalty

        val (newLvl, newXp) = levelService.checkLevelUp(stats.level, stats.xp, xpChange)
        var tempStats = stats.copy(
            level = newLvl,
            xp = newXp,
            coins = (stats.coins + coinChange).coerceAtLeast(0),
            totalXp = stats.totalXp + xpChange,
            todayXpEarned = (stats.todayXpEarned + xpChange).coerceAtLeast(0),
            todayCoinsEarned = (stats.todayCoinsEarned + coinChange).coerceAtLeast(0)
        )

        if (completed) {
            tempStats = streakService.updateStreak(tempStats, true)
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.HABIT,
                    referenceId = habit.id,
                    xpChange = xpChange,
                    coinChange = coinChange,
                    createdAt = System.currentTimeMillis()
                )
            )
            questEngine.triggerQuestEngineForHabit(updatedHabit, habit.maxValue - habit.currentValue)
            questEngine.triggerQuestEngineForXp(xpChange, newLvl, tempStats.streak)
        }

        val finalStats = achievementService.checkAchievements(tempStats, emptyList())
        userRepository.saveUserStats(finalStats)
    }

    suspend fun claimQuest(id: String) {
        val quest = questRepository.getQuestById(id) ?: return
        if (quest.status == QuestStatus.ACTIVE && quest.currentValue >= quest.targetValue) {
            val stats = userRepository.getUserStatsDirect() ?: UserStats()
            
            val xpReward = quest.xpReward
            val coinReward = quest.coinReward

            val (newLvl, newXp) = levelService.checkLevelUp(stats.level, stats.xp, xpReward)
            val updatedStats = stats.copy(
                level = newLvl,
                xp = newXp,
                coins = stats.coins + coinReward,
                totalXp = stats.totalXp + xpReward,
                todayXpEarned = stats.todayXpEarned + xpReward,
                todayCoinsEarned = stats.todayCoinsEarned + coinReward
            )
            userRepository.saveUserStats(updatedStats)

            val updatedQuest = quest.copy(status = QuestStatus.COMPLETED, completedAt = System.currentTimeMillis())
            questRepository.updateQuest(updatedQuest)

            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.QUEST,
                    referenceId = quest.id,
                    xpChange = xpReward,
                    coinChange = coinReward,
                    createdAt = System.currentTimeMillis()
                )
            )
            questEngine.triggerQuestEngineForXp(xpReward, newLvl, updatedStats.streak)
        }
    }

    suspend fun addTask(task: PriorityTask) {
        taskRepository.insertTask(task)
        if (task.completed) {
            val stats = userRepository.getUserStatsDirect() ?: UserStats()
            val xpGained = xpService.calculateTaskXp(task)
            val coinsGained = coinService.calculateTaskCoins(task)
            val (lvl, xp) = levelService.checkLevelUp(stats.level, stats.xp, xpGained)
            var tempStats = stats.copy(
                level = lvl,
                xp = xp,
                coins = stats.coins + coinsGained,
                totalXp = stats.totalXp + xpGained,
                todayXpEarned = stats.todayXpEarned + xpGained,
                todayCoinsEarned = stats.todayCoinsEarned + coinsGained
            )
            tempStats = streakService.updateStreak(tempStats, true)
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.TASK,
                    referenceId = task.id,
                    xpChange = xpGained,
                    coinChange = coinsGained,
                    createdAt = System.currentTimeMillis()
                )
            )
            questEngine.triggerQuestEngineForTask(task)
            questEngine.triggerQuestEngineForXp(xpGained, lvl, tempStats.streak)

            val finalStats = achievementService.checkAchievements(tempStats, emptyList())
            userRepository.saveUserStats(finalStats)
        }
    }

    suspend fun updateTask(updatedTask: PriorityTask, oldTask: PriorityTask) {
        val wasCompleted = oldTask.completed
        val isNowCompleted = updatedTask.completed
        
        val xpDiff = if (isNowCompleted && !wasCompleted) {
            xpService.calculateTaskXp(updatedTask)
        } else if (!isNowCompleted && wasCompleted) {
            -oldTask.xpReward
        } else {
            0
        }
        val coinDiff = if (isNowCompleted && !wasCompleted) {
            coinService.calculateTaskCoins(updatedTask)
        } else if (!isNowCompleted && wasCompleted) {
            -oldTask.coinReward
        } else {
            0
        }
        
        taskRepository.updateTask(updatedTask)
        
        if (xpDiff != 0 || coinDiff != 0) {
            val stats = userRepository.getUserStatsDirect() ?: UserStats()
            val (lvl, xp) = levelService.checkLevelUp(stats.level, stats.xp, xpDiff)
            var tempStats = stats.copy(
                level = lvl,
                xp = xp,
                coins = (stats.coins + coinDiff).coerceAtLeast(0),
                totalXp = stats.totalXp + xpDiff,
                todayXpEarned = (stats.todayXpEarned + xpDiff).coerceAtLeast(0),
                todayCoinsEarned = (stats.todayCoinsEarned + coinDiff).coerceAtLeast(0)
            )
            
            if (isNowCompleted && !wasCompleted) {
                tempStats = streakService.updateStreak(tempStats, true)
                statisticsRepository.insertLog(
                    ActivityLogEntity(
                        id = UUID.randomUUID().toString(),
                        type = ActivityLogType.TASK,
                        referenceId = updatedTask.id,
                        xpChange = xpDiff,
                        coinChange = coinDiff,
                        createdAt = System.currentTimeMillis()
                    )
                )
                questEngine.triggerQuestEngineForTask(updatedTask)
                questEngine.triggerQuestEngineForXp(xpDiff, lvl, tempStats.streak)
            }

            val finalStats = achievementService.checkAchievements(tempStats, emptyList())
            userRepository.saveUserStats(finalStats)
        }
    }

    suspend fun addHabit(habit: Habit) {
        habitRepository.insertHabit(habit)
    }

    suspend fun updateHabitDetails(updatedHabit: Habit, oldHabit: Habit) {
        val wasCompleted = oldHabit.completed
        val isNowCompleted = updatedHabit.completed

        val xpDiff = if (isNowCompleted && !wasCompleted) {
            xpService.calculateHabitXp(updatedHabit)
        } else if (!isNowCompleted && wasCompleted) {
            -oldHabit.xpReward
        } else {
            0
        }

        val coinsDiff = if (isNowCompleted && !wasCompleted) {
            coinService.calculateHabitCoins(updatedHabit)
        } else if (!isNowCompleted && wasCompleted) {
            val coinsToSubtract = if (oldHabit.isPenaltyEnabled) -oldHabit.coinReward else 0
            coinsToSubtract
        } else {
            0
        }

        habitRepository.updateHabit(updatedHabit)

        if (xpDiff != 0 || coinsDiff != 0) {
            val stats = userRepository.getUserStatsDirect() ?: UserStats()
            val (lvl, xp) = levelService.checkLevelUp(stats.level, stats.xp, xpDiff)
            var tempStats = stats.copy(
                level = lvl,
                xp = xp,
                coins = (stats.coins + coinsDiff).coerceAtLeast(0),
                totalXp = stats.totalXp + xpDiff,
                todayXpEarned = (stats.todayXpEarned + xpDiff).coerceAtLeast(0),
                todayCoinsEarned = (stats.todayCoinsEarned + coinsDiff).coerceAtLeast(0)
            )

            if (isNowCompleted && !wasCompleted) {
                tempStats = streakService.updateStreak(tempStats, true)
                statisticsRepository.insertLog(
                    ActivityLogEntity(
                        id = UUID.randomUUID().toString(),
                        type = ActivityLogType.HABIT,
                        referenceId = updatedHabit.id,
                        xpChange = xpDiff,
                        coinChange = coinsDiff,
                        createdAt = System.currentTimeMillis()
                    )
                )
                questEngine.triggerQuestEngineForHabit(updatedHabit, updatedHabit.currentValue - oldHabit.currentValue)
                questEngine.triggerQuestEngineForXp(xpDiff, lvl, tempStats.streak)
            }

            val finalStats = achievementService.checkAchievements(tempStats, emptyList())
            userRepository.saveUserStats(finalStats)
        }
    }

    suspend fun addQuest(quest: Quest, allQuests: List<Quest>) {
        if (quest.type == QuestType.MAIN) {
            allQuests.filter { it.type == QuestType.MAIN }.forEach {
                questRepository.updateQuest(it.copy(type = QuestType.SPECIAL))
            }
        }
        questRepository.insertQuest(quest)
    }

    suspend fun updateQuestDetails(updatedQuest: Quest, oldQuest: Quest, allQuests: List<Quest>) {
        val wasCompleted = oldQuest.status == QuestStatus.COMPLETED
        val isNowCompleted = updatedQuest.status == QuestStatus.COMPLETED

        if (updatedQuest.type == QuestType.MAIN) {
            allQuests.filter { it.type == QuestType.MAIN && it.id != updatedQuest.id }.forEach {
                questRepository.updateQuest(it.copy(type = QuestType.SPECIAL))
            }
        }

        questRepository.updateQuest(updatedQuest)

        if (isNowCompleted && !wasCompleted) {
            statisticsRepository.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    type = ActivityLogType.QUEST,
                    referenceId = updatedQuest.id,
                    xpChange = updatedQuest.xpReward,
                    coinChange = updatedQuest.coinReward,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
