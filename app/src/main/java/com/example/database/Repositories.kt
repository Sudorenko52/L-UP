package com.example.database

import com.example.UserStats
import com.example.PriorityTask
import com.example.Habit
import com.example.Quest
import com.example.PriorityLevel
import com.example.QuestType
import com.example.QuestStatus
import com.example.RewardChest
import com.example.TaskStatus
import com.example.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// =====================================================================================
// TASK REPOSITORY
// =====================================================================================
class TaskRepository(private val db: AppDatabase) {
    private val taskDao = db.taskDao()
    private val tagDao = db.tagDao()
    private val userDao = db.userDao()

    val allTasksFlow: Flow<List<PriorityTask>> = taskDao.getAllTasksFlow().map { list ->
        list.map { entity ->
            PriorityTask(
                id = entity.id,
                title = entity.title,
                level = entity.priority,
                iconName = entity.iconName,
                completed = entity.status == TaskStatus.COMPLETED,
                description = entity.description,
                xpReward = entity.xp,
                coinReward = entity.coins,
                time = entity.dueTime,
                date = entity.dueDate,
                progress = entity.progress,
                tags = if (entity.repeatType.contains("|")) entity.repeatType.split("|").filter { it.isNotEmpty() }.drop(1) else emptyList(),
                repeat = if (entity.repeatType.contains("|")) entity.repeatType.split("|").first() else entity.repeatType,
                status = entity.status
            )
        }
    }

    suspend fun getTaskById(id: String): PriorityTask? {
        val entity = taskDao.getTaskById(id) ?: return null
        return PriorityTask(
            id = entity.id,
            title = entity.title,
            level = entity.priority,
            iconName = entity.iconName,
            completed = entity.status == TaskStatus.COMPLETED,
            description = entity.description,
            xpReward = entity.xp,
            coinReward = entity.coins,
            time = entity.dueTime,
            date = entity.dueDate,
            progress = entity.progress,
            tags = if (entity.repeatType.contains("|")) entity.repeatType.split("|").filter { it.isNotEmpty() }.drop(1) else emptyList(),
            repeat = if (entity.repeatType.contains("|")) entity.repeatType.split("|").first() else entity.repeatType,
            status = entity.status
        )
    }

    suspend fun insertTask(task: PriorityTask) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"
        val tagsString = task.tags.joinToString("|")

        val entity = TaskEntity(
            id = task.id.ifEmpty { UUID.randomUUID().toString() },
            userId = userId,
            title = task.title,
            description = task.description,
            priority = task.level,
            xp = task.xpReward,
            coins = task.coinReward,
            dueDate = task.date,
            dueTime = task.time,
            repeatType = if (task.tags.isNotEmpty()) "${task.repeat}|$tagsString" else task.repeat,
            status = if (task.completed) TaskStatus.COMPLETED else TaskStatus.ACTIVE,
            createdAt = System.currentTimeMillis(),
            completedAt = if (task.completed) System.currentTimeMillis() else null,
            progress = task.progress,
            iconName = task.iconName
        )
        taskDao.insertTask(entity)

        for (tagName in task.tags) {
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                tag = TagEntity(id = UUID.randomUUID().toString(), name = tagName, createdAt = System.currentTimeMillis())
                tagDao.insertTag(tag)
            }
            taskDao.insertTaskTag(TaskTagCrossRef(taskId = entity.id, tagId = tag.id))
        }
    }

    suspend fun updateTask(task: PriorityTask) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"
        val tagsString = task.tags.joinToString("|")

        val entity = TaskEntity(
            id = task.id,
            userId = userId,
            title = task.title,
            description = task.description,
            priority = task.level,
            xp = task.xpReward,
            coins = task.coinReward,
            dueDate = task.date,
            dueTime = task.time,
            repeatType = if (task.tags.isNotEmpty()) "${task.repeat}|$tagsString" else task.repeat,
            status = if (task.completed) TaskStatus.COMPLETED else TaskStatus.ACTIVE,
            createdAt = System.currentTimeMillis(),
            completedAt = if (task.completed) System.currentTimeMillis() else null,
            progress = task.progress,
            iconName = task.iconName
        )
        taskDao.updateTask(entity)

        taskDao.deleteTagsForTask(task.id)
        for (tagName in task.tags) {
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                tag = TagEntity(id = UUID.randomUUID().toString(), name = tagName, createdAt = System.currentTimeMillis())
                tagDao.insertTag(tag)
            }
            taskDao.insertTaskTag(TaskTagCrossRef(taskId = task.id, tagId = tag.id))
        }
    }

    suspend fun deleteTask(id: String) {
        taskDao.deleteTagsForTask(id)
        taskDao.deleteTaskById(id)
    }
}

