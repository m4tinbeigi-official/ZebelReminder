package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.RecurrenceType

class Converters {
    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType {
        return try {
            RecurrenceType.valueOf(value)
        } catch (e: Exception) {
            RecurrenceType.ONE_TIME
        }
    }
}
