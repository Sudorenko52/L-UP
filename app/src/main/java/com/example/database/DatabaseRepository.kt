package com.example.database

import com.example.UserStats
import com.example.PriorityTask
import com.example.Habit
import com.example.Quest
import com.example.PriorityLevel
import com.example.QuestType
import com.example.QuestStatus
import com.example.RewardChest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DatabaseRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val taskDao = db.taskDao()
    private val habitDao = db.habitDao()
    private val questDao = db.questDao()
    private val achievementDao = db.achievementDao()
    private val activityLogDao = db.activityLogDao()
    private val settingsDao = db.settingsDao()
    private val tagDao = db.tagDao()

    // ---------------------------------------------------------------------------------
    // User Stats Mapping & Actions
    // ---------------------------------------------------------------------------------
    val userStatsFlow: Flow<UserStats?> = userDao.getUserFlow().map { entity ->
        entity?.let {
            UserStats(
                name = it.name,
                level = it.level,
                xp = it.xp,
                maxXp = 1000, // standard max XP
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

    // ---------------------------------------------------------------------------------
    // Task Mapping & Actions
    // ---------------------------------------------------------------------------------
    val allTasksFlow: Flow<List<PriorityTask>> = taskDao.getAllTasksFlow().map { list ->
        list.map { entity ->
            PriorityTask(
                id = entity.id,
                title = entity.title,
                level = when (entity.priority.uppercase()) {
                    "CRITICAL" -> PriorityLevel.CRITICAL
                    "IMPORTANT" -> PriorityLevel.IMPORTANT
                    else -> PriorityLevel.NORMAL
                },
                iconName = entity.iconName,
                completed = entity.status == "Completed",
                description = entity.description,
                xpReward = entity.xp,
                coinReward = entity.coins,
                time = entity.dueTime,
                date = entity.dueDate,
                progress = entity.progress,
                tags = if (entity.repeatType.contains("|")) entity.repeatType.split("|").filter { it.isNotEmpty() } else emptyList(), // we can encode task tags or rely on cross refs
                repeat = if (entity.repeatType.contains("|")) "Once" else entity.repeatType,
                status = entity.status
            )
        }
    }

    suspend fun insertTask(task: PriorityTask) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"
        
        // Ensure user exists
        if (user == null) {
            saveUserStats(UserStats())
        }

        // We can save tags in DB and cross references
        val tagsString = task.tags.joinToString("|")

        val entity = TaskEntity(
            id = task.id.ifEmpty { UUID.randomUUID().toString() },
            userId = userId,
            title = task.title,
            description = task.description,
            priority = task.level.name,
            xp = task.xpReward,
            coins = task.coinReward,
            dueDate = task.date,
            dueTime = task.time,
            repeatType = if (task.tags.isNotEmpty()) "${task.repeat}|$tagsString" else task.repeat,
            status = if (task.completed) "Completed" else "Active",
            createdAt = System.currentTimeMillis(),
            completedAt = if (task.completed) System.currentTimeMillis() else null,
            progress = task.progress,
            iconName = task.iconName
        )
        taskDao.insertTask(entity)

        // Save N:M Tags and Cross References
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
            priority = task.level.name,
            xp = task.xpReward,
            coins = task.coinReward,
            dueDate = task.date,
            dueTime = task.time,
            repeatType = if (task.tags.isNotEmpty()) "${task.repeat}|$tagsString" else task.repeat,
            status = if (task.completed) "Completed" else "Active",
            createdAt = System.currentTimeMillis(),
            completedAt = if (task.completed) System.currentTimeMillis() else null,
            progress = task.progress,
            iconName = task.iconName
        )
        taskDao.updateTask(entity)

        // Sync N:M Tags
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

    // ---------------------------------------------------------------------------------
    // Habit Mapping & Actions
    // ---------------------------------------------------------------------------------
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
            status = "Active",
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

        // Sync junction tags
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
            status = "Active",
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

        // Sync junction tags
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

    // ---------------------------------------------------------------------------------
    // Quest Mapping & Actions
    // ---------------------------------------------------------------------------------
    val allQuestsFlow: Flow<List<Quest>> = questDao.getAllQuestsFlow().map { list ->
        list.map { entity ->
            Quest(
                id = entity.id,
                title = entity.title,
                type = try { QuestType.valueOf(entity.type) } catch(e: Exception) { QuestType.SPECIAL },
                description = entity.description,
                targetValue = entity.target,
                currentValue = entity.progress,
                xpReward = entity.xpReward,
                coinReward = entity.coinReward,
                chest = try { RewardChest.valueOf(entity.chest) } catch(e: Exception) { RewardChest.NONE },
                status = try { QuestStatus.valueOf(entity.status) } catch(e: Exception) { QuestStatus.ACTIVE },
                targetType = entity.targetType,
                tags = if (entity.tagsJson.isEmpty()) emptyList() else entity.tagsJson.split("|"),
                createdAt = entity.createdAt,
                completedAt = entity.completedAt,
                durationText = entity.durationText
            )
        }
    }

    suspend fun insertQuest(quest: Quest) {
        val user = userDao.getUserDirect()
        val userId = user?.id ?: "default_user_id"

        val entity = QuestEntity(
            id = quest.id.ifEmpty { UUID.randomUUID().toString() },
            userId = userId,
            title = quest.title,
            type = quest.type.name,
            target = quest.targetValue,
            progress = quest.currentValue,
            xpReward = quest.xpReward,
            coinReward = quest.coinReward,
            status = quest.status.name,
            isManual = quest.type == QuestType.SPECIAL,
            createdAt = quest.createdAt,
            completedAt = quest.completedAt,
            description = quest.description,
            chest = quest.chest.name,
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
            type = quest.type.name,
            target = quest.targetValue,
            progress = quest.currentValue,
            xpReward = quest.xpReward,
            coinReward = quest.coinReward,
            status = quest.status.name,
            isManual = quest.type == QuestType.SPECIAL,
            createdAt = quest.createdAt,
            completedAt = quest.completedAt,
            description = quest.description,
            chest = quest.chest.name,
            targetType = quest.targetType,
            durationText = quest.durationText,
            tagsJson = quest.tags.joinToString("|")
        )
        questDao.updateQuest(entity)
    }

    suspend fun deleteQuest(id: String) {
        questDao.deleteQuestById(id)
    }

    // ---------------------------------------------------------------------------------
    // Activity Log Mapping & Actions
    // ---------------------------------------------------------------------------------
    val allLogsFlow: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogsFlow()

    suspend fun insertLog(log: ActivityLogEntity) {
        activityLogDao.insertLog(log)
    }

    // ---------------------------------------------------------------------------------
    // Settings Mapping & Actions
    // ---------------------------------------------------------------------------------
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
}
