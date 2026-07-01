package me.anyang.wfodays.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import me.anyang.wfodays.R
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    locationManager: NativeLocationManager
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }
    var targetPercentage by remember { mutableFloatStateOf(30f) }
    var officeRadius by remember { mutableFloatStateOf(800f) }
    var showPercentageDialog by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // Load target percentage from preferences
    LaunchedEffect(Unit) {
        preferencesManager.wfoTargetPercentage.collect { percentage ->
            targetPercentage = percentage
        }
    }

    // Load office radius from preferences
    LaunchedEffect(Unit) {
        preferencesManager.officeRadius.collect { radius ->
            officeRadius = radius
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // TARGET Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400))
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_target)) {
                    TargetPercentageItem(
                        percentage = targetPercentage,
                        onClick = { showPercentageDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LOCATION Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500))
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_location)) {
                    SettingsItem(
                        icon = Icons.Default.Business,
                        iconColor = PrimaryBlue,
                        title = stringResource(R.string.office_location),
                        subtitle = NativeLocationManager.OFFICE_NAME,
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.MyLocation,
                        iconColor = PrimaryBlue,
                        title = stringResource(R.string.office_radius),
                        subtitle = stringResource(R.string.meters_format, officeRadius.toInt()),
                        showArrow = false,
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NOTIFICATIONS Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600))
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconColor = WarningOrange,
                        title = stringResource(R.string.test_notification),
                        subtitle = stringResource(R.string.test_notification_subtitle),
                        onClick = {
                            scope.launch {
                                if (!locationPermissionState.status.isGranted) {
                                    NotificationHelper.showAttendanceNotification(
                                        context,
                                        LocalDate.now(),
                                        context.getString(R.string.test_check_in),
                                        context.getString(R.string.test_checkin_permission_required)
                                    )
                                    return@launch
                                }

                                // Try to use existing location state first
                                var location = locationManager.locationState.value
                                        as? NativeLocationManager.LocationState.Success

                                if (location == null) {
                                    // Start location updates to get fresh location
                                    locationManager.startLocationUpdates()
                                    location = withTimeoutOrNull(5000L) {
                                        locationManager.locationState.first {
                                            it is NativeLocationManager.LocationState.Success
                                        }
                                    } as? NativeLocationManager.LocationState.Success
                                }

                                if (location != null) {
                                    val distance = locationManager.calculateDistanceToOffice(
                                        location.latitude, location.longitude
                                    )
                                    val distanceDisplay =
                                        if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                                            context.getString(R.string.meters_format, distance.toInt())
                                        } else {
                                            context.getString(R.string.distance_kilometer_format, distance / 1000)
                                        }
                                    val isInRange =
                                        distance <= NativeLocationManager.OFFICE_RADIUS_METERS

                                    NotificationHelper.showAttendanceNotification(
                                        context,
                                        LocalDate.now(),
                                        if (isInRange) context.getString(R.string.test_check_in_wfo) else context.getString(R.string.test_check_in_wfh),
                                        context.getString(R.string.distance_to_office_format, distanceDisplay)
                                    )
                                } else {
                                    NotificationHelper.showAttendanceNotification(
                                        context,
                                        LocalDate.now(),
                                        context.getString(R.string.test_check_in),
                                        context.getString(R.string.unable_to_get_location)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PERMISSIONS Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700))
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_permissions)) {
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        iconColor = SuccessGreen,
                        title = stringResource(R.string.location_permission_title),
                        isGranted = locationPermissionState.status.isGranted,
                        onClick = { locationPermissionState.launchPermissionRequest() }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PermissionItem(
                            icon = Icons.Default.LocationOn,
                            iconColor = SuccessGreen,
                            title = stringResource(R.string.background_location_permission_title),
                            isGranted = backgroundLocationState?.status?.isGranted ?: false,
                            onClick = { backgroundLocationState?.launchPermissionRequest() }
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionItem(
                            icon = Icons.Default.Notifications,
                            iconColor = SuccessGreen,
                            title = stringResource(R.string.notifications),
                            isGranted = notificationPermissionState?.status?.isGranted ?: false,
                            onClick = { notificationPermissionState?.launchPermissionRequest() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ABOUT Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800))
            ) {
                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconColor = Gray400,
                        title = stringResource(R.string.wfo_days),
                        subtitle = context.getString(
                            R.string.version_format,
                            context.packageManager.getPackageInfo(context.packageName, 0).versionName
                        ),
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.NewReleases,
                        iconColor = Gray400,
                        title = stringResource(R.string.whats_new),
                        subtitle = null,
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://github.com/anyangmvp/wfodays/releases")
                            )
                            context.startActivity(intent)
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        iconColor = Gray400,
                        title = stringResource(R.string.privacy_local_only),
                        subtitle = stringResource(R.string.privacy_local_only_desc),
                        showArrow = false,
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Percentage Dialog
        if (showPercentageDialog) {
            PercentageDialog(
                currentPercentage = targetPercentage,
                onDismiss = { showPercentageDialog = false },
                onConfirm = { percentage ->
                    targetPercentage = percentage
                    showPercentageDialog = false
                    scope.launch {
                        preferencesManager.setWfoTargetPercentage(percentage)
                    }
                }
            )
        }
    }
}

@Composable
private fun PercentageDialog(
    currentPercentage: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(currentPercentage) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BackgroundWhite)
        ) {
            Column {
                // Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.wfo_target),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.set_wfo_target_percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Slider content
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.percentage_format, sliderValue.toInt()),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryBlue,
                            activeTrackColor = PrimaryBlue,
                            inactiveTrackColor = Gray200
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.slider_min_value), style = MaterialTheme.typography.bodySmall, color = Gray400)
                        Text(stringResource(R.string.slider_max_value), style = MaterialTheme.typography.bodySmall, color = Gray400)
                    }
                }

                // Buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Gray200)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(44.dp)
                            .background(Gray200)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onConfirm(sliderValue) }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetPercentageItem(
    percentage: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.wfo_target),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.monthly_office_attendance_goal),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = stringResource(R.string.percentage_format, percentage.toInt()),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Gray300,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Gray400,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BackgroundWhite)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Gray300,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isGranted) onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) SuccessGreen else iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }

        Text(
            text = if (isGranted) stringResource(R.string.allowed) else stringResource(R.string.not_allowed),
            style = MaterialTheme.typography.bodySmall,
            color = if (isGranted) SuccessGreen else WarningOrange
        )
    }
}
