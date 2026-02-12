package me.anyang.wfodays.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.anyang.wfodays.R
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.DailyCheckScheduler
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.components.PermissionGuideCard
import me.anyang.wfodays.ui.components.SettingsCard
import me.anyang.wfodays.ui.components.SettingsGroupTitle
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.utils.LanguageManager
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    locationManager: NativeLocationManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val preferencesManager = remember { PreferencesManager(context) }
    val debugNotificationMode by preferencesManager.debugNotificationMode.collectAsState(initial = false)
    val debugNotificationInterval by preferencesManager.debugNotificationInterval.collectAsState(initial = 10)

    val locationState by locationManager.locationState.collectAsState()

    var currentLat by remember { mutableStateOf(0.0) }
    var currentLon by remember { mutableStateOf(0.0) }
    var currentAccuracy by remember { mutableStateOf(0f) }
    var distanceToOffice by remember { mutableStateOf(0f) }
    var isVisible by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(locationState) {
        if (locationState is NativeLocationManager.LocationState.Success) {
            val state = locationState as NativeLocationManager.LocationState.Success
            currentLat = state.latitude
            currentLon = state.longitude
            currentAccuracy = state.accuracy
            distanceToOffice = locationManager.calculateDistanceToOffice(currentLat, currentLon)
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            locationManager.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Scaffold(
        containerColor = JoyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.setting),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JoyCoral,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JoyCoral.copy(alpha = 0.08f),
                            JoyBackground,
                            JoyCardAccent1.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                LocationTestCard(
                    title = stringResource(R.string.location_test_card_title),
                    icon = Icons.Default.LocationOn,
                    locationState = locationState,
                    currentLat = currentLat,
                    currentLon = currentLon,
                    currentAccuracy = currentAccuracy,
                    distanceToOffice = distanceToOffice,
                    onRefresh = { locationManager.getLastKnownLocation() },
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() },
                    hasPermission = locationPermissionState.status.isGranted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SettingsGroupTitle(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.permissions_setting_title)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                PermissionGuideCard(
                    title = stringResource(R.string.location_permission_title),
                    description = stringResource(R.string.location_permission_desc),
                    isGranted = locationPermissionState.status.isGranted,
                    onRequest = { locationPermissionState.launchPermissionRequest() },
                    icon = Icons.Default.LocationOn
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(700)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                ) {
                    backgroundLocationState?.let { state ->
                        PermissionGuideCard(
                            title = stringResource(R.string.background_location_permission_title),
                            description = stringResource(R.string.background_location_permission_desc),
                            isGranted = state.status.isGranted,
                            onRequest = { state.launchPermissionRequest() },
                            icon = Icons.Default.LocationOn
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(800)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                ) {
                    notificationPermissionState?.let { state ->
                        PermissionGuideCard(
                            title = stringResource(R.string.notification_permission_title),
                            description = stringResource(R.string.notification_permission_desc),
                            isGranted = state.status.isGranted,
                            onRequest = { state.launchPermissionRequest() },
                            icon = Icons.Default.Notifications
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(950)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SettingsGroupTitle(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.test_notification)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                NotificationTestCard(
                    currentLat = currentLat,
                    currentLon = currentLon,
                    locationManager = locationManager
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1050)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                DebugNotificationModeCard(
                    isEnabled = debugNotificationMode,
                    intervalMinutes = debugNotificationInterval,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            preferencesManager.setDebugNotificationMode(enabled)
                            DailyCheckScheduler.scheduleDailyCheck(context)
                        }
                    },
                    onIntervalChange = { minutes ->
                        coroutineScope.launch {
                            preferencesManager.setDebugNotificationInterval(minutes)
                            DailyCheckScheduler.scheduleDailyCheck(context)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1050)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SettingsGroupTitle(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_title)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1100)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                AboutCard()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LocationTestCard(
    title: String,
    icon: ImageVector,
    locationState: NativeLocationManager.LocationState,
    currentLat: Double,
    currentLon: Double,
    currentAccuracy: Float,
    distanceToOffice: Float,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    hasPermission: Boolean
) {
    val isInRange = distanceToOffice <= NativeLocationManager.OFFICE_RADIUS_METERS
    val isLoading = locationState is NativeLocationManager.LocationState.Loading

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyOrange.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = JoyOrange.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(JoyGradientPrimary)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 14.dp),
                        color = JoyOnBackground
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading && hasPermission
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isLoading) JoyGray200 else JoyOrange.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_location_content_desc),
                            tint = if (isLoading) JoyGray400 else JoyOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LocationTestContentBody(
                locationState = locationState,
                currentLat = currentLat,
                currentLon = currentLon,
                currentAccuracy = currentAccuracy,
                distanceToOffice = distanceToOffice,
                isInRange = isInRange,
                isLoading = isLoading,
                onRequestPermission = onRequestPermission,
                hasPermission = hasPermission
            )
        }
    }
}

@Composable
private fun LocationTestContentBody(
    locationState: NativeLocationManager.LocationState,
    currentLat: Double,
    currentLon: Double,
    currentAccuracy: Float,
    distanceToOffice: Float,
    isInRange: Boolean,
    isLoading: Boolean,
    onRequestPermission: () -> Unit,
    hasPermission: Boolean
) {
    Column {
        if (isLoading) {
            LoadingState()
        } else if (currentLat != 0.0 && currentLon != 0.0) {
            LocationInfo(
                lat = currentLat,
                lon = currentLon,
                accuracy = currentAccuracy
            )

            Spacer(modifier = Modifier.height(16.dp))

            DistanceCard(
                distance = distanceToOffice,
                isInRange = isInRange
            )

            Spacer(modifier = Modifier.height(12.dp))

            TargetLocationInfo()
        } else {
            EmptyLocationState(
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(44.dp),
                color = JoyOrange,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.getting_location_text),
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocationInfo(
    lat: Double,
    lon: Double,
    accuracy: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LocationInfoItem(
            label = stringResource(R.string.latitude_label),
            value = "${String.format("%.6f", lat)}°N"
        )
        LocationInfoItem(
            label = stringResource(R.string.longitude_label),
            value = "${String.format("%.6f", lon)}°E"
        )
        LocationInfoItem(
            label = stringResource(R.string.accuracy_label),
            value = stringResource(R.string.accuracy_format_string, String.format("%.0f", accuracy))
        )
    }
}

@Composable
private fun LocationInfoItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = JoyOnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = JoyOnBackground
        )
    }
}

