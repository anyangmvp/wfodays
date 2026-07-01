package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.DailyCheckScheduler
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.ui.components.PermissionGuideCard
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.utils.LanguageManager
import android.Manifest
import android.os.Build
import androidx.compose.ui.res.stringResource
import me.anyang.wfodays.R

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

    // 动画状态
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 启动每日定时检查（10:30）
        DailyCheckScheduler.scheduleDailyCheck(context)
        delay(100)
        isVisible = true
    }

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // 欢迎图标 - 带脉冲动画
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = PrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 欢迎标题
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.welcome_subtitle, NativeLocationManager.OFFICE_NAME),
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 语言选择
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(650, delayMillis = 250)) + slideInVertically(
                initialOffsetY = { 20 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 功能说明卡片
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(700, delayMillis = 300)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            FeatureCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 权限申请标题
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(800, delayMillis = 400))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .background(PrimaryBlue, RoundedCornerShape(2.dp))
                )
                Text(
                            text = stringResource(R.string.permissions_needed_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 12.dp),
                            color = PrimaryBlueDark
                        )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 权限卡片列表
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(900, delayMillis = 500)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column {
                // 位置权限
                PermissionGuideCard(
                    title = stringResource(R.string.location_permission_title),
                    description = stringResource(R.string.location_permission_desc),
                    isGranted = locationPermissionState.status.isGranted,
                    onRequest = { locationPermissionState.launchPermissionRequest() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 后台位置权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationState?.let { state ->
                        PermissionGuideCard(
                            title = stringResource(R.string.background_location_permission_title),
                            description = stringResource(R.string.background_location_permission_desc),
                            isGranted = state.status.isGranted,
                            onRequest = { state.launchPermissionRequest() }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // 通知权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionState?.let { state ->
                        PermissionGuideCard(
                            title = stringResource(R.string.notification_permission_title),
                            description = stringResource(R.string.notification_permission_desc),
                            isGranted = state.status.isGranted,
                            onRequest = { state.launchPermissionRequest() }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(1000, delayMillis = 600)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column {
                Button(
                    onClick = {
                        scope.launch {
                            preferencesManager.setFirstLaunchComplete()
                            onNavigateToHome()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = locationPermissionState.status.isGranted,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = Gray300
                    )
                ) {
                    Text(
                        text = stringResource(R.string.start_using_button),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningOrange.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.location_permission_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningOrange
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FeatureCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .background(PrimaryBlue, RoundedCornerShape(2.dp))
                )
                Text(
                    text = stringResource(R.string.main_features_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = TextPrimary
                )
            }

            // Feature items
            FeatureItem(
                icon = Icons.Default.LocationOn,
                iconColor = PrimaryBlue,
                title = stringResource(R.string.auto_detection_title)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.EditCalendar,
                iconColor = SuccessGreen,
                title = stringResource(R.string.manual_recording_title)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.Notifications,
                iconColor = WarningOrange,
                title = stringResource(R.string.daily_reminder_title)
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
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

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
