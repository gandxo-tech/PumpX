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
    val todayEpochDay = LocalDate.now().toEpochDay()
    val isSameDay = lastResetDateEpochDay == todayEpochDay
    val baseline = if (isSameDay) {
        if (initialUsageTodayMinutes == 0 && currentTodayUsageMinutes > 0) {
            currentTodayUsageMinutes
        } else {
            initialUsageTodayMinutes
        }
    } else 0

    val adjustedBaseline = if (currentTodayUsageMinutes >= baseline) baseline else 0
    return maxOf(0, currentTodayUsageMinutes - adjustedBaseline)
}

fun MonitoredAppEntity.getEffectiveLimitToday(): Int {
    val todayEpochDay = LocalDate.now().toEpochDay()
    val isSameDay = lastResetDateEpochDay == todayEpochDay
    val bonus = if (isSameDay) bonusMinutesUnlockedToday else 0
    return dailyLimitMinutes + bonus
}

