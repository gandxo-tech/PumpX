package com.example.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.PumpXApplication
import com.example.data.local.getEffectiveUsageToday
import com.example.data.local.getEffectiveLimitToday
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

class BlockerService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var lastBlockedPackage = ""
    private var lastBlockedTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForegroundService()
            startMonitoring()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "blocker_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PumpX Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PumpX est actif")
            .setContentText("Surveillance du temps d'écran en cours...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)
    }

    private fun startMonitoring() {
        scope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return@launch
            val app = application as PumpXApplication
            
            while (isRunning) {
                try {
                    val monitoredApps = app.monitoredAppRepository.allMonitoredApps.firstOrNull() ?: emptyList()
                    if (monitoredApps.isEmpty()) {
                        delay(2000)
                        continue
                    }

                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000 * 10 // last 10 seconds
                    val events = usageStatsManager.queryEvents(startTime, endTime)
                    
                    var currentForegroundApp: String? = null
                    while (events.hasNextEvent()) {
                        val event = UsageEvents.Event()
                        events.getNextEvent(event)
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            currentForegroundApp = event.packageName
                        }
                    }

                    if (currentForegroundApp != null && currentForegroundApp != packageName) {
                        val monitoredApp = monitoredApps.find { it.packageName == currentForegroundApp }
                        if (monitoredApp != null && monitoredApp.isEnabled) {
                            val rawTodayUsage = app.screenTimeRepository.getTodayUsageMinutes(currentForegroundApp)
                            if (monitoredApp.lastResetDateEpochDay == java.time.LocalDate.now().toEpochDay() && monitoredApp.initialUsageTodayMinutes == 0 && rawTodayUsage > 0) {
                                app.monitoredAppRepository.checkAndAutoRepairBaseline(monitoredApp.packageName, rawTodayUsage)
                            }
                            val effectiveUsage = monitoredApp.getEffectiveUsageToday(rawTodayUsage)
                            val effectiveLimit = monitoredApp.getEffectiveLimitToday()
                            if (effectiveUsage >= effectiveLimit) {
                                // Exceeded! Launch blocking screen
                                if (currentForegroundApp != lastBlockedPackage || System.currentTimeMillis() - lastBlockedTime > 60000) {
                                    lastBlockedPackage = currentForegroundApp
                                    lastBlockedTime = System.currentTimeMillis()
                                    launchBlockingScreen(currentForegroundApp, effectiveLimit)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000)
            }
        }
    }

    private fun launchBlockingScreen(targetPackage: String, limit: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("blocked_package", targetPackage)
            putExtra("blocked_limit", limit)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, "blocker_service_channel")
            .setContentTitle("Temps écoulé !")
            .setContentText("Vous avez dépassé votre limite pour cette application. Faites des pompes pour continuer !")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()
            
        notificationManager?.notify(targetPackage.hashCode(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
    }
}
