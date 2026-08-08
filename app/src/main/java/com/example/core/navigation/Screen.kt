package com.example.core.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Stats : Screen("stats")
    object Apps : Screen("apps")
    object Settings : Screen("settings")
    object Calibration : Screen("calibration")
    object CameraSession : Screen("camera_session/{targetPackage}/{targetPushups}/{bonusMinutes}") {
        fun createRoute(targetPackage: String = "general", targetPushups: Int = 10, bonusMinutes: Int = 15): String {
            return "camera_session/$targetPackage/$targetPushups/$bonusMinutes"
        }
    }
    object LimitReached : Screen("limit_reached/{targetPackage}/{targetPushups}/{bonusMinutes}") {
        fun createRoute(targetPackage: String = "general", targetPushups: Int = 10, bonusMinutes: Int = 15): String {
            return "limit_reached/$targetPackage/$targetPushups/$bonusMinutes"
        }
    }
    object ShareStats : Screen("share_stats")
    object About : Screen("about")
    object Legal : Screen("legal")
    object Terms : Screen("terms")
}
