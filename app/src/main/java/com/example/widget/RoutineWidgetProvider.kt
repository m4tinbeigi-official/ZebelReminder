package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import ir.m4tinbeigi.taskreminder.R
import com.example.data.local.AppDatabase
import com.example.data.model.RecurrenceType
import com.example.data.model.RoutineProgressEntity
import com.example.data.repository.ProgressRepository
import com.example.util.DateUtils
import com.example.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoutineWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate triggered for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        Log.d(TAG, "onReceive: action=$action")

        if (action == ACTION_COMPLETE_WIDGET_TASK) {
            val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            Log.d(TAG, "Marking task complete via widget: taskId=$taskId, widgetId=$appWidgetId")

            if (taskId != -1) {
                val db = AppDatabase.getDatabase(context)
                val taskDao = db.taskDao()
                val progressDao = db.routineProgressDao()
                val progressRepository = ProgressRepository(progressDao)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val task = taskDao.getTaskById(taskId)
                        if (task != null) {
                            val todayDate = DateUtils.getTodayDateString()
                            val pType = when (task.recurrenceType) {
                                RecurrenceType.WEEKLY -> "weekly"
                                RecurrenceType.MONTHLY -> "monthly"
                                else -> "daily"
                            }
                            val targetDateKey = when (pType) {
                                "weekly" -> DateUtils.getStartOfWeekDate(todayDate)
                                "monthly" -> todayDate.substring(0, 7)
                                else -> todayDate
                            }

                            val exists = progressRepository.checkProgressExistsForTaskInPeriod(task.id, pType, targetDateKey)
                            if (!exists) {
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

                            // Advance next recurrence
                            if (task.recurrenceType == RecurrenceType.ONE_TIME) {
                                val updatedTask = task.copy(
                                    isCompleted = true,
                                    isActive = false,
                                    lastCompletedAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                taskDao.updateTask(updatedTask)
                                ReminderScheduler.cancelReminder(context, task.id)
                            } else {
                                val (nextDate, nextTime) = DateUtils.getNextOccurrence(
                                    task.dueDate,
                                    task.dueTime,
                                    task.recurrenceType,
                                    task.selectedWeekdays,
                                    task.customIntervalDays
                                )
                                val nextReminder = DateUtils.parseDateAndTimeToCalendar(nextDate, nextTime).timeInMillis
                                val updatedTask = task.copy(
                                    isCompleted = false,
                                    isActive = true,
                                    dueDate = nextDate,
                                    dueTime = nextTime,
                                    nextReminderAt = nextReminder,
                                    lastCompletedAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                taskDao.updateTask(updatedTask)
                                ReminderScheduler.scheduleReminder(context, updatedTask)
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "روتین با موفقیت انجام شد.", Toast.LENGTH_SHORT).show()
                            }

                            // Trigger widget reload
                            triggerUpdate(context)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error toggling task on widget receive", e)
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "RoutineWidget"
        const val ACTION_COMPLETE_WIDGET_TASK = "com.example.action.COMPLETE_WIDGET_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"

        fun triggerUpdate(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, RoutineWidgetProvider::class.java)
            )
            val intent = Intent(context, RoutineWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val db = AppDatabase.getDatabase(context)
            val taskDao = db.taskDao()
            val progressDao = db.routineProgressDao()
            val todayDate = DateUtils.getTodayDateString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allTasks = taskDao.getAllTasks().first()
                    val progressList = progressDao.getTodayProgress(todayDate).first()

                    val todayCompletedCount = progressList.size
                    val todayPending = allTasks.filter { it.isRoutine && it.isActive && !it.isCompleted && it.dueDate == todayDate }
                    val todayTotalCount = todayCompletedCount + todayPending.size
                    val todayPercentage = if (todayTotalCount > 0) (todayCompletedCount * 100) / todayTotalCount else 0

                    val sharedPrefs = context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
                    val appLanguagePref = sharedPrefs.getString("app_language", "system") ?: "system"
                    val sysLang = java.util.Locale.getDefault().language
                    val lang = if (appLanguagePref == "system") {
                        if (sysLang == "fa") "fa" else "en"
                    } else {
                        appLanguagePref
                    }

                    val titleText = if (lang == "fa") "روتین‌های امروز" else "Today's Routines"
                    val emptyText = if (lang == "fa") "روتین فعالی برای امروز باقی نمانده!" else "All routines completed today!"
                    
                    val progressText = if (lang == "fa") {
                        val persianPct = com.example.util.JalaliCalendarHelper.englishToPersianDigits(todayPercentage.toString())
                        val persianDone = com.example.util.JalaliCalendarHelper.englishToPersianDigits(todayCompletedCount.toString())
                        val persianTotal = com.example.util.JalaliCalendarHelper.englishToPersianDigits(todayTotalCount.toString())
                        "$persianPct٪ ($persianDone از $persianTotal)"
                    } else {
                        "$todayPercentage% ($todayCompletedCount of $todayTotalCount)"
                    }

                    val views = RemoteViews(context.packageName, R.layout.routine_widget)
                    views.setTextViewText(R.id.widget_title, titleText)
                    views.setTextViewText(R.id.widget_progress_text, progressText)
                    views.setProgressBar(R.id.widget_progress_bar, 100, todayPercentage, false)

                    if (todayTotalCount > 0) {
                        views.setViewVisibility(R.id.widget_list, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                    } else {
                        views.setViewVisibility(R.id.widget_list, View.GONE)
                        views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                        views.setTextViewText(R.id.widget_empty_view, emptyText)
                    }

                    // RemoteViews Service connector
                    val adapterIntent = Intent(context, RoutineWidgetService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                    }
                    views.setRemoteAdapter(R.id.widget_list, adapterIntent)

                    // Template for item clicks inside ListView
                    val completeIntent = Intent(context, RoutineWidgetProvider::class.java).apply {
                        action = ACTION_COMPLETE_WIDGET_TASK
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    val completePendingIntent = PendingIntent.getBroadcast(
                        context,
                        appWidgetId + 90000,
                        completeIntent,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    views.setPendingIntentTemplate(R.id.widget_list, completePendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list)
                } catch (e: Exception) {
                    Log.e(TAG, "Error refreshing widget", e)
                }
            }
        }
    }
}
