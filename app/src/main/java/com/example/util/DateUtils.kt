package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    /**
     * Returns today's date in YYYY-MM-DD format
     */
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Returns current time in HH:mm format
     */
    fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Returns a user friendly display date. If it's today, return "امروز".
     * If tomorrow, return "فردا". Otherwise format it.
     */
    fun getFriendlyDateLabel(dateStr: String): String {
        val today = getTodayDateString()
        if (dateStr == today) return "امروز"

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        if (dateStr == tomorrow) return "فردا"

        // Format dates beautifully (e.g. YYYY/MM/DD)
        return dateStr.replace("-", "/")
    }

    /**
     * Compares date string in format YYYY-MM-DD
     * returns:
     * -1 if date1 < date2 (past)
     * 1 if date1 > date2 (future)
     * 0 if equal
     */
    fun compareDates(date1: String, date2: String): Int {
        return date1.compareTo(date2)
    }

    /**
     * Checks if a task is overdue (due date is before today, and not completed)
     */
    fun isOverdue(dueDate: String, dueTime: String, isCompleted: Boolean): Boolean {
        if (isCompleted) return false
        val today = getTodayDateString()
        val dateCompare = compareDates(dueDate, today)
        if (dateCompare < 0) return true
        if (dateCompare == 0) {
            val currentTime = getCurrentTimeString()
            return dueTime < currentTime
        }
        return false
    }

    fun parseDateAndTimeToCalendar(dateStr: String, timeStr: String): Calendar {
        val cal = Calendar.getInstance()
        try {
            val dateParts = dateStr.split("-").map { it.toInt() }
            val timeParts = timeStr.split(":").map { it.toInt() }
            if (dateParts.size >= 3 && timeParts.size >= 2) {
                cal.set(Calendar.YEAR, dateParts[0])
                cal.set(Calendar.MONTH, dateParts[1] - 1)
                cal.set(Calendar.DAY_OF_MONTH, dateParts[2])
                cal.set(Calendar.HOUR_OF_DAY, timeParts[0])
                cal.set(Calendar.MINUTE, timeParts[1])
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
        } catch (e: Exception) {
            // Log or ignore
        }
        return cal
    }

    fun formatCalendarToDateString(cal: Calendar): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun formatCalendarToTimeString(cal: Calendar): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(cal.time)
    }

    fun mapPersianIndexToCalendarDay(index: Int): Int {
        return when (index) {
            0 -> Calendar.SATURDAY
            1 -> Calendar.SUNDAY
            2 -> Calendar.MONDAY
            3 -> Calendar.TUESDAY
            4 -> Calendar.WEDNESDAY
            5 -> Calendar.THURSDAY
            6 -> Calendar.FRIDAY
            else -> Calendar.SUNDAY
        }
    }

    /**
     * Calculates the next occurrence date and time based on recurrence type and rules.
     */
    fun getNextOccurrence(
        currentDateStr: String,
        currentTimeStr: String,
        recurrenceType: com.example.data.model.RecurrenceType,
        selectedWeekdays: String? = null,
        customIntervalDays: Int? = null
    ): Pair<String, String> {
        val cal = parseDateAndTimeToCalendar(currentDateStr, currentTimeStr)
        when (recurrenceType) {
            com.example.data.model.RecurrenceType.ONE_TIME -> {
                // One-time tasks do not recur
            }
            com.example.data.model.RecurrenceType.DAILY -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            com.example.data.model.RecurrenceType.WEEKLY -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
            }
            com.example.data.model.RecurrenceType.MONTHLY -> {
                cal.add(Calendar.MONTH, 1)
            }
            com.example.data.model.RecurrenceType.YEARLY -> {
                cal.add(Calendar.YEAR, 1)
            }
            com.example.data.model.RecurrenceType.EVERY_X_DAYS -> {
                val interval = customIntervalDays ?: 1
                cal.add(Calendar.DAY_OF_YEAR, interval)
            }
            com.example.data.model.RecurrenceType.CUSTOM_WEEKDAYS -> {
                val selectedIndices = selectedWeekdays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                val calendarDays = selectedIndices.map { mapPersianIndexToCalendarDay(it) }.toSet()
                if (calendarDays.isNotEmpty()) {
                    var found = false
                    for (i in 1..7) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        if (calendarDays.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                            found = true
                            break
                        }
                    }
                    if (!found) {
                        cal.add(Calendar.DAY_OF_YEAR, 7)
                    }
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }
        return Pair(formatCalendarToDateString(cal), formatCalendarToTimeString(cal))
    }

    /**
     * Returns a list of the last 7 days including today in YYYY-MM-DD format
     */
    fun getLast7Days(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0 until 7) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    /**
     * Return start of week date (Saturday) for any given YYYY-MM-DD date
     */
    fun getStartOfWeekDate(dateStr: String): String {
        val cal = parseDateAndTimeToCalendar(dateStr, "12:00")
        var count = 0
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && count < 8) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            count++
        }
        return formatCalendarToDateString(cal)
    }
}
