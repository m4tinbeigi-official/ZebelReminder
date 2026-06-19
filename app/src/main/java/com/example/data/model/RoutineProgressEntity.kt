package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_progress")
data class RoutineProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskId: Int,
    val routineTitle: String,
    val routineCategory: String,
    val completedAt: Long,
    val periodType: String,       // "daily", "weekly", "monthly"
    val targetDate: String,       // format "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis(),
    val routineId: String = ""    // For matching with RoutineSuggestion.id (e.g., "drink_water")
) {
    val title: String get() = routineTitle
    val dateString: String get() = targetDate
    val isCompleted: Boolean get() = true
    val progressValue: Int get() = 1
    val targetValue: Int get() = 1
    val timestamp: Long get() = completedAt
}
