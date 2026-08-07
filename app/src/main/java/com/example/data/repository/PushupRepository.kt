package com.example.data.repository

import com.example.data.local.PushupSessionDao
import com.example.data.local.PushupSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PushupRepository(private val pushupSessionDao: PushupSessionDao) {

    val allSessions: Flow<List<PushupSessionEntity>> = pushupSessionDao.getAllSessions()

    fun getTodayStartMs(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun getTodayPushups(): Flow<Int?> {
        return pushupSessionDao.getTotalPushupsSince(getTodayStartMs())
    }

    fun getTodayBonusMinutes(): Flow<Int?> {
        return pushupSessionDao.getTotalBonusMinutesSince(getTodayStartMs())
    }

    fun getSessionsForRange(startTimeMs: Long, endTimeMs: Long): Flow<List<PushupSessionEntity>> {
        return pushupSessionDao.getSessionsInRange(startTimeMs, endTimeMs)
    }

    suspend fun recordSession(
        pushupsCount: Int,
        targetPushups: Int,
        unlockedBonusMinutes: Int,
        durationSeconds: Int,
        packageNameTarget: String? = null
    ): Long {
        val session = PushupSessionEntity(
            timestamp = System.currentTimeMillis(),
            pushupsCount = pushupsCount,
            targetPushups = targetPushups,
            unlockedBonusMinutes = unlockedBonusMinutes,
            durationSeconds = durationSeconds,
            packageNameTarget = packageNameTarget
        )
        return pushupSessionDao.insertSession(session)
    }
}
