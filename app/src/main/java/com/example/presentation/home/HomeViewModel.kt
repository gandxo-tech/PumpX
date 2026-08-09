package com.example.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.data.local.MonitoredAppEntity
import com.example.data.local.getEffectiveUsageToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

import com.example.core.permissions.PermissionUtils

data class HomeUiState(
    val userNickname: String = "",
    val todayPushups: Int = 0,
    val todayBonusMinutes: Int = 0,
    val totalScreenTimeTodayMinutes: Int = 0,
    val monitoredApps: List<MonitoredAppEntity> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val isLoading: Boolean = false,
    val totalPushupsAllTime: Int = 0,
    val level: Int = 1
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as PumpXApplication
    private val pushupRepository = app.pushupRepository
    private val monitoredAppRepository = app.monitoredAppRepository
    private val screenTimeRepository = app.screenTimeRepository
    private val userPreferencesRepository = app.userPreferencesRepository

    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.userNickname,
        pushupRepository.getTodayPushups(),
        pushupRepository.getTodayBonusMinutes(),
        monitoredAppRepository.allMonitoredApps,
        pushupRepository.getTotalPushupsAllTime(),
        _refreshTrigger
    ) { params ->
        val nickname = params[0] as String
        val pushups = params[1] as Int? ?: 0
        val bonus = params[2] as Int? ?: 0
        val apps = params[3] as List<MonitoredAppEntity>
        val allTimePushups = params[4] as Int? ?: 0

        val hasPermission = screenTimeRepository.isPermissionGranted()
        val hasOverlayPermission = PermissionUtils.hasOverlayPermission(getApplication())
        val totalScreenTime = if (hasPermission) {
            apps.sumOf { appEntity ->
                val rawUsage = screenTimeRepository.getTodayUsageMinutes(appEntity.packageName)
                if (appEntity.lastResetDateEpochDay == java.time.LocalDate.now().toEpochDay() && appEntity.initialUsageTodayMinutes == 0 && rawUsage > 0) {
                    viewModelScope.launch(Dispatchers.IO) {
                        monitoredAppRepository.checkAndAutoRepairBaseline(appEntity.packageName, rawUsage)
                    }
                }
                appEntity.getEffectiveUsageToday(rawUsage)
            }
        } else 0

        val level = (allTimePushups / 100) + 1

        HomeUiState(
            userNickname = nickname,
            todayPushups = pushups,
            todayBonusMinutes = bonus,
            totalScreenTimeTodayMinutes = totalScreenTime,
            monitoredApps = apps,
            hasUsagePermission = hasPermission,
            hasOverlayPermission = hasOverlayPermission,
            totalPushupsAllTime = allTimePushups,
            level = level
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun refresh() {
        _refreshTrigger.value++
    }
}
