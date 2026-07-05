package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

// ---------------------------------------------------------------------------------
// 9.3 Таблиця User
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "users",
    indices = [Index(value = ["id"])]
)
data class UserEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val avatar: String, // avatar string representation or avatarId
    val level: Int,
    val xp: Int,
    val coins: Int,
    val currentStreak: Int,
    val createdAt: Long, // UTC timestamp
    val updatedAt: Long, // UTC timestamp
    
    // Additional fields for complete userStats state persistence
    val selectedTitle: String,
    val unlockedTitlesJson: String, // comma-separated or serialized
    val unlockedBadgesJson: String, // comma-separated or serialized
    val totalXp: Int,
    val todayXpEarned: Int = 65,
    val todayCoinsEarned: Int = 18
)

// ---------------------------------------------------------------------------------
// 9.4 Таблиця Tasks
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class TaskEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String, // User 1→N Tasks
    val title: String,
    val description: String,
    val priority: String, // CRITICAL, IMPORTANT, NORMAL
    val xp: Int,
    val coins: Int,
    val dueDate: String, // dueDate as "Today", "Tomorrow", "Upcoming" or date string
    val dueTime: String, // dueTime e.g., "18:00"
    val repeatType: String, // Once, Daily, etc. (repeatType)
    val status: String, // "Active", "Completed"
    val createdAt: Long, // UTC
    val completedAt: Long?, // UTC
    
    // Additional field for UI tracking
    val progress: Float,
    val iconName: String
)

// ---------------------------------------------------------------------------------
// 9.5 Таблиця Habits
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class HabitEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String, // User 1→N Habits
    val title: String,
    val icon: String, // iconName
    val schedule: String, // repeat (schedule)
    val xp: Int,
    val coins: Int,
    val coinPenalty: Int, // coinPenalty
    val status: String, // e.g., "Active"
    val createdAt: Long, // UTC
    val lastCompletedAt: Long?, // UTC
    
    // Additional fields to maintain Habit state in UI
    val colorHex: Long,
    val currentValue: Float,
    val maxValue: Float,
    val unit: String,
    val subtitle: String,
    val completed: Boolean,
    val incrementStep: Float,
    val isPenaltyEnabled: Boolean,
    val description: String
)

// ---------------------------------------------------------------------------------
// 9.6 Таблиця Quests
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "quests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class QuestEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String, // User 1→N Quests
    val title: String,
    val type: String, // MAIN, WEEKLY, MONTHLY, SPECIAL
    val target: Float, // targetValue
    val progress: Float, // currentValue
    val xpReward: Int,
    val coinReward: Int,
    val status: String, // ACTIVE, COMPLETED, EXPIRED
    val isManual: Boolean, // isManual
    val createdAt: Long, // UTC
    val completedAt: Long?, // UTC
    
    // Additional fields to maintain Quest state in UI
    val description: String,
    val chest: String, // RewardChest name
    val targetType: String,
    val durationText: String,
    val tagsJson: String
)

// ---------------------------------------------------------------------------------
// 9.7 Таблиця Achievements
// ---------------------------------------------------------------------------------
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // UUID
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? // UTC
)

// ---------------------------------------------------------------------------------
// 9.8 Таблиця ActivityLog
// ---------------------------------------------------------------------------------
// Business rule: "Видалення Tasks/Habits не видаляє ActivityLog."
// Therefore, we do not define cascading foreign keys here, referenceId is stored purely as a reference string.
@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String, // UUID
    val type: String, // "TASK", "HABIT", "QUEST", "ACHIEVEMENT"
    val referenceId: String,
    val xpChange: Int,
    val coinChange: Int,
    val createdAt: Long // UTC
)

// ---------------------------------------------------------------------------------
// Settings Таблиця
// ---------------------------------------------------------------------------------
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: String, // UUID (usually single row)
    val soundEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val hapticFeedback: Boolean,
    val createdAt: Long, // UTC
    val updatedAt: Long  // UTC
)

// ---------------------------------------------------------------------------------
// Tags Таблиця
// ---------------------------------------------------------------------------------
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val createdAt: Long // UTC
)

// ---------------------------------------------------------------------------------
// TaskTags Cross Reference (N↔N Relationships)
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "task_tags",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["tagId"])]
)
data class TaskTagCrossRef(
    val taskId: String,
    val tagId: String
)

// ---------------------------------------------------------------------------------
// HabitTags Cross Reference (N↔N Relationships)
// ---------------------------------------------------------------------------------
@Entity(
    tableName = "habit_tags",
    primaryKeys = ["habitId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["habitId"]), Index(value = ["tagId"])]
)
data class HabitTagCrossRef(
    val habitId: String,
    val tagId: String
)
