package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pumpx_settings")

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_NICKNAME = stringPreferencesKey("user_nickname")
    }

    val appTheme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        val themeName = prefs[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
        try {
            AppTheme.valueOf(themeName)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val userNickname: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_NICKNAME] ?: ""
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setUserNickname(nickname: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NICKNAME] = nickname.trim()
        }
    }
}
