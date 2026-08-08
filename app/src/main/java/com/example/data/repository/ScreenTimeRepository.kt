package com.example.data.repository

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.core.permissions.PermissionUtils
import java.util.Calendar

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val iconDrawable: android.graphics.drawable.Drawable? = null
)

class ScreenTimeRepository(private val context: Context) {

    fun isPermissionGranted(): Boolean {
        return PermissionUtils.hasUsageStatsPermission(context)
    }

    fun getInstalledApps(): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val selfPackageName = context.packageName

        return resolveInfos
            .mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == selfPackageName) return@mapNotNull null

                val appName = resolveInfo.loadLabel(packageManager).toString()
                val icon = try {
                    resolveInfo.loadIcon(packageManager)
                } catch (e: Exception) {
                    null
                }

                InstalledAppInfo(
                    packageName = pkgName,
                    appName = appName,
                    iconDrawable = icon
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun getTodayUsageMinutes(packageName: String): Int {
        if (!isPermissionGranted()) return 0

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val midnight = calendar.timeInMillis
        val installTime = try {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            0L
        }
        val startTime = maxOf(midnight, installTime)
        val endTime = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var totalTimeMs = 0L
        var lastEventTime = 0L
        var isForeground = false

        while (events.hasNextEvent()) {
            val event = android.app.usage.UsageEvents.Event()
            events.getNextEvent(event)

            if (event.packageName == packageName) {
                if (event.eventType == 1) { // ACTIVITY_RESUMED
                    lastEventTime = event.timeStamp
                    isForeground = true
                } else if (event.eventType == 2 || event.eventType == 23) { // ACTIVITY_PAUSED or STOPPED
                    if (isForeground) {
                        totalTimeMs += (event.timeStamp - lastEventTime)
                        isForeground = false
                    } else {
                        // Was in foreground before startTime
                        totalTimeMs += (event.timeStamp - startTime)
                    }
                }
            }
        }

        if (isForeground) {
            totalTimeMs += (endTime - lastEventTime)
        }

        return (totalTimeMs / 1000 / 60).toInt()
    }

    fun getTotalScreenTimeTodayMinutes(): Int {
        if (!isPermissionGranted()) return 0

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val midnight = calendar.timeInMillis
        val installTime = try {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            0L
        }
        val startTime = maxOf(midnight, installTime)
        val endTime = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var totalTimeMs = 0L
        val appStartTimes = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            val event = android.app.usage.UsageEvents.Event()
            events.getNextEvent(event)

            val pkg = event.packageName
            if (event.eventType == 1) { // ACTIVITY_RESUMED
                appStartTimes[pkg] = event.timeStamp
            } else if (event.eventType == 2 || event.eventType == 23) { // ACTIVITY_PAUSED or STOPPED
                val lastTime = appStartTimes[pkg]
                if (lastTime != null) {
                    totalTimeMs += (event.timeStamp - lastTime)
                    appStartTimes.remove(pkg)
                } else {
                    // Was in foreground before startTime
                    totalTimeMs += (event.timeStamp - startTime)
                }
            }
        }

        for ((_, lastTime) in appStartTimes) {
            totalTimeMs += (endTime - lastTime)
        }

        return (totalTimeMs / 1000 / 60).toInt()
    }
}
