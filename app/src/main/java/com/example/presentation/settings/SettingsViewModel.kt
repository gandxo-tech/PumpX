package com.example.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PumpXApplication
import com.example.core.datastore.AppTheme
import com.example.core.permissions.PermissionUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val hasCameraPermission: Boolean = false,
    val hasUsagePermission: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PumpXApplication
    private val userPrefs = app.userPreferencesRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        userPrefs.appTheme
    ) { themeArray ->
        val theme = themeArray[0]
        SettingsUiState(
            appTheme = theme,
            hasCameraPermission = PermissionUtils.hasCameraPermission(app),
            hasUsagePermission = PermissionUtils.hasUsageStatsPermission(app)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPrefs.setAppTheme(theme)
        }
    }
}
