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
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.utils.LanguageManager
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

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                SettingsSection(title = "TARGET") {
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
                SettingsSection(title = "LOCATION") {
                    SettingsItem(
                        icon = Icons.Default.Business,
                        iconColor = PrimaryBlue,
                        title = "Office Location",
                        subtitle = NativeLocationManager.OFFICE_NAME,
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.MyLocation,
                        iconColor = PrimaryBlue,
                        title = "Office Radius",
                        subtitle = "${NativeLocationManager.OFFICE_RADIUS_METERS.toInt()} m",
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.LocationOn,
                        iconColor = PrimaryBlue,
                        title = "Calibrate Office Location",
                        subtitle = null,
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
                SettingsSection(title = "NOTIFICATIONS") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconColor = WarningOrange,
                        title = "Test Notification",
                        subtitle = "Send a test notification",
                        onClick = {
                            NotificationHelper.showAttendanceNotification(
                                context,
                                LocalDate.now(),
                                "Test Notification",
                                "This is a test notification from WFO Days"
                            )
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
                SettingsSection(title = "PERMISSIONS") {
                    PermissionItem(
                        icon = Icons.Default.LocationOn,
                        iconColor = SuccessGreen,
                        title = "Location Permission",
                        isGranted = locationPermissionState.status.isGranted,
                        onClick = { locationPermissionState.launchPermissionRequest() }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PermissionItem(
                            icon = Icons.Default.LocationOn,
                            iconColor = SuccessGreen,
                            title = "Background Location",
                            isGranted = backgroundLocationState?.status?.isGranted ?: false,
                            onClick = { backgroundLocationState?.launchPermissionRequest() }
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionItem(
                            icon = Icons.Default.Notifications,
                            iconColor = SuccessGreen,
                            title = "Notifications",
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
                SettingsSection(title = "ABOUT") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconColor = Gray400,
                        title = "WFO Days",
                        subtitle = "Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
                        showArrow = false,
                        onClick = { }
                    )
                    SettingsItem(
                        icon = Icons.Default.NewReleases,
                        iconColor = Gray400,
                        title = "What's New",
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
                        title = "Privacy (Local Only)",
                        subtitle = "All data is stored on this device only.",
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "WFO Target",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Set your monthly WFO target percentage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${sliderValue.toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue,
                        inactiveTrackColor = Gray300
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("10%", style = MaterialTheme.typography.bodySmall, color = Gray400)
                    Text("100%", style = MaterialTheme.typography.bodySmall, color = Gray400)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(sliderValue) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
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
                text = "WFO Target",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = "Monthly office attendance goal",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = "${percentage.toInt()}%",
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
            text = if (isGranted) "Allowed" else "Not Allowed",
            style = MaterialTheme.typography.bodySmall,
            color = if (isGranted) SuccessGreen else WarningOrange
        )
    }
}
