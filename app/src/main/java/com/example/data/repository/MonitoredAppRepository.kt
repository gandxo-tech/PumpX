package com.example.data.repository

import com.example.data.local.MonitoredAppDao
import com.example.data.local.MonitoredAppEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MonitoredAppRepository(private val monitoredAppDao: MonitoredAppDao) {

    val allMonitoredApps: Flow<List<MonitoredAppEntity>> = monitoredAppDao.getAllMonitoredApps()

    suspend fun getApp(packageName: String): MonitoredAppEntity? {
        return monitoredAppDao.getAppByPackageName(packageName)
    }

    suspend fun addOrUpdateApp(packageName: String, appName: String, dailyLimitMinutes: Int, currentTodayUsageMinutes: Int = 0) {
        val existing = monitoredAppDao.getAppByPackageName(packageName)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val isSameDay = existing != null && existing.lastResetDateEpochDay == todayEpochDay

        val bonus = if (isSameDay) existing!!.bonusMinutesUnlockedToday else 0
        val initialUsage = if (isSameDay && existing!!.initialUsageTodayMinutes > 0) {
            existing.initialUsageTodayMinutes
        } else {
            currentTodayUsageMinutes
        }

        val entity = MonitoredAppEntity(
            packageName = packageName,
            appName = appName,
            dailyLimitMinutes = dailyLimitMinutes,
            bonusMinutesUnlockedToday = bonus,
            initialUsageTodayMinutes = initialUsage,
            lastResetDateEpochDay = todayEpochDay,
            isEnabled = true
        )
        monitoredAppDao.insertOrUpdateApp(entity)
    }

    suspend fun checkAndAutoRepairBaseline(packageName: String, currentUsageMinutes: Int) {
        val existing = monitoredAppDao.getAppByPackageName(packageName) ?: return
        val todayEpochDay = LocalDate.now().toEpochDay()
        if (existing.lastResetDateEpochDay == todayEpochDay && existing.initialUsageTodayMinutes == 0 && currentUsageMinutes > 0) {
            val repaired = existing.copy(initialUsageTodayMinutes = currentUsageMinutes)
            monitoredAppDao.updateApp(repaired)
        }
    }

    suspend fun addBonusMinutes(packageName: String, minutes: Int) {
        val app = monitoredAppDao.getAppByPackageName(packageName) ?: return
        val todayEpochDay = LocalDate.now().toEpochDay()
        val currentBonus = if (app.lastResetDateEpochDay == todayEpochDay) app.bonusMinutesUnlockedToday else 0
        
        val updated = app.copy(
            bonusMinutesUnlockedToday = currentBonus + minutes,
            lastResetDateEpochDay = todayEpochDay
        )
        monitoredAppDao.updateApp(updated)
    }

    suspend fun removeApp(packageName: String) {
        monitoredAppDao.deleteByPackageName(packageName)
    }
}
