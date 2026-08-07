package com.example.core.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(private val context: Context) {

    private val firebaseAnalytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseAnalytics.getInstance(context)
        } catch (e: Exception) {
            Log.e("AnalyticsManager", "Firebase Analytics initialization failed: ${e.message}")
            null
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        try {
            val bundle = Bundle()
            params.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Float -> bundle.putFloat(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                }
            }
            firebaseAnalytics?.logEvent(eventName, bundle)
            Log.d("AnalyticsManager", "Logged event: $eventName with params: $params")
        } catch (e: Exception) {
            Log.e("AnalyticsManager", "Error logging event $eventName: ${e.message}")
        }
    }

    fun trackScreenView(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to screenName
        ))
    }

    fun trackUserNicknameSaved(hasNickname: Boolean) {
        logEvent("set_user_nickname", mapOf(
            "has_nickname" to hasNickname
        ))
    }

    fun trackPushupSessionStarted() {
        logEvent("pushup_session_start")
    }

    fun trackPushupCompleted(count: Int, timeSpentSeconds: Long) {
        logEvent("pushup_session_complete", mapOf(
            "count" to count,
            "duration_sec" to timeSpentSeconds
        ))
    }

    fun trackOnboardingCompleted() {
        logEvent("onboarding_completed")
    }
}
