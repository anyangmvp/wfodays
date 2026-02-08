package me.anyang.wfodays

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.ui.screens.CalendarScreen
import me.anyang.wfodays.ui.screens.HomeScreen
import me.anyang.wfodays.ui.screens.OnboardingScreen
import me.anyang.wfodays.ui.screens.SettingsScreen
import me.anyang.wfodays.ui.screens.StatsScreen
import me.anyang.wfodays.ui.theme.WFODaysTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var locationManager: NativeLocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            WFODaysTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
    
    NavHost(
        navController = navController,
        startDestination = if (isFirstLaunch) "onboarding" else "home"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                preferencesManager = preferencesManager
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("calendar") {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("stats") {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
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