// =====================================================================================
// HABIT REPOSITORY
// =====================================================================================
class HabitRepository(private val db: AppDatabase) {
    private val habitDao = db.habitDao()
    private val tagDao = db.tagDao()
    private val userDao = db.userDao()

    val allHabitsFlow: Flow<List<Habit>> = habitDao.getAllHabitsFlow().map { list ->
        list.map { entity ->
            val tagParts = if (entity.schedule.contains("|")) entity.schedule.split("|") else listOf(entity.schedule)
            val sched = tagParts.firstOrNull() ?: "Every Day"
            val tagsList = if (tagParts.size > 1) tagParts[1].split(",").filter { it.isNotEmpty() } else emptyList()

            Habit(
                id = entity.id,
                title = entity.title,
                iconName = entity.icon,
                colorHex = entity.colorHex,
                currentValue = entity.currentValue,
                maxValue = entity.maxValue,
                unit = entity.unit,
                subtitle = entity.subtitle,
                completed = entity.completed,
                incrementStep = entity.incrementStep,
                description = entity.description,
                repeat = sched,
                xpReward = entity.xp,
                coinReward = entity.coins,
                isPenaltyEnabled = entity.isPenaltyEnabled,
                tags = tagsList,
                createdAt = entity.createdAt
            )
        }
    }

    suspend fun getHabitById(id: String): Habit? {
        val entity = habitDao.getHabitById(id) ?: return null
        val tagParts = if (entity.schedule.contains("|")) entity.schedule.split("|") else listOf(entity.schedule)
        val sched = tagParts.firstOrNull() ?: "Every Day"
        val tagsList = if (tagParts.size > 1) tagParts[1].split(",").filter { it.isNotEmpty() } else emptyList()

        return Habit(
            id = entity.id,
            title = entity.title,
            iconName = entity.icon,
            colorHex = entity.colorHex,
            currentValue = entity.currentValue,
            maxValue = entity.maxValue,
            unit = entity.unit,
            subtitle = entity.subtitle,
            completed = entity.completed,
            incrementStep = entity.incrementStep,
            description = entity.description,
            repeat = sched,
            xpReward = entity.xp,
            coinReward = entity.coins,
            isPenaltyEnabled = entity.isPenaltyEnabled,
            tags = tagsList,
            createdAt = entity.createdAt
        )
    }

    suspend fun insertHabit(habit: Habit) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"
        val tagsJoined = habit.tags.joinToString(",")
        val scheduleWithTags = "${habit.repeat}|$tagsJoined"

        val entity = HabitEntity(
            id = habit.id.ifEmpty { UUID.randomUUID().toString() },
            userId = userId,
            title = habit.title,
            icon = habit.iconName,
            schedule = scheduleWithTags,
            xp = habit.xpReward,
            coins = habit.coinReward,
            coinPenalty = if (habit.isPenaltyEnabled) habit.coinReward else 0,
            status = HabitStatus.ACTIVE,
            createdAt = habit.createdAt,
            lastCompletedAt = if (habit.completed) System.currentTimeMillis() else null,
            colorHex = habit.colorHex,
            currentValue = habit.currentValue,
            maxValue = habit.maxValue,
            unit = habit.unit,
            subtitle = habit.subtitle,
            completed = habit.completed,
            incrementStep = habit.incrementStep,
            isPenaltyEnabled = habit.isPenaltyEnabled,
            description = habit.description
        )
        habitDao.insertHabit(entity)

        for (tagName in habit.tags) {
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                tag = TagEntity(id = UUID.randomUUID().toString(), name = tagName, createdAt = System.currentTimeMillis())
                tagDao.insertTag(tag)
            }
            habitDao.insertHabitTag(HabitTagCrossRef(habitId = entity.id, tagId = tag.id))
        }
    }

    suspend fun updateHabit(habit: Habit) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"
        val tagsJoined = habit.tags.joinToString(",")
        val scheduleWithTags = "${habit.repeat}|$tagsJoined"

        val entity = HabitEntity(
            id = habit.id,
            userId = userId,
            title = habit.title,
            icon = habit.iconName,
            schedule = scheduleWithTags,
            xp = habit.xpReward,
            coins = habit.coinReward,
            coinPenalty = if (habit.isPenaltyEnabled) habit.coinReward else 0,
            status = HabitStatus.ACTIVE,
            createdAt = habit.createdAt,
            lastCompletedAt = if (habit.completed) System.currentTimeMillis() else null,
            colorHex = habit.colorHex,
            currentValue = habit.currentValue,
            maxValue = habit.maxValue,
            unit = habit.unit,
            subtitle = habit.subtitle,
            completed = habit.completed,
            incrementStep = habit.incrementStep,
            isPenaltyEnabled = habit.isPenaltyEnabled,
            description = habit.description
        )
        habitDao.updateHabit(entity)

        habitDao.deleteTagsForHabit(habit.id)
        for (tagName in habit.tags) {
            var tag = tagDao.getTagByName(tagName)
            if (tag == null) {
                tag = TagEntity(id = UUID.randomUUID().toString(), name = tagName, createdAt = System.currentTimeMillis())
                tagDao.insertTag(tag)
            }
            habitDao.insertHabitTag(HabitTagCrossRef(habitId = habit.id, tagId = tag.id))
        }
    }

    suspend fun deleteHabit(id: String) {
        habitDao.deleteTagsForHabit(id)
        habitDao.deleteHabitById(id)
    }
}

