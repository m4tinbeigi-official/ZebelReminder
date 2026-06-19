package com.example.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import ir.m4tinbeigi.taskreminder.R
import com.example.data.local.AppDatabase
import com.example.data.model.Task
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class RoutineWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RoutineWidgetViewsFactory(this.applicationContext)
    }
}

class RoutineWidgetViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var taskList = listOf<Task>()
    private var todayCompletedTaskIds = setOf<Int>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        Log.d("RoutineWidgetService", "onDataSetChanged triggered")
        val db = AppDatabase.getDatabase(context)
        val todayDate = DateUtils.getTodayDateString()

        runBlocking {
            try {
                val allTasksList = db.taskDao().getAllTasks().first()
                val todayProgress = db.routineProgressDao().getTodayProgress(todayDate).first()

                todayCompletedTaskIds = todayProgress.map { it.taskId }.toSet()
                
                // Select active routines which are either due today or completed today
                val routines = allTasksList.filter {
                    it.isRoutine && it.isActive && (it.dueDate == todayDate || todayCompletedTaskIds.contains(it.id))
                }

                // Sorting: pending first, completed last. Then sorted by dueTime.
                taskList = routines.sortedWith(compareBy<Task> {
                    todayCompletedTaskIds.contains(it.id)
                }.thenBy { it.dueTime })

                Log.d("RoutineWidgetService", "Factory dataset changed. Count: ${taskList.size}")
            } catch (e: Exception) {
                Log.e("RoutineWidgetService", "Error querying tasks for widget list", e)
            }
        }
    }

    override fun onDestroy() {
        taskList = emptyList()
    }

    override fun getCount(): Int = taskList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= taskList.size) return null

        val task = taskList[position]
        val isCompletedToday = todayCompletedTaskIds.contains(task.id)

        val views = RemoteViews(context.packageName, R.layout.widget_routine_item)
        views.setTextViewText(R.id.routine_title, task.title)

        // Resolve language settings to show appropriate texts
        val sharedPrefs = context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
        val appLanguagePref = sharedPrefs.getString("app_language", "system") ?: "system"
        val sysLang = java.util.Locale.getDefault().language
        val lang = if (appLanguagePref == "system") {
            if (sysLang == "fa") "fa" else "en"
        } else {
            appLanguagePref
        }

        val displayTime = if (lang == "fa") {
            com.example.util.JalaliCalendarHelper.englishToPersianDigits(task.dueTime)
        } else {
            task.dueTime
        }

        val categoryLabel = task.category ?: (if (lang == "fa") "روتین" else "Routine")
        val infoText = if (lang == "fa") {
            if (isCompletedToday) "ساعت $displayTime • انجام شد" else "ساعت $displayTime • $categoryLabel"
        } else {
            if (isCompletedToday) "At $displayTime • Completed" else "At $displayTime • $categoryLabel"
        }

        views.setTextViewText(R.id.routine_info, infoText)

        if (isCompletedToday) {
            views.setImageViewResource(R.id.btn_complete, R.drawable.ic_check_circle_filled)
            views.setTextColor(R.id.routine_title, android.graphics.Color.parseColor("#7F8C8D"))
            views.setTextColor(R.id.routine_info, android.graphics.Color.parseColor("#95A5A6"))
            
            // Set empty intent on completed item clicks to prevent multiple invocations
            views.setOnClickFillInIntent(R.id.btn_complete, Intent())
        } else {
            views.setImageViewResource(R.id.btn_complete, R.drawable.ic_circle_outlined)
            views.setTextColor(R.id.routine_title, android.graphics.Color.WHITE)
            views.setTextColor(R.id.routine_info, android.graphics.Color.parseColor("#BDC3C7"))

            val fillInIntent = Intent().apply {
                putExtra(RoutineWidgetProvider.EXTRA_TASK_ID, task.id)
            }
            views.setOnClickFillInIntent(R.id.btn_complete, fillInIntent)
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position >= 0 && position < taskList.size) taskList[position].id.toLong() else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
