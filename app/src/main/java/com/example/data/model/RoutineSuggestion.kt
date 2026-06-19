package com.example.data.model

data class RoutineSuggestion(
    val id: String,
    val title: String,
    val shortDescription: String,
    val category: String,           // سلامت و انرژی, تمرکز و بهره‌وری, etc.
    val frequency: String,          // daily, weekly, monthly
    val suggestedTime: String?,     // e.g. "08:00"
    val motivationalText: String,   // Persian motivation
    val defaultSnoozeMinutes: Int = 10,
    val canBeConvertedToTask: Boolean = true
) {
    val defaultTitle: String get() = title
    val defaultDesc: String get() = shortDescription
    
    val iconName: String get() = when {
        id.contains("water") -> "water"
        id.contains("walk") || id.contains("run") || id.contains("sport") || id.contains("exercise") || id.contains("stretch") -> "walk"
        id.contains("book") || id.contains("read") || id.contains("study") || id.contains("learn") -> "book"
        id.contains("sleep") || id.contains("bedtime") -> "sleep"
        else -> "mind"
    }
    
    val recommendedTime: String get() = suggestedTime ?: "08:00"
    
    val targetType: String get() = if (id.contains("water")) "count" else "binary"
}
