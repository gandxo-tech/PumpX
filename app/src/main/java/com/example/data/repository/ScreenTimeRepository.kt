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
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, midnight, endTime)
        
        var totalTimeMs = 0L
        if (stats != null) {
            for (usageStats in stats) {
                if (usageStats.packageName == packageName) {
                    totalTimeMs += usageStats.totalTimeInForeground
                }
            }
        }

        return (totalTimeMs / 1000 / 60).toInt()
    }

    fun getMonitoredScreenTimeTodayMinutes(packageNames: List<String>): Int {
        if (!isPermissionGranted() || packageNames.isEmpty()) return 0

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val midnight = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, midnight, endTime)
        
        var totalTimeMs = 0L
        if (stats != null) {
            val monitoredSet = packageNames.toSet()
            for (usageStats in stats) {
                if (monitoredSet.contains(usageStats.packageName)) {
                    totalTimeMs += usageStats.totalTimeInForeground
                }
            }
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
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, midnight, endTime)
        
        var totalTimeMs = 0L
        if (stats != null) {
            for (usageStats in stats) {
                totalTimeMs += usageStats.totalTimeInForeground
            }
        }

        return (totalTimeMs / 1000 / 60).toInt()
    }
}
