package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        HabitEntity::class,
        QuestEntity::class,
        AchievementEntity::class,
        ActivityLogEntity::class,
        SettingsEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        HabitTagCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun questDao(): QuestDao
    abstract fun achievementDao(): AchievementDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun settingsDao(): SettingsDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDate` ON `tasks` (`dueDate`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_createdAt` ON `tasks` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_status` ON `habits` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_createdAt` ON `habits` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_status` ON `quests` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_type` ON `quests` (`type`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_createdAt` ON `quests` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_createdAt` ON `activity_logs` (`createdAt`)")
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_id` ON `users` (`id`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_userId` ON `tasks` (`userId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_status` ON `tasks` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_dueDate` ON `tasks` (`dueDate`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_createdAt` ON `tasks` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_userId` ON `habits` (`userId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_status` ON `habits` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habits_createdAt` ON `habits` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_userId` ON `quests` (`userId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_status` ON `quests` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_type` ON `quests` (`type`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_quests_createdAt` ON `quests` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_logs_createdAt` ON `activity_logs` (`createdAt`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_tags_taskId` ON `task_tags` (`taskId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_tags_tagId` ON `task_tags` (`tagId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_tags_habitId` ON `habit_tags` (`habitId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_habit_tags_tagId` ON `habit_tags` (`tagId`)")
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "levelup_bible_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