// =====================================================================================
// QUEST REPOSITORY
// =====================================================================================
class QuestRepository(private val db: AppDatabase) {
    private val questDao = db.questDao()
    private val userDao = db.userDao()

    val allQuestsFlow: Flow<List<Quest>> = questDao.getAllQuestsFlow().map { list ->
        list.map { entity ->
            Quest(
                id = entity.id,
                title = entity.title,
                type = entity.type,
                description = entity.description,
                targetValue = entity.target,
                currentValue = entity.progress,
                xpReward = entity.xpReward,
                coinReward = entity.coinReward,
                chest = entity.chest,
                status = entity.status,
                targetType = entity.targetType,
                tags = if (entity.tagsJson.isEmpty()) emptyList() else entity.tagsJson.split("|"),
                createdAt = entity.createdAt,
                completedAt = entity.completedAt,
                durationText = entity.durationText
            )
        }
    }

    suspend fun getAllQuestsDirect(): List<Quest> {
        return questDao.getAllQuestsDirect().map { entity ->
            Quest(
                id = entity.id,
                title = entity.title,
                type = entity.type,
                description = entity.description,
                targetValue = entity.target,
                currentValue = entity.progress,
                xpReward = entity.xpReward,
                coinReward = entity.coinReward,
                chest = entity.chest,
                status = entity.status,
                targetType = entity.targetType,
                tags = if (entity.tagsJson.isEmpty()) emptyList() else entity.tagsJson.split("|"),
                createdAt = entity.createdAt,
                completedAt = entity.completedAt,
                durationText = entity.durationText
            )
        }
    }

    suspend fun getQuestById(id: String): Quest? {
        val entity = questDao.getQuestById(id) ?: return null
        return Quest(
            id = entity.id,
            title = entity.title,
            type = entity.type,
            description = entity.description,
            targetValue = entity.target,
            currentValue = entity.progress,
            xpReward = entity.xpReward,
            coinReward = entity.coinReward,
            chest = entity.chest,
            status = entity.status,
            targetType = entity.targetType,
            tags = if (entity.tagsJson.isEmpty()) emptyList() else entity.tagsJson.split("|"),
            createdAt = entity.createdAt,
            completedAt = entity.completedAt,
            durationText = entity.durationText
        )
    }

    suspend fun insertQuest(quest: Quest) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"

        val entity = QuestEntity(
            id = quest.id.ifEmpty { UUID.randomUUID().toString() },
            userId = userId,
            title = quest.title,
            type = quest.type,
            target = quest.targetValue,
            progress = quest.currentValue,
            xpReward = quest.xpReward,
            coinReward = quest.coinReward,
            status = quest.status,
            isManual = quest.type == QuestType.SPECIAL,
            createdAt = quest.createdAt,
            completedAt = quest.completedAt,
            description = quest.description,
            chest = quest.chest,
            targetType = quest.targetType,
            durationText = quest.durationText,
            tagsJson = quest.tags.joinToString("|")
        )
        questDao.insertQuest(entity)
    }

    suspend fun updateQuest(quest: Quest) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"

        val entity = QuestEntity(
            id = quest.id,
            userId = userId,
            title = quest.title,
            type = quest.type,
            target = quest.targetValue,
            progress = quest.currentValue,
            xpReward = quest.xpReward,
            coinReward = quest.coinReward,
            status = quest.status,
            isManual = quest.type == QuestType.SPECIAL,
            createdAt = quest.createdAt,
            completedAt = quest.completedAt,
            description = quest.description,
            chest = quest.chest,
            targetType = quest.targetType,
            durationText = quest.durationText,
            tagsJson = quest.tags.joinToString("|")
        )
        questDao.updateQuest(entity)
    }

    suspend fun deleteQuest(id: String) {
        questDao.deleteQuestById(id)
    }
}

// =====================================================================================
// USER REPOSITORY
// =====================================================================================
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

// =====================================================================================
// STATISTICS REPOSITORY
// =====================================================================================
class StatisticsRepository(private val db: AppDatabase) {
    private val activityLogDao = db.activityLogDao()

    val allLogsFlow: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogsFlow()

    suspend fun insertLog(log: ActivityLogEntity) {
        activityLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        activityLogDao.clearLogs()
    }
}
