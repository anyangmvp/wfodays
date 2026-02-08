package me.anyang.wfodays.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.DailyCheckScheduler
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.ui.components.PermissionGuideCard
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.SuccessGreen
import android.Manifest
import android.os.Build
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    preferencesManager: PreferencesManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 权限状态
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null
    
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(Unit) {
        // 启动每日定时检查（10:30）
        DailyCheckScheduler.scheduleDailyCheck(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // 欢迎标题
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = HSBCRed
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "欢迎使用 WFODays",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "${NativeLocationManager.OFFICE_NAME}办公位置记录助手",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 功能说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "主要功能",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                FeatureItem(
                    icon = Icons.Default.LocationOn,
                    text = "自动检测：进入${NativeLocationManager.OFFICE_NAME}800米范围自动记录WFO"
                )
                FeatureItem(
                    icon = Icons.Default.CheckCircle,
                    text = "手动记录：支持任意日期手动补录WFO/WFH/休假"
                )
                FeatureItem(
                    icon = Icons.Default.Notifications,
                    text = "每日提醒：上午10点位置记录提醒"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 权限申请
        Text(
            text = "需要以下权限",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 开始按钮
        Button(
            onClick = {
                scope.launch {
                    preferencesManager.setFirstLaunchComplete()
                    onNavigateToHome()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = locationPermissionState.status.isGranted
        ) {
            Text(
                text = "开始使用",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (!locationPermissionState.status.isGranted) {
            Text(
                text = "请先授权位置权限",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SuccessGreen,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
