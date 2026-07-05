package com.example.database

import com.example.Quest
import com.example.QuestType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

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
