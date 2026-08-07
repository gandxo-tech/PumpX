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

    suspend fun addOrUpdateApp(packageName: String, appName: String, dailyLimitMinutes: Int) {
        val existing = monitoredAppDao.getAppByPackageName(packageName)
        val todayEpochDay = LocalDate.now().toEpochDay()
        val bonus = if (existing != null && existing.lastResetDateEpochDay == todayEpochDay) {
            existing.bonusMinutesUnlockedToday
        } else 0

        val entity = MonitoredAppEntity(
            packageName = packageName,
            appName = appName,
            dailyLimitMinutes = dailyLimitMinutes,
            bonusMinutesUnlockedToday = bonus,
            lastResetDateEpochDay = todayEpochDay,
            isEnabled = true
        )
        monitoredAppDao.insertOrUpdateApp(entity)
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