@Composable
private fun DistanceCard(
    distance: Float,
    isInRange: Boolean
) {
    val gradientColors = if (isInRange) JoyGradientWFH else JoyGradientPrimary
    val icon = if (isInRange) Icons.Default.Business else Icons.Default.HomeWork

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.15f) })
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = gradientColors.first().copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.padding(start = 18.dp)
            ) {
                Text(
                    text = stringResource(R.string.distance_to_office, NativeLocationManager.OFFICE_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = JoyOnSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.distance_format_string, distance.toInt()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = gradientColors.first()
                )
                Text(
                    text = if (isInRange) stringResource(R.string.distance_in_range_message) else stringResource(R.string.distance_out_of_range_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInRange) JoyMint else JoyOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TargetLocationInfo() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = JoyOnSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.target_location_with_radius, 
                NativeLocationManager.OFFICE_LATITUDE.toString(), 
                NativeLocationManager.OFFICE_LONGITUDE.toString()),
            style = MaterialTheme.typography.bodySmall,
            color = JoyOnSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun EmptyLocationState(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        JoyCardAccent1.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = JoyOrange,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (hasPermission) stringResource(R.string.click_to_refresh_location) else stringResource(R.string.need_location_permission),
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnSurfaceVariant
            )
            if (!hasPermission) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = JoyOrange),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.grant_location_permission_button))
                }
            }
        }
    }
}

