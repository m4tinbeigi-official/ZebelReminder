package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.Task

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    /**
     * Schedules a reminder / notification for the given task.
     */
    fun scheduleReminder(context: Context, task: Task) {
        val nextReminder = task.nextReminderAt ?: return
        
        // Only schedule if the alarm time is in the future
        if (nextReminder <= System.currentTimeMillis()) {
            Log.d(TAG, "زمان یادآوری برای کار '${task.title}' گذشته است و نادیده گرفته شد.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_ALARM
            putExtra(NotificationReceiver.EXTRA_TASK_ID, task.id)
            putExtra(NotificationReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(NotificationReceiver.EXTRA_TASK_DESC, task.description.ifBlank { "زمان انجام این کار رسیده است." })
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check exact alarm capability on Android 12+ (SDK 31+)
        val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextReminder,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextReminder,
                    pendingIntent
                )
            }
            Log.d(TAG, "یادآوری برای '${task.title}' تنظیم شد.")
        } catch (e: SecurityException) {
            Log.w(TAG, "مجوز برای exact alarm در دسترس نبود، استفاده از هشدار معمولی", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextReminder,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "خطا در تنظیم یادآور برای '${task.title}'", e)
        }
    }

    /**
     * Cancels an existing scheduling for the given task identifier.
     */
    fun cancelReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "یادآور با شناسه $taskId لغو شد.")
    }
}
