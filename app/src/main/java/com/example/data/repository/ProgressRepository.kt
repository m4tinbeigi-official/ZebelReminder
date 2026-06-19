package com.example.data.repository

import com.example.data.local.RoutineProgressDao
import com.example.data.model.RoutineProgressEntity
import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val routineProgressDao: RoutineProgressDao) {

    val allProgress: Flow<List<RoutineProgressEntity>> = routineProgressDao.getAllProgress()

    fun getProgressByDate(dateString: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getProgressByDate(dateString)
    }

    fun getProgressByRoutine(routineId: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getProgressByRoutine(routineId)
    }

    suspend fun insertProgress(progress: RoutineProgressEntity): Long {
        return routineProgressDao.insertProgress(progress)
    }

    suspend fun updateProgress(progress: RoutineProgressEntity) {
        routineProgressDao.updateProgress(progress)
    }

    suspend fun deleteProgress(progress: RoutineProgressEntity) {
        routineProgressDao.deleteProgress(progress)
    }

    suspend fun deleteProgressById(id: Int) {
        routineProgressDao.deleteProgressById(id)
    }

    suspend fun deleteAllProgress() {
        routineProgressDao.deleteAllProgress()
    }

    // --- NEWLY REQUESTED REPOSITORY METHODS ---

    fun getTodayProgress(today: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getTodayProgress(today)
    }

    fun getThisWeekProgress(startDate: String, endDate: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getThisWeekProgress(startDate, endDate)
    }

    fun getThisMonthProgress(monthPrefix: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getThisMonthProgress(monthPrefix)
    }

    fun getProgressByTaskId(taskId: Int): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getProgressByTaskId(taskId)
    }

    fun getProgressBetweenDates(startDate: String, endDate: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getProgressBetweenDates(startDate, endDate)
    }

    fun getProgressByCategory(category: String): Flow<List<RoutineProgressEntity>> {
        return routineProgressDao.getProgressByCategory(category)
    }

    suspend fun checkProgressExistsForTaskInPeriod(taskId: Int, periodType: String, targetDate: String): Boolean {
        return routineProgressDao.checkProgressExistsForTaskInPeriod(taskId, periodType, targetDate) > 0
    }
}
