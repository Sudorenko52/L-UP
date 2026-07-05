package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.QuestType
import com.example.QuestStatus
import com.example.RewardChest

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
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["type"]),
        Index(value = ["createdAt"])
    ]
)
data class QuestEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String, // User 1→N Quests
    val title: String,
    val type: QuestType, // MAIN, WEEKLY, MONTHLY, SPECIAL
    val target: Float, // targetValue
    val progress: Float, // currentValue
    val xpReward: Int,
    val coinReward: Int,
    val status: QuestStatus, // ACTIVE, COMPLETED, EXPIRED
    val isManual: Boolean, // isManual
    val createdAt: Long, // UTC
    val completedAt: Long?, // UTC
    
    // Additional fields to maintain Quest state in UI
    val description: String,
    val chest: RewardChest, // RewardChest name
    val targetType: String,
    val durationText: String,
    val tagsJson: String
)
