package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurrenceType(val title: String) {
    ONE_TIME("فقط یکبار"),
    DAILY("هر روز"),
    WEEKLY("هر هفته"),
    MONTHLY("هر ماه"),
    YEARLY("هر سال"),
    CUSTOM_WEEKDAYS("روزهای خاص هفته"),
    EVERY_X_DAYS("هر چند روز یکبار")
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val category: String? = null,
    val recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
    val dueDate: String, // format YYYY-MM-DD or Solar Hijri
    val dueTime: String, // format HH:MM
    val selectedWeekdays: String? = null, // e.g., "1,3,5" representing Sat, Mon, Wed
    val customIntervalDays: Int? = null, // for EVERY_X_DAYS
    val snoozeMinutes: Int = 5,
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val lastCompletedAt: Long? = null,
    val nextReminderAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isRoutine: Boolean = false,
    val routineCategory: String? = null,
    val routineGoalType: String? = null,
    val routineTargetCount: Int? = null,
    val routinePeriod: String? = null,
    val routineSuggestionId: String? = null
)
