package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.HabitStatus

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
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
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
    val status: HabitStatus, // e.g., ACTIVE, INACTIVE
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
