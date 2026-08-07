package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pushup_sessions")
data class PushupSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pushupsCount: Int,
    val targetPushups: Int,
    val unlockedBonusMinutes: Int,
    val durationSeconds: Int,
    val packageNameTarget: String? = null
)
