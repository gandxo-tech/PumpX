package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 30,
    val bonusMinutesUnlockedToday: Int = 0,
    val lastResetDateEpochDay: Long = 0L, // To reset daily bonus
    val isEnabled: Boolean = true
)
