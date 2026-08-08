package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PushupSessionDao {

    @Query("SELECT * FROM pushup_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PushupSessionEntity>>

    @Query("SELECT * FROM pushup_sessions WHERE timestamp >= :startTimeMs AND timestamp <= :endTimeMs ORDER BY timestamp ASC")
    fun getSessionsInRange(startTimeMs: Long, endTimeMs: Long): Flow<List<PushupSessionEntity>>

    @Query("SELECT SUM(pushupsCount) FROM pushup_sessions WHERE timestamp >= :startTimeMs")
    fun getTotalPushupsSince(startTimeMs: Long): Flow<Int?>

    @Query("SELECT SUM(unlockedBonusMinutes) FROM pushup_sessions WHERE timestamp >= :startTimeMs")
    fun getTotalBonusMinutesSince(startTimeMs: Long): Flow<Int?>

    @Query("SELECT SUM(pushupsCount) FROM pushup_sessions")
    fun getTotalPushupsAllTime(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PushupSessionEntity): Long
}
