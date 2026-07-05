package com.example

import com.example.services.XPService
import com.example.services.CoinService
import com.example.services.LevelService
import com.example.services.StreakService
import com.example.PriorityTask
import com.example.PriorityLevel
import com.example.Habit
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testXPService_calculatesCorrectXP() {
        val xpService = XPService()
        val task = PriorityTask(
            id = "test_task",
            title = "Test Task",
            level = PriorityLevel.CRITICAL,
            iconName = "Gym",
            xpReward = 30,
            coinReward = 5
        )
        val habit = Habit(
            id = "test_habit",
            title = "Test Habit",
            iconName = "Water",
            colorHex = 0xFF000000,
            currentValue = 0f,
            maxValue = 10f,
            unit = "ml",
            subtitle = "",
            xpReward = 15,
            coinReward = 3
        )

        assertEquals(30, xpService.calculateTaskXp(task))
        assertEquals(15, xpService.calculateHabitXp(habit))
    }

    @Test
    fun testCoinService_calculatesCorrectCoinsAndPenalty() {
        val coinService = CoinService()
        val task = PriorityTask(
            id = "test_task",
            title = "Test Task",
            level = PriorityLevel.CRITICAL,
            iconName = "Gym",
            xpReward = 30,
            coinReward = 5
        )
        val habit = Habit(
            id = "test_habit",
            title = "Test Habit",
            iconName = "Water",
            colorHex = 0xFF000000,
            currentValue = 0f,
            maxValue = 10f,
            unit = "ml",
            subtitle = "",
            xpReward = 15,
            coinReward = 3,
            isPenaltyEnabled = true
        )

        assertEquals(5, coinService.calculateTaskCoins(task))
        assertEquals(3, coinService.calculateHabitCoins(habit))
        assertEquals(3, coinService.calculateHabitPenalty(habit))

        val habitNoPenalty = habit.copy(isPenaltyEnabled = false)
        assertEquals(0, coinService.calculateHabitPenalty(habitNoPenalty))
    }

    @Test
    fun testLevelService_handlesLevelUpAndXpTransition() {
        val levelService = LevelService()

        // standard XP gain, no level up
        val (lvl1, xp1) = levelService.checkLevelUp(currentLevel = 1, currentXp = 500, xpGained = 200)
        assertEquals(1, lvl1)
        assertEquals(700, xp1)

        // exact transition to next level
        val (lvl2, xp2) = levelService.checkLevelUp(currentLevel = 1, currentXp = 800, xpGained = 200)
        assertEquals(2, lvl2)
        assertEquals(0, xp2)

        // carry over to next level
        val (lvl3, xp3) = levelService.checkLevelUp(currentLevel = 1, currentXp = 900, xpGained = 250)
        assertEquals(2, lvl3)
        assertEquals(150, xp3)

        // negative XP (penalty)
        val (lvl4, xp4) = levelService.checkLevelUp(currentLevel = 2, currentXp = 100, xpGained = -200)
        assertEquals(1, lvl4)
        assertEquals(900, xp4)
    }

    @Test
    fun testStreakService_updatesStreakCorrectly() {
        val streakService = StreakService()
        val stats = UserStats(streak = 5)

        // activity happened today, streak preserved or set
        val updatedStats = streakService.updateStreak(stats, activityHappenedToday = true)
        assertEquals(5, updatedStats.streak)

        // brand new streak
        val statsNew = UserStats(streak = 0)
        val updatedStatsNew = streakService.updateStreak(statsNew, activityHappenedToday = true)
        assertEquals(1, updatedStatsNew.streak)

        // increment streak manually
        val incremented = streakService.incrementStreak(stats)
        assertEquals(6, incremented.streak)
    }
}

