package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 30,
    val bonusMinutesUnlockedToday: Int = 0,
    val initialUsageTodayMinutes: Int = 0,
    val lastResetDateEpochDay: Long = 0L, // To reset daily bonus
    val isEnabled: Boolean = true
)

fun MonitoredAppEntity.getEffectiveUsageToday(currentTodayUsageMinutes: Int): Int {
    return currentTodayUsageMinutes
}

fun MonitoredAppEntity.getEffectiveLimitToday(): Int {
    val todayEpochDay = LocalDate.now().toEpochDay()
    val isSameDay = lastResetDateEpochDay == todayEpochDay
    val bonus = if (isSameDay) bonusMinutesUnlockedToday else 0
    return dailyLimitMinutes + bonus
}

