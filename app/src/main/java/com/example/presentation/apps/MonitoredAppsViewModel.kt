package com.example.presentation.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.data.local.MonitoredAppEntity
import com.example.data.repository.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.local.getEffectiveUsageToday

data class MonitoredAppWithUsage(
    val entity: MonitoredAppEntity,
    val usageMinutesToday: Int,
    val iconDrawable: android.graphics.drawable.Drawable? = null
)

data class AppsUiState(
    val monitoredApps: List<MonitoredAppWithUsage> = emptyList(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val hasUsagePermission: Boolean = false,
    val isAddDialogOpen: Boolean = false,
    val searchQuery: String = "",
    val isLoadingInstalledApps: Boolean = false
)

class MonitoredAppsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PumpXApplication
    private val monitoredAppRepository = app.monitoredAppRepository
    private val screenTimeRepository = app.screenTimeRepository

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<AppsUiState> = combine(
        monitoredAppRepository.allMonitoredApps,
        _isAddDialogOpen,
        _searchQuery,
        _installedApps,
        _refreshTrigger
    ) { apps, isAddOpen, query, installedList, _ ->
        val hasPermission = screenTimeRepository.isPermissionGranted()

        val appsWithUsage = apps.map { entity ->
            val effectiveUsage = if (hasPermission) {
                val raw = screenTimeRepository.getTodayUsageMinutes(entity.packageName)
                if (entity.lastResetDateEpochDay == java.time.LocalDate.now().toEpochDay() && entity.initialUsageTodayMinutes == 0 && raw > 0) {
                    viewModelScope.launch(Dispatchers.IO) {
                        monitoredAppRepository.checkAndAutoRepairBaseline(entity.packageName, raw)
                    }
                }
                entity.getEffectiveUsageToday(raw)
            } else 0
            val icon = installedList.find { it.packageName == entity.packageName }?.iconDrawable
            MonitoredAppWithUsage(entity, effectiveUsage, icon)
        }

        val filteredInstalled = if (query.isBlank()) {
            installedList
        } else {
            installedList.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }

        AppsUiState(
            monitoredApps = appsWithUsage,
            installedApps = filteredInstalled,
            hasUsagePermission = hasPermission,
            isAddDialogOpen = isAddOpen,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppsUiState()
    )

    fun openAddDialog() {
        _isAddDialogOpen.value = true
        loadInstalledApps()
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = screenTimeRepository.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun addAppToMonitor(packageName: String, appName: String, dailyLimitMinutes: Int) {
        viewModelScope.launch {
            val currentUsage = screenTimeRepository.getTodayUsageMinutes(packageName)
            monitoredAppRepository.addOrUpdateApp(
                packageName = packageName,
                appName = appName,
                dailyLimitMinutes = dailyLimitMinutes,
                currentTodayUsageMinutes = currentUsage
            )
            closeAddDialog()
        }
    }

    fun updateAppLimit(packageName: String, appName: String, newLimitMinutes: Int) {
        viewModelScope.launch {
            val currentUsage = screenTimeRepository.getTodayUsageMinutes(packageName)
            monitoredAppRepository.addOrUpdateApp(
                packageName = packageName,
                appName = appName,
                dailyLimitMinutes = newLimitMinutes,
                currentTodayUsageMinutes = currentUsage
            )
        }
    }

    fun removeApp(packageName: String) {
        viewModelScope.launch {
            monitoredAppRepository.removeApp(packageName)
        }
    }

    fun refresh() {
        _refreshTrigger.value++
    }
}
