package com.example.database

import com.example.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val settingsDao = db.settingsDao()
    private val achievementDao = db.achievementDao()

    val userStatsFlow: Flow<UserStats?> = userDao.getUserFlow().map { entity ->
        entity?.let {
            UserStats(
                name = it.name,
                level = it.level,
                xp = it.xp,
                maxXp = 1000,
                coins = it.coins,
                streak = it.currentStreak,
                selectedTitle = it.selectedTitle,
                unlockedTitles = if (it.unlockedTitlesJson.isEmpty()) emptyList() else it.unlockedTitlesJson.split("|"),
                unlockedBadges = if (it.unlockedBadgesJson.isEmpty()) emptyList() else it.unlockedBadgesJson.split("|"),
                totalXp = it.totalXp,
                avatarId = it.avatar.toIntOrNull() ?: 0,
                todayXpEarned = it.todayXpEarned,
                todayCoinsEarned = it.todayCoinsEarned
            )
        }
    }

    suspend fun getUserStatsDirect(): UserStats? {
        val entity = userDao.getUserDirect() ?: return null
        return UserStats(
            name = entity.name,
            level = entity.level,
            xp = entity.xp,
            maxXp = 1000,
            coins = entity.coins,
            streak = entity.currentStreak,
            selectedTitle = entity.selectedTitle,
            unlockedTitles = if (entity.unlockedTitlesJson.isEmpty()) emptyList() else entity.unlockedTitlesJson.split("|"),
            unlockedBadges = if (entity.unlockedBadgesJson.isEmpty()) emptyList() else entity.unlockedBadgesJson.split("|"),
            totalXp = entity.totalXp,
            avatarId = entity.avatar.toIntOrNull() ?: 0,
            todayXpEarned = entity.todayXpEarned,
            todayCoinsEarned = entity.todayCoinsEarned
        )
    }

    suspend fun saveUserStats(stats: UserStats) {
        val existing = userDao.getUserDirect()
        val userId = existing?.id ?: "default_user_id"
        val entity = UserEntity(
            id = userId,
            name = stats.name,
            avatar = stats.avatarId.toString(),
            level = stats.level,
            xp = stats.xp,
            coins = stats.coins,
            currentStreak = stats.streak,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            selectedTitle = stats.selectedTitle,
            unlockedTitlesJson = stats.unlockedTitles.joinToString("|"),
            unlockedBadgesJson = stats.unlockedBadges.joinToString("|"),
            totalXp = stats.totalXp,
            todayXpEarned = stats.todayXpEarned,
            todayCoinsEarned = stats.todayCoinsEarned
        )
        if (existing != null) {
            userDao.updateUser(entity)
        } else {
            userDao.insertUser(entity)
        }
    }

    val settingsFlow: Flow<SettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun saveSettings(sound: Boolean, notifications: Boolean, haptic: Boolean) {
        val entity = SettingsEntity(
            id = "default_settings",
            soundEnabled = sound,
            notificationsEnabled = notifications,
            hapticFeedback = haptic,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        settingsDao.insertSettings(entity)
    }

    val allAchievementsFlow: Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()

    suspend fun updateAchievement(achievement: AchievementEntity) {
        achievementDao.updateAchievement(achievement)
    }

    suspend fun insertAchievements(achievements: List<AchievementEntity>) {
        achievementDao.insertAchievements(achievements)
    }
}
