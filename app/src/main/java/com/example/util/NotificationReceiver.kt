package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.model.RecurrenceType
import com.example.data.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        
        Log.d(TAG, "onReceive action=$action, taskId=$taskId")

        if (taskId == -1) return

        when (action) {
            ACTION_COMPLETE_TASK -> {
                handleCompleteTask(context, taskId)
            }
            ACTION_SNOOZE_TASK -> {
                val minutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 5)
                handleSnoozeTask(context, taskId, minutes)
            }
            ACTION_ALARM, null -> {
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "یادآوری کار"
                val taskDesc = intent.getStringExtra(EXTRA_TASK_DESC) ?: "زمان انجام این کار رسیده است."
                showNotification(context, taskId, taskTitle, taskDesc)
            }
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, desc: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel
        createNotificationChannel(context, notificationManager)

        // Intent to open Main Activity on tap
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "انجام شد" Done Action
        val completeIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 100000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "سِنوز ۵ دقیقه" Snooze Action
        val snoozeFiveIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_SNOOZE_MINUTES, 5)
        }
        val snoozeFivePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 200000,
            snoozeFiveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "سِنوز ۱۰ دقیقه" Snooze Action
        val snoozeTenIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_SNOOZE_MINUTES, 10)
        }
        val snoozeTenPendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 300000,
            snoozeTenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sharedPrefs = context.getSharedPreferences("zebel_settings", Context.MODE_PRIVATE)
        val playSound = sharedPrefs.getBoolean("play_reminder_sound", true)
        val channelIdToUse = if (playSound) CHANNEL_ID else CHANNEL_ID_SILENT

        // Build notification
        val builder = NotificationCompat.Builder(context, channelIdToUse)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system icon for guarantee of existence
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "انجام شد", completePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "سِنوز ۵ دقیقه", snoozeFivePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "سِنوز ۱۰ دقیقه", snoozeTenPendingIntent)

        if (playSound) {
            val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
            builder.setVibrate(longArrayOf(0, 250, 250, 250))
            
            // Explicitly play standard alarm/notification sound through RingtoneManager to guarantee system volume playback
            try {
                val ringtone = android.media.RingtoneManager.getRingtone(context, soundUri)
                ringtone?.play()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom notification sound via RingtoneManager", e)
            }
        } else {
            builder.setSound(null)
            builder.setVibrate(null)
        }

        notificationManager.notify(taskId, builder.build())
    }

    private fun handleCompleteTask(context: Context, taskId: Int) {
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskDao.getTaskById(taskId)
                if (task != null) {
                    if (task.recurrenceType == RecurrenceType.ONE_TIME) {
                        val updated = task.copy(
                            isCompleted = true,
                            isActive = false,
                            lastCompletedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        taskDao.updateTask(updated)
                        ReminderScheduler.cancelReminder(context, taskId)
                        showToast(context, "یادآور علامت زده شد: انجام شد")
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
                        val updated = task.copy(
                            isCompleted = false,
                            isActive = true,
                            dueDate = nextDate,
                            dueTime = nextTime,
                            nextReminderAt = nextReminder,
                            lastCompletedAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        taskDao.updateTask(updated)
                        ReminderScheduler.scheduleReminder(context, updated)

                        val friendlyDate = DateUtils.getFriendlyDateLabel(nextDate)
                        showToast(context, "انجام شد! یادآور بعدی برای $friendlyDate ساعت $nextTime")
                    }
                }
                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId)
                com.example.widget.RoutineWidgetProvider.triggerUpdate(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error completing task in receiver", e)
            }
        }
    }

    private fun handleSnoozeTask(context: Context, taskId: Int, minutes: Int) {
        val db = AppDatabase.getDatabase(context)
        val taskDao = db.taskDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = taskDao.getTaskById(taskId)
                if (task != null) {
                    val nextReminder = System.currentTimeMillis() + (minutes * 60 * 1000)
                    val updated = task.copy(
                        nextReminderAt = nextReminder,
                        updatedAt = System.currentTimeMillis()
                    )
                    taskDao.updateTask(updated)
                    ReminderScheduler.scheduleReminder(context, updated)
                    showToast(context, "یادآور برای $minutes دقیقه دیگر به تعویق افتاد.")
                }
                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId)
                com.example.widget.RoutineWidgetProvider.triggerUpdate(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error snoozing task in receiver", e)
            }
        }
    }

    private fun createNotificationChannel(context: Context, manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Channel with sound enabled
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            manager.createNotificationChannel(channel)

            // 2. Silent Channel
            val channelSilent = NotificationChannel(
                CHANNEL_ID_SILENT,
                "یادآوری‌های بی‌صدا",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "اعلان‌های بدون صدا و لرزش برای یادآورها"
                enableVibration(false)
                enableLights(true)
                setSound(null, null)
            }
            manager.createNotificationChannel(channelSilent)
        }
    }

    private suspend fun showToast(context: Context, message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "NotificationReceiver"
        const val CHANNEL_ID = "task_reminders_channel"
        const val CHANNEL_ID_SILENT = "task_reminders_channel_silent"
        const val CHANNEL_NAME = "یادآوری‌ها"
        const val CHANNEL_DESC = "اعلان‌های مربوط به کارها و یادآورها"

        const val ACTION_ALARM = "com.example.action.ALARM"
        const val ACTION_COMPLETE_TASK = "com.example.action.COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.example.action.SNOOZE_TASK"

        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_TASK_DESC = "task_desc"
        const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"
    }
}
