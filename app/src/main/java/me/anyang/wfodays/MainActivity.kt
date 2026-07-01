package me.anyang.wfodays

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.ui.components.BottomNavBar
import me.anyang.wfodays.ui.components.bottomNavItems
import me.anyang.wfodays.ui.screens.CalendarScreen
import me.anyang.wfodays.ui.screens.HomeScreen
import me.anyang.wfodays.ui.screens.LocationScreen
import me.anyang.wfodays.ui.screens.OnboardingScreen
import me.anyang.wfodays.ui.screens.SettingsScreen
import me.anyang.wfodays.ui.screens.StatsScreen
import me.anyang.wfodays.ui.theme.WFODaysTheme
import me.anyang.wfodays.utils.LanguageManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var locationManager: NativeLocationManager

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let {
            val language = LanguageManager.getCurrentLanguage(it)
            LanguageManager.setLanguage(it, language)
            it
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WFODaysTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    WFOApp(
                        preferencesManager = preferencesManager,
                        locationManager = locationManager
                    )
                }
            }
        }
    }
}

@Composable
fun WFOApp(
    preferencesManager: PreferencesManager,
    locationManager: NativeLocationManager
) {
    val navController = rememberNavController()
    val isFirstLaunch by preferencesManager.isFirstLaunch.collectAsState(initial = true)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that show bottom navigation
    val bottomBarRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isFirstLaunch) "onboarding" else "summary",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onNavigateToHome = {
                        navController.navigate("summary") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    preferencesManager = preferencesManager
                )
            }
            composable("summary") {
                HomeScreen(
                    onNavigateToCalendar = {
                        navController.navigate("calendar") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToStats = {
                        navController.navigate("trends") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("trends") {
                StatsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("location") {
                LocationScreen(
                    locationManager = locationManager
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    locationManager = locationManager
                )
            }
        }
    }
}
