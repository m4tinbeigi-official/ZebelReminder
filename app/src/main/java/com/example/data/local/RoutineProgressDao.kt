package com.example.data.local

import androidx.room.*
import com.example.data.model.RoutineProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineProgressDao {
    @Query("SELECT * FROM routine_progress ORDER BY targetDate DESC, completedAt DESC")
    fun getAllProgress(): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE targetDate = :dateString")
    fun getProgressByDate(dateString: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE routineId = :routineId")
    fun getProgressByRoutine(routineId: String): Flow<List<RoutineProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: RoutineProgressEntity): Long

    @Update
    suspend fun updateProgress(progress: RoutineProgressEntity)

    @Delete
    suspend fun deleteProgress(progress: RoutineProgressEntity)

    @Query("DELETE FROM routine_progress WHERE id = :id")
    suspend fun deleteProgressById(id: Int)

    @Query("DELETE FROM routine_progress")
    suspend fun deleteAllProgress()

    // --- NEWLY REQUESTED DAO METHODS ---

    @Query("SELECT * FROM routine_progress WHERE targetDate = :today")
    fun getTodayProgress(today: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE targetDate BETWEEN :startDate AND :endDate")
    fun getThisWeekProgress(startDate: String, endDate: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE targetDate LIKE :monthPrefix")
    fun getThisMonthProgress(monthPrefix: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE taskId = :taskId")
    fun getProgressByTaskId(taskId: Int): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE targetDate BETWEEN :startDate AND :endDate")
    fun getProgressBetweenDates(startDate: String, endDate: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT * FROM routine_progress WHERE routineCategory = :category")
    fun getProgressByCategory(category: String): Flow<List<RoutineProgressEntity>>

    @Query("SELECT COUNT(*) FROM routine_progress WHERE taskId = :taskId AND periodType = :periodType AND targetDate = :targetDate")
    suspend fun checkProgressExistsForTaskInPeriod(taskId: Int, periodType: String, targetDate: String): Int
}
