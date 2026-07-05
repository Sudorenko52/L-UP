package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserDirect(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    // Task tags N:M helpers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskTag(crossRef: TaskTagCrossRef)

    @Query("DELETE FROM task_tags WHERE taskId = :taskId")
    suspend fun deleteTagsForTask(taskId: String)

    @Query("SELECT t.* FROM tags t INNER JOIN task_tags tt ON t.id = tt.tagId WHERE tt.taskId = :taskId")
    fun getTagsForTaskFlow(taskId: String): Flow<List<TagEntity>>
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: String)

    // Habit tags N:M helpers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitTag(crossRef: HabitTagCrossRef)

    @Query("DELETE FROM habit_tags WHERE habitId = :habitId")
    suspend fun deleteTagsForHabit(habitId: String)

    @Query("SELECT t.* FROM tags t INNER JOIN habit_tags ht ON t.id = ht.tagId WHERE ht.habitId = :habitId")
    fun getTagsForHabitFlow(habitId: String): Flow<List<TagEntity>>
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY createdAt DESC")
    fun getAllQuestsFlow(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests")
    suspend fun getAllQuestsDirect(): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: String): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun deleteQuestById(id: String)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY createdAt DESC")
    fun getAllLogsFlow(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTagsFlow(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: String)
}
