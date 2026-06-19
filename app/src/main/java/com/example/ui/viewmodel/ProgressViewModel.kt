package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.RoutineProgressEntity
import com.example.data.model.RoutineSuggestion
import com.example.data.model.Task
import com.example.data.repository.ProgressRepository
import com.example.util.DateUtils
import com.example.util.RoutineSuggestionProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CategoryProgress(
    val category: String,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Int
)

data class ProgressUiState(
    val todayCompletedCount: Int = 0,
    val todayTotalCount: Int = 0,
    val todayPercentage: Int = 0,
    val weekCompletedCount: Int = 0,
    val weekTotalCount: Int = 0,
    val weekPercentage: Int = 0,
    val monthCompletedCount: Int = 0,
    val monthTotalCount: Int = 0,
    val monthPercentage: Int = 0,
    val currentDailyStreak: Int = 0,
    val bestDailyStreak: Int = 0,
    val categoryProgressList: List<CategoryProgress> = emptyList(),
    val dailyChartData: List<Pair<String, Int>> = emptyList(),
    val weeklyChartData: List<Pair<String, Int>> = emptyList(),
    val monthlyChartData: List<Pair<String, Int>> = emptyList()
)

class ProgressViewModel(
    application: Application,
    private val repository: ProgressRepository
) : AndroidViewModel(application) {

    // Predefined suggestions
    val suggestions: List<RoutineSuggestion> = RoutineSuggestionProvider.suggestions

    // Reactive flow of all routine progress records
    val allProgress: StateFlow<List<RoutineProgressEntity>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    
    // Reactive flow of all tasks for combined progress tracking
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Filtered daily progress flow
    val dailyProgress: StateFlow<List<RoutineProgressEntity>> = combine(allProgress, selectedDate) { progressList, date ->
        progressList.filter { it.dateString == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly completion rate statistics (last 7 days)
    val weeklyCompletionRate: StateFlow<Float> = allProgress.map { list ->
        if (list.isEmpty()) 0f
        else {
            val completedCount = list.count { it.isCompleted }
            completedCount.toFloat() / list.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Combined UI state flow for the Progress Dashboard
    val uiState: StateFlow<ProgressUiState> = combine(
        allProgress,
        allTasks
    ) { progressList, tasks ->
        val todayDate = DateUtils.getTodayDateString()
        val thisWeekDates = DateUtils.getLast7Days()
        val currentMonthPrefix = todayDate.substring(0, 7) // "YYYY-MM"

        // 1. Today's metrics
        val todayCompleted = progressList.filter { it.targetDate == todayDate }
        val todayCompletedCount = todayCompleted.size
        val todayPending = tasks.filter { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate == todayDate }
        val todayTotalCount = todayCompletedCount + todayPending.size
        val todayPercentage = if (todayTotalCount > 0) (todayCompletedCount * 100) / todayTotalCount else 0

        // 2. Week's metrics
        val weekCompleted = progressList.filter { it.targetDate in thisWeekDates }
        val weekCompletedCount = weekCompleted.size
        val weekPending = tasks.filter { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate in thisWeekDates }
        val weekTotalCount = weekCompletedCount + weekPending.size
        val weekPercentage = if (weekTotalCount > 0) (weekCompletedCount * 100) / weekTotalCount else 0

        // 3. Month's metrics
        val monthCompleted = progressList.filter { it.targetDate.startsWith(currentMonthPrefix) }
        val monthCompletedCount = monthCompleted.size
        val monthPending = tasks.filter { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate.startsWith(currentMonthPrefix) }
        val monthTotalCount = monthCompletedCount + monthPending.size
        val monthPercentage = if (monthTotalCount > 0) (monthCompletedCount * 100) / monthTotalCount else 0

        // 4. Streak calculations
        val completedDates = progressList.map { it.targetDate }.distinct().sortedDescending()
        var currentDailyStreak = 0
        if (completedDates.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            var checkDate = todayDate
            
            var hasToday = completedDates.contains(checkDate)
            if (!hasToday) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                checkDate = DateUtils.formatCalendarToDateString(calendar)
            }
            
            if (completedDates.contains(checkDate)) {
                while (completedDates.contains(checkDate)) {
                    currentDailyStreak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    checkDate = DateUtils.formatCalendarToDateString(calendar)
                }
            }
        }

        var bestDailyStreak = 0
        if (completedDates.isNotEmpty()) {
            val sortedAsc = completedDates.sorted()
            var currentBest = 0
            var prevDate: Calendar? = null
            
            for (dateStr in sortedAsc) {
                val currCal = DateUtils.parseDateAndTimeToCalendar(dateStr, "12:00")
                if (prevDate == null) {
                    currentBest = 1
                } else {
                    val diffMs = currCal.timeInMillis - prevDate.timeInMillis
                    val diffDays = (diffMs + 1000 * 60 * 60) / (1000 * 60 * 60 * 24)
                    if (diffDays <= 1L) {
                        currentBest++
                    } else {
                        if (currentBest > bestDailyStreak) {
                            bestDailyStreak = currentBest
                        }
                        currentBest = 1
                    }
                }
                prevDate = currCal
            }
            if (currentBest > bestDailyStreak) {
                bestDailyStreak = currentBest
            }
        }

        // 5. Progress by Category
        val categories = listOf(
            "سلامت و انرژی",
            "تمرکز و بهره‌وری",
            "رشد فردی",
            "نظم شخصی",
            "آرامش ذهن",
            "روابط و خانواده",
            "مدیریت مالی",
            "خانه و زندگی"
        )
        
        val categoryProgressList = categories.map { category ->
            val catCompletedCount = progressList.count { it.routineCategory == category }
            val catPendingCount = tasks.count { it.isRoutine && it.isActive && !it.isCompleted && it.category == category }
            val catTotal = catCompletedCount + catPendingCount
            val catPercentage = if (catTotal > 0) (catCompletedCount * 100) / catTotal else 0
            CategoryProgress(
                category = category,
                completedCount = catCompletedCount,
                totalCount = catTotal,
                percentage = catPercentage
            )
        }

        // 5. Daily chart data (last 7 days, sorted chronologically Saturday to Friday or past to present)
        // Let's map Gregorian date to Persian weekday name
        val dailyChartData = thisWeekDates.map { dateStr ->
            val completed = progressList.count { it.targetDate == dateStr }
            val pending = tasks.count { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate == dateStr }
            val total = completed + pending
            val percentage = if (total > 0) (completed * 100) / total else 0
            
            val cal = DateUtils.parseDateAndTimeToCalendar(dateStr, "12:00")
            val pDayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SATURDAY -> "شنبه"
                Calendar.SUNDAY -> "یکشنبه"
                Calendar.MONDAY -> "دوشنبه"
                Calendar.TUESDAY -> "سه‌شنبه"
                Calendar.WEDNESDAY -> "چهارشنبه"
                Calendar.THURSDAY -> "پنجشنبه"
                Calendar.FRIDAY -> "جمعه"
                else -> "شنبه"
            }
            Pair(pDayName, percentage)
        }

        // 6. Weekly chart data (last 4 weeks: Week 1, Week 2, Week 3, Week 4)
        // Week 4 is the current week. Let's compute dates for the last 4 weeks.
        fun getDatesPastDaysLocal(startOffset: Int, count: Int): List<String> {
            val list = mutableListOf<String>()
            val sdfStr = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            for (i in 0 until count) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -(startOffset + i))
                list.add(sdfStr.format(cal.time))
            }
            return list
        }

        val week4Dates = getDatesPastDaysLocal(0, 7)
        val week3Dates = getDatesPastDaysLocal(7, 7)
        val week2Dates = getDatesPastDaysLocal(14, 7)
        val week1Dates = getDatesPastDaysLocal(21, 7)

        val weeklyChartData = listOf(
            Pair("هفته ۱", week1Dates),
            Pair("هفته ۲", week2Dates),
            Pair("هفته ۳", week3Dates),
            Pair("هفته ۴", week4Dates)
        ).map { (label, dates) ->
            val completed = progressList.count { it.targetDate in dates }
            val pending = tasks.count { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate in dates }
            val total = completed + pending
            val percentage = if (total > 0) (completed * 100) / total else 0
            Pair(label, percentage)
        }

        // 7. Monthly chart data (last 6 months: Month 1, ..., Month 6)
        val prefixes = mutableListOf<String>()
        val sdfMonth = SimpleDateFormat("yyyy-MM", Locale.US)
        for (i in 5 downTo 0) {
            val calPrefix = Calendar.getInstance()
            calPrefix.add(Calendar.MONTH, -i)
            prefixes.add(sdfMonth.format(calPrefix.time))
        }

        val monthlyChartData = prefixes.mapIndexed { index, prefix ->
            val completed = progressList.count { it.targetDate.startsWith(prefix) }
            val pending = tasks.count { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate.startsWith(prefix) }
            val total = completed + pending
            val percentage = if (total > 0) (completed * 100) / total else 0
            Pair("ماه ${index + 1}", percentage)
        }

        ProgressUiState(
            todayCompletedCount = todayCompletedCount,
            todayTotalCount = todayTotalCount,
            todayPercentage = todayPercentage,
            weekCompletedCount = weekCompletedCount,
            weekTotalCount = weekTotalCount,
            weekPercentage = weekPercentage,
            monthCompletedCount = monthCompletedCount,
            monthTotalCount = monthTotalCount,
            monthPercentage = monthPercentage,
            currentDailyStreak = currentDailyStreak,
            bestDailyStreak = bestDailyStreak,
            categoryProgressList = categoryProgressList,
            dailyChartData = dailyChartData,
            weeklyChartData = weeklyChartData,
            monthlyChartData = monthlyChartData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    fun changeSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Toggle logic for routine completion
    fun toggleRoutineCompletion(
        suggestion: RoutineSuggestion,
        dateString: String,
        currentProgress: RoutineProgressEntity? = null
    ) {
        viewModelScope.launch {
            if (currentProgress != null) {
                repository.deleteProgress(currentProgress)
            } else {
                val newEntity = RoutineProgressEntity(
                    taskId = -1,
                    routineTitle = suggestion.title,
                    routineCategory = suggestion.category,
                    completedAt = System.currentTimeMillis(),
                    periodType = suggestion.frequency,
                    targetDate = dateString,
                    routineId = suggestion.id
                )
                repository.insertProgress(newEntity)
            }
        }
    }

    // Advanced progress logging (cups of water / items increment)
    fun incrementRoutineValue(
        suggestion: RoutineSuggestion,
        dateString: String,
        currentProgress: RoutineProgressEntity? = null,
        targetValue: Int = 8
    ) {
        viewModelScope.launch {
            if (currentProgress == null) {
                val newEntity = RoutineProgressEntity(
                    taskId = -1,
                    routineTitle = suggestion.title,
                    routineCategory = suggestion.category,
                    completedAt = System.currentTimeMillis(),
                    periodType = suggestion.frequency,
                    targetDate = dateString,
                    routineId = suggestion.id
                )
                repository.insertProgress(newEntity)
            }
        }
    }

    // Reset progress record for specific suggestions
    fun resetRoutineProgress(progress: RoutineProgressEntity) {
        viewModelScope.launch {
            repository.deleteProgress(progress)
        }
    }

    fun clearAllProgress() {
        viewModelScope.launch {
            repository.deleteAllProgress()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(application)
                    val repository = ProgressRepository(database.routineProgressDao())
                    return ProgressViewModel(application, repository) as T
                }
            }
    }
}
