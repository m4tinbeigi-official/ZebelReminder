package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Task
import com.example.data.model.RoutineProgressEntity
import com.example.data.repository.TaskRepository
import com.example.data.repository.ProgressRepository
import com.example.util.DateUtils
import com.example.util.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val progressRepository by lazy {
        ProgressRepository(AppDatabase.getDatabase(getApplication()).routineProgressDao())
    }

    // Expose all tasks reactively
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Today's tasks (due today, active and not completed yet)
    val todayTasks: StateFlow<List<Task>> = repository.allTasks.map { list ->
        val today = DateUtils.getTodayDateString()
        list.filter {
            it.isActive && !it.isCompleted && it.dueDate == today && !DateUtils.isOverdue(it.dueDate, it.dueTime, it.isCompleted)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Upcoming tasks (due date in future, active and not completed)
    val upcomingTasks: StateFlow<List<Task>> = repository.allTasks.map { list ->
        val today = DateUtils.getTodayDateString()
        list.filter {
            it.isActive && !it.isCompleted && DateUtils.compareDates(it.dueDate, today) > 0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overdue tasks (due date/time before now, active and not completed)
    val overdueTasks: StateFlow<List<Task>> = repository.allTasks.map { list ->
        list.filter {
            it.isActive && !it.isCompleted && DateUtils.isOverdue(it.dueDate, it.dueTime, it.isCompleted)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completed tasks (completed or inactive)
    val completedTasks: StateFlow<List<Task>> = repository.allTasks.map { list ->
        list.filter { it.isCompleted || !it.isActive }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addTask(
        title: String,
        description: String,
        category: String?,
        recurrenceType: com.example.data.model.RecurrenceType,
        dueDate: String,
        dueTime: String,
        selectedWeekdays: String? = null,
        customIntervalDays: Int? = null,
        snoozeMinutes: Int = 10,
        isRoutine: Boolean = false,
        routineCategory: String? = null,
        routineGoalType: String? = null,
        routineTargetCount: Int? = null,
        routinePeriod: String? = null,
        routineSuggestionId: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                if (title.isBlank()) {
                    onFailure("عنوان یادآور نمی‌تواند خالی باشد.")
                    return@launch
                }
                val nextReminder = DateUtils.parseDateAndTimeToCalendar(dueDate, dueTime).timeInMillis
                val task = Task(
                    title = title,
                    description = description,
                    category = category,
                    recurrenceType = recurrenceType,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    selectedWeekdays = selectedWeekdays,
                    customIntervalDays = customIntervalDays,
                    snoozeMinutes = snoozeMinutes,
                    isCompleted = false,
                    isActive = true,
                    nextReminderAt = nextReminder,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isRoutine = isRoutine,
                    routineCategory = routineCategory,
                    routineGoalType = routineGoalType,
                    routineTargetCount = routineTargetCount,
                    routinePeriod = routinePeriod,
                    routineSuggestionId = routineSuggestionId
                )
                val id = repository.insertTask(task)
                val insertedTask = task.copy(id = id.toInt())
                ReminderScheduler.scheduleReminder(getApplication(), insertedTask)
                com.example.widget.RoutineWidgetProvider.triggerUpdate(getApplication())
                onSuccess()
            } catch (e: Exception) {
                onFailure("خطا در ثبت یادآور: ${e.localizedMessage}")
            }
        }
    }

    fun updateTask(
        task: Task,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                if (task.title.isBlank()) {
                    onFailure("عنوان یادآور نمی‌تواند خالی باشد.")
                    return@launch
                }
                val nextReminder = DateUtils.parseDateAndTimeToCalendar(task.dueDate, task.dueTime).timeInMillis
                val updatedTask = task.copy(
                    nextReminderAt = nextReminder,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                ReminderScheduler.scheduleReminder(getApplication(), updatedTask)
                com.example.widget.RoutineWidgetProvider.triggerUpdate(getApplication())
                onSuccess()
            } catch (e: Exception) {
                onFailure("خطا در به‌روزرسانی یادآور: ${e.localizedMessage}")
            }
        }
    }

    fun deleteTask(
        id: Int,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteTaskById(id)
                ReminderScheduler.cancelReminder(getApplication(), id)
                com.example.widget.RoutineWidgetProvider.triggerUpdate(getApplication())
                onSuccess()
            } catch (e: Exception) {
                onFailure("خطا در حذف یادآور: ${e.localizedMessage}")
            }
        }
    }

    fun toggleTaskCompletion(task: Task, onMessage: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val isCompletedNew = !task.isCompleted
                if (isCompletedNew) {
                    if (task.isRoutine) {
                        try {
                            val todayDate = DateUtils.getTodayDateString()
                            val pType = when (task.recurrenceType) {
                                com.example.data.model.RecurrenceType.WEEKLY -> "weekly"
                                com.example.data.model.RecurrenceType.MONTHLY -> "monthly"
                                else -> "daily"
                            }
                            val targetDateKey = when (pType) {
                                "weekly" -> DateUtils.getStartOfWeekDate(todayDate)
                                "monthly" -> todayDate.substring(0, 7)
                                else -> todayDate
                            }

                            val exists = progressRepository.checkProgressExistsForTaskInPeriod(task.id, pType, targetDateKey)
                            if (exists) {
                                onMessage("این روتین قبلاً برای این بازه ثبت شده است.")
                            } else {
                                val progress = RoutineProgressEntity(
                                    taskId = task.id,
                                    routineTitle = task.title,
                                    routineCategory = task.category ?: "روتین",
                                    completedAt = System.currentTimeMillis(),
                                    periodType = pType,
                                    targetDate = targetDateKey,
                                    routineId = task.routineSuggestionId ?: ""
                                )
                                progressRepository.insertProgress(progress)
                            }
                        } catch (e: Exception) {
                            // Suppress internal DB failure gracefully
                        }
                    }

                    if (task.recurrenceType == com.example.data.model.RecurrenceType.ONE_TIME) {
                        val updatedTask = task.copy(
                            isCompleted = true,
                            isActive = false,
                            lastCompletedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateTask(updatedTask)
                        ReminderScheduler.cancelReminder(getApplication(), task.id)
                        onMessage("یادآور با موفقیت انجام شد.")
                    } else {
                        // Calculate next occurrence
                        val (nextDate, nextTime) = DateUtils.getNextOccurrence(
                            task.dueDate,
                            task.dueTime,
                            task.recurrenceType,
                            task.selectedWeekdays,
                            task.customIntervalDays
                        )
                        val nextReminder = DateUtils.parseDateAndTimeToCalendar(nextDate, nextTime).timeInMillis
                        val updatedTask = task.copy(
                            isCompleted = false, // Keep recurring task active / pending
                            isActive = true,
                            dueDate = nextDate,
                            dueTime = nextTime,
                            nextReminderAt = nextReminder,
                            lastCompletedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.updateTask(updatedTask)
                        ReminderScheduler.scheduleReminder(getApplication(), updatedTask)
                        val friendlyDate = DateUtils.getFriendlyDateLabel(nextDate)
                        onMessage("انجام شد! یادآور بعدی برای $friendlyDate ساعت $nextTime برنامه‌ریزی شد.")
                    }
                } else {
                    val updatedTask = task.copy(
                        isCompleted = false,
                        isActive = true,
                        lastCompletedAt = null,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateTask(updatedTask)
                    ReminderScheduler.scheduleReminder(getApplication(), updatedTask)
                    onMessage("یادآور به حالت فعال بازگردانده شد.")
                }
                com.example.widget.RoutineWidgetProvider.triggerUpdate(getApplication())
            } catch (e: Exception) {
                onMessage("خطا در تغییر وضعیت یادآور: ${e.localizedMessage}")
            }
        }
    }

    fun snoozeTask(task: Task, minutes: Int, onMessage: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val nextReminder = System.currentTimeMillis() + (minutes * 60 * 1000)
                val updatedTask = task.copy(
                    nextReminderAt = nextReminder,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updatedTask)
                ReminderScheduler.scheduleReminder(getApplication(), updatedTask)
                onMessage("یادآور برای $minutes دقیقه دیگر به تعویق افتاد.")
                com.example.widget.RoutineWidgetProvider.triggerUpdate(getApplication())
            } catch (e: Exception) {
                onMessage("خطا در تعلیق یادآور: ${e.localizedMessage}")
            }
        }
    }

    // Task repository factory
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(application)
                    val repository = TaskRepository(database.taskDao())
                    return TaskViewModel(application, repository) as T
                }
            }
    }
}
