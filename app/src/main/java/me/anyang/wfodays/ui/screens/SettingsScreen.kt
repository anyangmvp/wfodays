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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.anyang.wfodays.R
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.components.PermissionGuideCard
import me.anyang.wfodays.ui.theme.PrimaryBlue
import me.anyang.wfodays.ui.theme.PrimaryBlueDark
import me.anyang.wfodays.ui.theme.PrimaryBlueLight
import me.anyang.wfodays.ui.theme.SuccessGreen
import me.anyang.wfodays.utils.BatteryOptimizationHelper
import me.anyang.wfodays.utils.LanguageManager
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    locationManager: NativeLocationManager
) {
    val context = LocalContext.current

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

    // Update location display when state changes
    LaunchedEffect(locationState) {
        if (locationState is NativeLocationManager.LocationState.Success) {
            val state = locationState as NativeLocationManager.LocationState.Success
            currentLat = state.latitude
            currentLon = state.longitude
            currentAccuracy = state.accuracy
            distanceToOffice = locationManager.calculateDistanceToOffice(currentLat, currentLon)
        }
    }

    // Start location updates when permission is granted
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.setting),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
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
                            PrimaryBlue.copy(alpha = 0.05f),
                            Color.White
                        )
                    )
                )
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 位置测试卡片 - 商务风格
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                LocationTestCard(
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

            Spacer(modifier = Modifier.height(24.dp))

            // 权限设置标题
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SectionTitle(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.permissions_setting_title)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 位置权限
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
                    onRequest = { locationPermissionState.launchPermissionRequest() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 后台位置权限
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
                            onRequest = { state.launchPermissionRequest() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 通知权限
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
                            onRequest = { state.launchPermissionRequest() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 电池优化设置标题
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(850)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SectionTitle(
                    icon = Icons.Default.Settings,
                    title = stringResource(R.string.battery_optimization_setting_title)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 电池优化设置
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(900)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                BatteryOptimizationCard()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 测试通知
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(900)) + slideInVertically(
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

            Spacer(modifier = Modifier.height(24.dp))

            // 关于
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1000)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                SectionTitle(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.about_title)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(1100)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                AboutCard()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
    title: String
) {
    Row(
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
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp),
            color = PrimaryBlueDark
        )
    }
}

@Composable
private fun LocationTestCard(
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
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.location_test_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp),
                        color = PrimaryBlueDark
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading && hasPermission
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh_location_content_desc),
                        tint = if (isLoading) Color.Gray else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LoadingState()
            } else if (currentLat != 0.0 && currentLon != 0.0) {
                // 位置信息
                LocationInfo(
                    lat = currentLat,
                    lon = currentLon,
                    accuracy = currentAccuracy
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 距离卡片
                DistanceCard(
                    distance = distanceToOffice,
                    isInRange = isInRange
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 目标位置
                TargetLocationInfo()
            } else {
                EmptyLocationState(
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission
                )
            }
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
                modifier = Modifier.size(40.dp),
                color = PrimaryBlue,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.getting_location_text),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
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
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlueDark
        )
    }
}

@Composable
private fun DistanceCard(
    distance: Float,
    isInRange: Boolean
) {
    val cardColor = if (isInRange) SuccessGreen else PrimaryBlue
    val icon = if (isInRange) Icons.Default.Business else Icons.Default.HomeWork

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.distance_to_office, NativeLocationManager.OFFICE_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(R.string.distance_format_string, distance.toInt()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = cardColor
                )
                Text(
                    text = if (isInRange) stringResource(R.string.distance_in_range_message) else stringResource(R.string.distance_out_of_range_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInRange) SuccessGreen else Color.Gray
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
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.target_location_with_radius, 
                NativeLocationManager.OFFICE_LATITUDE.toString(), 
                NativeLocationManager.OFFICE_LONGITUDE.toString()),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp)
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
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasPermission) stringResource(R.string.click_to_refresh_location) else stringResource(R.string.need_location_permission),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            if (!hasPermission) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.test_notification),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = PrimaryBlueDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.test_notification_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isPressed = true
                    // 获取配置好语言的 Context，确保通知显示正确的语言
                    val localizedContext = LanguageManager.getLocalizedContext(context)
                    
                    // 根据当前位置判断是在公司还是家里
                    val isInOffice = if (currentLat != 0.0 && currentLon != 0.0) {
                        locationManager.isWithinOfficeRadius(currentLat, currentLon)
                    } else {
                        true
                    }

                    val distance = if (currentLat != 0.0 && currentLon != 0.0) {
                         locationManager.calculateDistanceToOffice(currentLat, currentLon)
                     } else {
                         100f // 默认距离
                     }
                     
                     // 距离显示逻辑（参考DailyLocationCheckWorker中的实现）
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
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
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
}

@Composable
private fun BatteryOptimizationCard() {
    val context = LocalContext.current
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    // 每次显示时刷新状态
    LaunchedEffect(Unit) {
        isIgnoringBatteryOptimizations = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isIgnoringBatteryOptimizations)
                                    SuccessGreen.copy(alpha = 0.1f)
                                else
                                    PrimaryBlue.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isIgnoringBatteryOptimizations)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (isIgnoringBatteryOptimizations) SuccessGreen else PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.battery_optimization_whitelist),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp),
                        color = PrimaryBlueDark
                    )
                }

                // 状态标签
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isIgnoringBatteryOptimizations)
                                SuccessGreen.copy(alpha = 0.1f)
                            else
                                PrimaryBlue.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isIgnoringBatteryOptimizations) stringResource(R.string.already_set) else stringResource(R.string.not_set),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isIgnoringBatteryOptimizations) SuccessGreen else PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 说明文字
            Text(
                text = stringResource(R.string.battery_optimization_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 品牌特定指南
            Text(
                text = BatteryOptimizationHelper.getBrandSpecificGuide(context),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isIgnoringBatteryOptimizations) {
                Button(
                    onClick = {
                        val intent = BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.add_to_whitelist))
                }
            } else {
                OutlinedButton(
                    onClick = {
                        val intent = BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.view_battery_settings))
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
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryBlueDark, PrimaryBlue, PrimaryBlueLight)
                )
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // 应用信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = context.applicationInfo.loadLabel(context.packageManager).toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.version_info, context.packageManager.getPackageInfo(context.packageName, 0).versionName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 描述
            Text(
                text = stringResource(R.string.app_description_short),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 作者信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.author_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 目标位置
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.target_location_format_new, 
                        NativeLocationManager.OFFICE_LATITUDE.toString(), 
                        NativeLocationManager.OFFICE_LONGITUDE.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
