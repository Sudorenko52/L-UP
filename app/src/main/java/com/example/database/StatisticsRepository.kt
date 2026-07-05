package com.example.database

import kotlinx.coroutines.flow.Flow

class StatisticsRepository(private val db: AppDatabase) {
    private val activityLogDao = db.activityLogDao()

    val allLogsFlow: Flow<List<ActivityLogEntity>> = activityLogDao.getAllLogsFlow()

    suspend fun insertLog(log: ActivityLogEntity) {
        activityLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        activityLogDao.clearLogs()
    }
}
