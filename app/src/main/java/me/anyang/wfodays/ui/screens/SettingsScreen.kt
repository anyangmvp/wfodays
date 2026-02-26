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
import me.anyang.wfodays.ui.components.SettingsCard
import me.anyang.wfodays.ui.components.SettingsGroupTitle
import me.anyang.wfodays.ui.theme.NeutralGray200
import me.anyang.wfodays.ui.theme.NeutralGray400
import me.anyang.wfodays.ui.theme.NeutralGray500
import me.anyang.wfodays.ui.theme.NeutralGray600
import me.anyang.wfodays.ui.theme.NeutralGray700
import me.anyang.wfodays.ui.theme.NeutralGray900
import me.anyang.wfodays.ui.theme.PrimaryBlue
import me.anyang.wfodays.ui.theme.PrimaryBlueDark
import me.anyang.wfodays.ui.theme.PrimaryBlueLight
import me.anyang.wfodays.ui.theme.SuccessGreen
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 位置测试卡片
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

            // 权限设置分组
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
                    onRequest = { locationPermissionState.launchPermissionRequest() },
                    icon = Icons.Default.LocationOn
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
                            onRequest = { state.launchPermissionRequest() },
                            icon = Icons.Default.LocationOn
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
                            onRequest = { state.launchPermissionRequest() },
                            icon = Icons.Default.Notifications
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 测试通知分组
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

            // 测试通知
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

            Spacer(modifier = Modifier.height(20.dp))

            // 关于分组
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
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 标题行 - 标题和刷新按钮在同一行
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
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
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

                // 刷新按钮
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

            // 内容区域
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

    SettingsCard(
        title = stringResource(R.string.test_notification),
        icon = Icons.Default.Notifications
    ) {
        Text(
            text = stringResource(R.string.test_notification_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
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
            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun AboutCard() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = NeutralGray400.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 应用信息区域
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 应用图标容器 - 使用柔和的渐变背景
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PrimaryBlue.copy(alpha = 0.1f),
                                    PrimaryBlueLight.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryBlue,
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
                        color = NeutralGray900
                    )
                    Text(
                        text = stringResource(R.string.version_info, context.packageManager.getPackageInfo(context.packageName, 0).versionName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralGray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 分隔线 - 使用浅灰色
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NeutralGray200)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 应用描述
            Text(
                text = stringResource(R.string.app_description_short),
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray600,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 信息列表
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 作者信息
                InfoRow(
                    icon = Icons.Default.Person,
                    iconBackgroundColor = SuccessGreen.copy(alpha = 0.1f),
                    iconTint = SuccessGreen,
                    label = stringResource(R.string.author_label),
                    value = "Stephen An"
                )

                // 目标位置
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    iconBackgroundColor = PrimaryBlue.copy(alpha = 0.1f),
                    iconTint = PrimaryBlue,
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
    iconBackgroundColor: Color,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // 图标容器
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralGray400
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralGray700,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
