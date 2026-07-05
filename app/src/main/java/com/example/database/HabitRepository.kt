package com.example.database

import com.example.Habit
import com.example.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

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
