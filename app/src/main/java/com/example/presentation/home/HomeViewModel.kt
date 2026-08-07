package com.example.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.data.local.MonitoredAppEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val userNickname: String = "",
    val todayPushups: Int = 0,
    val todayBonusMinutes: Int = 0,
    val totalScreenTimeTodayMinutes: Int = 0,
    val monitoredApps: List<MonitoredAppEntity> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val isLoading: Boolean = false
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
        _refreshTrigger
    ) { nickname, pushups, bonus, apps, _ ->
        val hasPermission = screenTimeRepository.isPermissionGranted()
        val totalScreenTime = if (hasPermission) screenTimeRepository.getTotalScreenTimeTodayMinutes() else 0

        HomeUiState(
            userNickname = nickname,
            todayPushups = pushups ?: 0,
            todayBonusMinutes = bonus ?: 0,
            totalScreenTimeTodayMinutes = totalScreenTime,
            monitoredApps = apps,
            hasUsagePermission = hasPermission
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
