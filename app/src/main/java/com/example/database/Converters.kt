package com.example.database

import androidx.room.TypeConverter
import com.example.*

class Converters {
    @TypeConverter
    fun fromPriorityLevel(value: PriorityLevel): String = value.name

    @TypeConverter
    fun toPriorityLevel(value: String): PriorityLevel = try {
        PriorityLevel.valueOf(value)
    } catch (e: Exception) {
        PriorityLevel.NORMAL
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (e: Exception) {
        TaskStatus.ACTIVE
    }

    @TypeConverter
    fun fromQuestType(value: QuestType): String = value.name

    @TypeConverter
    fun toQuestType(value: String): QuestType = try {
        QuestType.valueOf(value)
    } catch (e: Exception) {
        QuestType.SPECIAL
    }

    @TypeConverter
    fun fromQuestStatus(value: QuestStatus): String = value.name

    @TypeConverter
    fun toQuestStatus(value: String): QuestStatus = try {
        QuestStatus.valueOf(value)
    } catch (e: Exception) {
        QuestStatus.ACTIVE
    }

    @TypeConverter
    fun fromRewardChest(value: RewardChest): String = value.name

    @TypeConverter
    fun toRewardChest(value: String): RewardChest = try {
        RewardChest.valueOf(value)
    } catch (e: Exception) {
        RewardChest.NONE
    }

    @TypeConverter
    fun fromHabitStatus(value: HabitStatus): String = value.name

    @TypeConverter
    fun toHabitStatus(value: String): HabitStatus = try {
        HabitStatus.valueOf(value)
    } catch (e: Exception) {
        HabitStatus.ACTIVE
    }

    @TypeConverter
    fun fromActivityLogType(value: ActivityLogType): String = value.name

    @TypeConverter
    fun toActivityLogType(value: String): ActivityLogType = try {
        ActivityLogType.valueOf(value)
    } catch (e: Exception) {
        ActivityLogType.TASK
    }
}
