package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.datastore.AppTheme
import com.example.core.navigation.Screen
import com.example.presentation.about.AboutScreen
import com.example.presentation.about.LegalScreen
import com.example.presentation.about.TermsScreen
import com.example.presentation.apps.MonitoredAppsScreen
import com.example.presentation.camera.CalibrationScreen
import com.example.presentation.camera.CameraSessionScreen
import com.example.presentation.camera.CameraViewModel
import com.example.presentation.home.HomeScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.share.ShareStatsScreen
import com.example.presentation.stats.StatsScreen
import com.example.ui.theme.PumpXTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            val serviceIntent = android.content.Intent(this, com.example.core.services.BlockerService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val app = application as PumpXApplication
        val userPrefs = app.userPreferencesRepository

        setContent {
            val appThemeState by userPrefs.appTheme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)
            
            // Use null as initial value to indicate loading state
            val onboardingState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
            
            androidx.compose.runtime.LaunchedEffect(Unit) {
                userPrefs.onboardingCompleted.collect { completed ->
                    if (onboardingState.value == null) {
                        onboardingState.value = completed
                    }
                }
            }

            val blockedPackage = intent?.getStringExtra("blocked_package")

            PumpXTheme(appTheme = appThemeState) {
                if (onboardingState.value != null) {
                    val initialRoute = if (blockedPackage != null) {
                        Screen.LimitReached.createRoute(blockedPackage, 10, 15)
                    } else if (onboardingState.value == true) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }

                    PumpXAppMain(
                        startDestination = initialRoute,
                        onCompleteOnboarding = {
                            val scope = (app as PumpXApplication)
                            // Handled inside scope
                        },
                        onSetOnboardingComplete = {
                            // Persist onboarding done
                            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                            scope.launch {
                                userPrefs.setOnboardingCompleted(true)
                            }
                        }
                    )
                } else {
                    // Show a simple loading screen or keep it blank while determining start destination
                    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun PumpXAppMain(
    startDestination: String,
    onCompleteOnboarding: () -> Unit,
    onSetOnboardingComplete: () -> Unit,
    cameraViewModel: CameraViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Stats.route,
        Screen.Apps.route,
        Screen.Settings.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            val context = navController.context
            val app = context.applicationContext as PumpXApplication
            val routeName = route.substringBefore("/")
            app.analyticsManager.trackScreenView(routeName)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Accueil") },
                        label = { Text("Accueil") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Stats.route,
                        onClick = {
                            navController.navigate(Screen.Stats.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = "Stats") },
                        label = { Text("Stats") },
                        modifier = Modifier.testTag("nav_item_stats")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Apps.route,
                        onClick = {
                            navController.navigate(Screen.Apps.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = "Apps") },
                        label = { Text("Apps") },
                        modifier = Modifier.testTag("nav_item_apps")
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Réglages") },
                        label = { Text("Réglages") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onCompleteOnboarding = {
                        val app = navController.context.applicationContext as PumpXApplication
                        app.analyticsManager.trackOnboardingCompleted()
                        onSetOnboardingComplete()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onSaveNickname = { nickname ->
                        val app = navController.context.applicationContext as PumpXApplication
                        app.analyticsManager.trackUserNicknameSaved(nickname.isNotBlank())
                        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                        scope.launch {
                            app.userPreferencesRepository.setUserNickname(nickname)
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCalibration = { navController.navigate(Screen.Calibration.route) },
                    onNavigateToApps = { navController.navigate(Screen.Apps.route) }
                )
            }

            composable(Screen.Stats.route) {
                StatsScreen(
                    onNavigateToShare = { navController.navigate(Screen.ShareStats.route) }
                )
            }

            composable(Screen.Apps.route) {
                MonitoredAppsScreen(
                    onStartPushupForApp = { pkg, pushups, bonus ->
                        navController.navigate(Screen.LimitReached.createRoute(pkg, pushups, bonus))
                    }
                )
            }

            composable(
                route = Screen.LimitReached.route,
                arguments = listOf(
                    navArgument("targetPackage") { type = NavType.StringType; defaultValue = "general" },
                    navArgument("targetPushups") { type = NavType.IntType; defaultValue = 10 },
                    navArgument("bonusMinutes") { type = NavType.IntType; defaultValue = 15 }
                )
            ) { backStackEntry ->
                val targetPackage = backStackEntry.arguments?.getString("targetPackage") ?: "general"
                val targetPushups = backStackEntry.arguments?.getInt("targetPushups") ?: 10
                val bonusMinutes = backStackEntry.arguments?.getInt("bonusMinutes") ?: 15

                com.example.presentation.blocker.LimitReachedScreen(
                    targetPackage = targetPackage,
                    targetPushups = targetPushups,
                    bonusMinutes = bonusMinutes,
                    onStartPushups = {
                        navController.navigate(Screen.CameraSession.createRoute(targetPackage, targetPushups, bonusMinutes)) {
                            popUpTo(Screen.LimitReached.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToAbout = { navController.navigate(Screen.About.route) },
                    onNavigateToLegal = { navController.navigate(Screen.Legal.route) },
                    onNavigateToTerms = { navController.navigate(Screen.Terms.route) }
                )
            }

            composable(Screen.Calibration.route) {
                CalibrationScreen(
                    onCalibrationComplete = {
                        navController.navigate(Screen.CameraSession.createRoute("general", 10, 15)) {
                            popUpTo(Screen.Calibration.route) { inclusive = true }
                        }
                    },
                    viewModel = cameraViewModel
                )
            }

            composable(
                route = Screen.CameraSession.route,
                arguments = listOf(
                    navArgument("targetPackage") { type = NavType.StringType; defaultValue = "general" },
                    navArgument("targetPushups") { type = NavType.IntType; defaultValue = 10 },
                    navArgument("bonusMinutes") { type = NavType.IntType; defaultValue = 15 }
                )
            ) { backStackEntry ->
                val targetPackage = backStackEntry.arguments?.getString("targetPackage") ?: "general"
                val targetPushups = backStackEntry.arguments?.getInt("targetPushups") ?: 10
                val bonusMinutes = backStackEntry.arguments?.getInt("bonusMinutes") ?: 15

                CameraSessionScreen(
                    targetPackage = targetPackage,
                    targetPushups = targetPushups,
                    bonusMinutes = bonusMinutes,
                    onFinishSession = {
                        navController.popBackStack(Screen.Home.route, false)
                    },
                    viewModel = cameraViewModel
                )
            }

            composable(Screen.ShareStats.route) {
                ShareStatsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Legal.route) {
                LegalScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Terms.route) {
                TermsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
