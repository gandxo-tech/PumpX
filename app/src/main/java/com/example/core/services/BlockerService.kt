package com.example.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.PumpXApplication
import com.example.core.datastore.AppTheme
import com.example.data.local.getEffectiveUsageToday
import com.example.data.local.getEffectiveLimitToday
import com.example.ui.theme.PumpXTheme
import com.example.presentation.blocker.LimitReachedScreen
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull

class OverlayServiceLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun onCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class BlockerService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false
    private var lastBlockedPackage = ""
    private var lastBlockedTime = 0L

    private var overlayView: View? = null
    private var overlayLifecycleOwner: OverlayServiceLifecycleOwner? = null
    private var currentOverlayPackage: String? = null

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
                        withContext(Dispatchers.Main) { hideOverlayWindow() }
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
                                // Exceeded! Cover target app with limit overlay
                                withContext(Dispatchers.Main) {
                                    showOverlayWindow(currentForegroundApp, effectiveLimit)
                                }
                            } else {
                                if (currentOverlayPackage == currentForegroundApp) {
                                    withContext(Dispatchers.Main) { hideOverlayWindow() }
                                }
                            }
                        } else {
                            if (currentOverlayPackage != null) {
                                withContext(Dispatchers.Main) { hideOverlayWindow() }
                            }
                        }
                    } else if (currentForegroundApp == packageName) {
                        withContext(Dispatchers.Main) { hideOverlayWindow() }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000)
            }
        }
    }

    private fun showOverlayWindow(targetPackage: String, limit: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // If overlay permission not granted, fallback to activity intent
            if (currentForegroundAppOrTarget(targetPackage) != lastBlockedPackage || System.currentTimeMillis() - lastBlockedTime > 60000) {
                lastBlockedPackage = targetPackage
                lastBlockedTime = System.currentTimeMillis()
                launchBlockingScreen(targetPackage, limit)
            }
            return
        }

        if (overlayView != null && currentOverlayPackage == targetPackage) {
            return
        }

        hideOverlayWindow()

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val lifecycleOwner = OverlayServiceLifecycleOwner()
        lifecycleOwner.onCreate()
        lifecycleOwner.onResume()

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)

            setContent {
                PumpXTheme(appTheme = AppTheme.DARK) {
                    LimitReachedScreen(
                        targetPackage = targetPackage,
                        targetPushups = 10,
                        bonusMinutes = 15,
                        onStartPushups = {
                            hideOverlayWindow()
                            val intent = Intent(this@BlockerService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("blocked_package", targetPackage)
                                putExtra("direct_to_camera", true)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, params)
            overlayView = composeView
            overlayLifecycleOwner = lifecycleOwner
            currentOverlayPackage = targetPackage
        } catch (e: Exception) {
            e.printStackTrace()
            launchBlockingScreen(targetPackage, limit)
        }
    }

    private fun currentForegroundAppOrTarget(target: String): String = target

    private fun hideOverlayWindow() {
        if (overlayView != null) {
            try {
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayLifecycleOwner?.onDestroy()
            overlayLifecycleOwner = null
            overlayView = null
            currentOverlayPackage = null
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
        hideOverlayWindow()
        isRunning = false
        scope.cancel()
    }
}

