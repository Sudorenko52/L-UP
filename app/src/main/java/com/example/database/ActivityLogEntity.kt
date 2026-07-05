package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.example.ActivityLogType

@Entity(
    tableName = "activity_logs",
    indices = [Index(value = ["createdAt"])]
)
data class ActivityLogEntity(
    @PrimaryKey val id: String, // UUID
    val type: ActivityLogType, // TASK, HABIT, QUEST, ACHIEVEMENT
    val referenceId: String,
    val xpChange: Int,
    val coinChange: Int,
    val createdAt: Long // UTC
)
