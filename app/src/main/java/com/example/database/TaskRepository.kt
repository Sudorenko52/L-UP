package com.example.database

import com.example.PriorityTask
import com.example.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

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