@Composable
private fun NotificationTestCard(
    currentLat: Double,
    currentLon: Double,
    locationManager: NativeLocationManager
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    SettingsCard(
        title = stringResource(R.string.test_notification),
        icon = Icons.Default.Notifications
    ) {
        Text(
            text = stringResource(R.string.test_notification_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = JoyOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                isPressed = true
                val localizedContext = LanguageManager.getLocalizedContext(context)

                val isInOffice = if (currentLat != 0.0 && currentLon != 0.0) {
                    locationManager.isWithinOfficeRadius(currentLat, currentLon)
                } else {
                    true
                }

                val distance = if (currentLat != 0.0 && currentLon != 0.0) {
                     locationManager.calculateDistanceToOffice(currentLat, currentLon)
                 } else {
                     100f
                 }

                 val distanceDisplay = if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                     val roundedDistance = ((distance / 100).toInt()) * 100
                     localizedContext.getString(R.string.distance_format_string, roundedDistance)
                 } else {
                     localizedContext.getString(R.string.distance_kilometer_format, distance / 1000)
                 }

                 if (isInOffice) {
                     NotificationHelper.showAttendanceNotification(
                         localizedContext,
                         LocalDate.now(),
                         localizedContext.getString(R.string.notification_title_wfo_success),
                         localizedContext.getString(R.string.notification_message_office_distance, distanceDisplay)
                     )
                 } else {
                     NotificationHelper.showAttendanceNotification(
                         localizedContext,
                         LocalDate.now(),
                         localizedContext.getString(R.string.notification_title_wfh_recorded),
                         localizedContext.getString(R.string.notification_message_home_distance, distanceDisplay)
                     )
                 }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = JoyOrange
            )
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.send_test_notification))
        }
    }
}

@Composable
private fun DebugNotificationModeCard(
    isEnabled: Boolean,
    intervalMinutes: Int,
    onToggle: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit
) {
    var intervalInput by remember { mutableStateOf(intervalMinutes.toString()) }
    var isEditing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyPurple.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = JoyPurple.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(JoyPurple, JoyLavender))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.debug_notification_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = JoyOnBackground
                        )
                        Text(
                            text = stringResource(R.string.debug_notification_mode_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = JoyOnSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JoyPurple,
                        checkedTrackColor = JoyPurple.copy(alpha = 0.5f),
                        uncheckedThumbColor = JoyGray400,
                        uncheckedTrackColor = JoyGray200
                    )
                )
            }

            AnimatedVisibility(
                visible = isEnabled,
                enter = fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = fadeOut(tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(JoyPurple.copy(alpha = 0.06f))
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.execution_interval),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = JoyOnBackground
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 10, 15, 30).forEach { minutes ->
                                    val isSelected = intervalMinutes == minutes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) JoyPurple else Color.White
                                            )
                                            .clickable {
                                                intervalInput = minutes.toString()
                                                onIntervalChange(minutes)
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.interval_minutes, minutes),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else JoyPurple
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.custom_interval),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JoyOnSurfaceVariant
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(
                                            width = if (isEditing) 2.dp else 1.dp,
                                            color = if (isEditing) JoyPurple else JoyGray300,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    BasicTextField(
                                        value = intervalInput,
                                        onValueChange = { newValue ->
                                            val filtered = newValue.filter { it.isDigit() }
                                            if (filtered.length <= 2) {
                                                intervalInput = filtered
                                                filtered.toIntOrNull()?.let { minutes ->
                                                    if (minutes in 1..60) {
                                                        onIntervalChange(minutes)
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = JoyOnBackground,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        decorationBox = { innerTextField ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (intervalInput.isEmpty()) {
                                                    Text(
                                                        text = stringResource(R.string.input_hint),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = JoyGray400
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }

                                Text(
                                    text = stringResource(R.string.minutes_unit),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = JoyOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutCard() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyMint.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = JoyMint.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(listOf(JoyMint, JoyTeal))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 18.dp)
                ) {
                    Text(
                        text = context.applicationInfo.loadLabel(context.packageManager).toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = JoyOnBackground
                    )
                    Text(
                        text = stringResource(R.string.version_info, context.packageManager.getPackageInfo(context.packageName, 0).versionName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(JoyGray200)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.app_description_short),
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.Person,
                    gradientColors = listOf(JoyOrange, JoyCoral),
                    label = stringResource(R.string.author_label),
                    value = "Stephen An"
                )

                InfoRow(
                    icon = Icons.Default.LocationOn,
                    gradientColors = listOf(JoyMint, JoyMintDark),
                    label = stringResource(R.string.target_location_label),
                    value = NativeLocationManager.OFFICE_NAME
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    gradientColors: List<Color>,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = gradientColors.first().copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(gradientColors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = JoyOnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnBackground,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
