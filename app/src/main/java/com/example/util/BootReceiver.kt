package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "دستگاه بارگذاری مجدد شد. برنامه‌ریزی مجدد یادآورهای فعال...")
            val db = AppDatabase.getDatabase(context)
            val taskDao = db.taskDao()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val allTasks = taskDao.getAllTasks().first()
                    // Filter tasks that are still active and not completed
                    val activeTasks = allTasks.filter { it.isActive && !it.isCompleted }
                    for (task in activeTasks) {
                        val nextReminder = task.nextReminderAt
                        if (nextReminder != null && nextReminder > System.currentTimeMillis()) {
                            ReminderScheduler.scheduleReminder(context, task)
                            Log.d(TAG, "یادآور کار با شناسه ${task.id} مجدداً برنامه‌ریزی شد.")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "خطا در بارگذاری مجدد یادآورها پس از بوت دستگاه", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
