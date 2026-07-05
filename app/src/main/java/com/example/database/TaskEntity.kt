package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.PriorityLevel
import com.example.TaskStatus

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
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["createdAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String, // User 1→N Tasks
    val title: String,
    val description: String,
    val priority: PriorityLevel, // CRITICAL, IMPORTANT, NORMAL
    val xp: Int,
    val coins: Int,
    val dueDate: String, // dueDate as "Today", "Tomorrow", "Upcoming" or date string
    val dueTime: String, // dueTime e.g., "18:00"
    val repeatType: String, // Once, Daily, etc. (repeatType)
    val status: TaskStatus, // ACTIVE, COMPLETED
    val createdAt: Long, // UTC
    val completedAt: Long?, // UTC
    
    // Additional field for UI tracking
    val progress: Float,
    val iconName: String
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String, // UUID
    val name: String,
    val createdAt: Long // UTC
)

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
