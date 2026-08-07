package com.example

import android.app.Application
import com.example.core.datastore.UserPreferencesRepository
import com.example.data.local.PumpXDatabase
import com.example.data.repository.MonitoredAppRepository
import com.example.data.repository.PushupRepository
import com.example.data.repository.ScreenTimeRepository

class PumpXApplication : Application() {

    val database by lazy { PumpXDatabase.getDatabase(this) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val monitoredAppRepository by lazy { MonitoredAppRepository(database.monitoredAppDao()) }
    val pushupRepository by lazy { PushupRepository(database.pushupSessionDao()) }
    val screenTimeRepository by lazy { ScreenTimeRepository(this) }
    val analyticsManager by lazy { com.example.core.analytics.AnalyticsManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PumpXApplication
            private set
    }
}
