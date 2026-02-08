package me.anyang.wfodays.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.res.stringResource
import me.anyang.wfodays.R
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.components.PermissionGuideCard
import me.anyang.wfodays.ui.theme.HSBCRed
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 位置测试卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "位置测试",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                locationManager.getLastKnownLocation()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新位置"
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (locationState is NativeLocationManager.LocationState.Loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = HSBCRed
                            )
                            Text(
                                text = "  正在获取位置...",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (currentLat != 0.0 && currentLon != 0.0) {
                        // 显示当前位置
                        Text(
                            text = "当前位置:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "纬度: ${String.format("%.6f", currentLat)}°N",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "经度: ${String.format("%.6f", currentLon)}°E",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "精度: ±${currentAccuracy.toInt()} 米",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 显示与办公室的距离
                        val isInRange = distanceToOffice <= NativeLocationManager.OFFICE_RADIUS_METERS
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isInRange) 
                                    androidx.compose.ui.graphics.Color(0xFFE8F5E9) 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "距离${NativeLocationManager.OFFICE_NAME}:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${distanceToOffice.toInt()} 米",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInRange) 
                                        androidx.compose.ui.graphics.Color(0xFF00A550) 
                                    else 
                                        HSBCRed
                                )
                                if (isInRange) {
                                    Text(
                                        text = "✓ 在800米范围内，将自动记录WFO",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.ui.graphics.Color(0xFF00A550)
                                    )
                                } else {
                                    Text(
                                        text = "超出800米范围",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 显示目标位置
                        Text(
                            text = "目标位置 (${NativeLocationManager.OFFICE_NAME}):",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${NativeLocationManager.OFFICE_LATITUDE}°N, ${NativeLocationManager.OFFICE_LONGITUDE}°E",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "点击刷新按钮获取当前位置",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                locationManager.getLastKnownLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("获取当前位置")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "权限设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 位置权限
            PermissionGuideCard(
                title = "位置权限",
                description = "用于检测您是否在公司附近",
                isGranted = locationPermissionState.status.isGranted,
                onRequest = { locationPermissionState.launchPermissionRequest() }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 后台位置权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationState?.let { state ->
                    PermissionGuideCard(
                        title = "后台位置权限",
                        description = "即使App关闭也能检测到公司",
                        isGranted = state.status.isGranted,
                        onRequest = { state.launchPermissionRequest() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // 通知权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionState?.let { state ->
                    PermissionGuideCard(
                        title = "通知权限",
                        description = "用于每日位置记录提醒",
                        isGranted = state.status.isGranted,
                        onRequest = { state.launchPermissionRequest() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 测试通知
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.test_notification),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.test_notification_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            // 根据当前位置判断是在公司还是家里
                            val isInOffice = if (currentLat != 0.0 && currentLon != 0.0) {
                                locationManager.isWithinOfficeRadius(currentLat, currentLon)
                            } else {
                                // 如果还没有获取到位置，默认显示公司通知
                                true
                            }
                            
                            val notificationTitle = context.getString(R.string.notification_title_location_updated)
                            val notificationMessage = if (isInOffice) {
                                context.getString(R.string.notification_message_wfo_updated)
                            } else {
                                context.getString(R.string.notification_message_wfh_updated)
                            }
                            
                            NotificationHelper.showAttendanceNotification(
                                context,
                                LocalDate.now(),
                                notificationTitle,
                                notificationMessage
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.send_test_notification))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 应用名称
                    Text(
                        text = context.applicationInfo.loadLabel(context.packageManager).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    // 版本号
                    Text(
                        text = "版本 ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 应用描述
                    Text(
                        text = "办公位置自动识别和记录助手",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 目标位置信息
                    Text(
                        text = "目标: ${NativeLocationManager.OFFICE_LATITUDE}°N, ${NativeLocationManager.OFFICE_LONGITUDE}°E\n范围: 800米",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 作者信息
                    Text(
                        text = "作者: Stephen An",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
