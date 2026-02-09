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
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = PrimaryBlue.copy(alpha = 0.4f)
                    )
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlueDark, PrimaryBlue, PrimaryBlueLight)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
            LanguageSelectorCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        // 开始按钮
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
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = if (locationPermissionState.status.isGranted) {
                                PrimaryBlue.copy(alpha = 0.4f)
                            } else Color.Gray.copy(alpha = 0.2f)
                        ),
                    enabled = locationPermissionState.status.isGranted,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.start_using_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarningYellow.copy(alpha = 0.1f))
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
                                tint = WarningYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.location_permission_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = WarningYellow
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
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            // 标题
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
                            color = PrimaryBlueDark
                        )
            }

            // 功能项
            FeatureItem(
                icon = Icons.Default.LocationOn,
                iconColor = PrimaryBlue,
                title = stringResource(R.string.auto_detection_title),
                description = stringResource(R.string.auto_detection_desc, NativeLocationManager.OFFICE_NAME)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.EditCalendar,
                iconColor = SuccessGreen,
                title = stringResource(R.string.manual_recording_title),
                description = stringResource(R.string.manual_recording_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureItem(
                icon = Icons.Default.Notifications,
                iconColor = WarningYellow,
                title = stringResource(R.string.daily_reminder_title),
                description = stringResource(R.string.daily_reminder_desc)
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = iconColor.copy(alpha = 0.3f)
                )
                .background(iconColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlueDark
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

/**
 * 语言选择卡片组件
 * 允许用户在引导页面选择应用语言
 */
@Composable
private fun LanguageSelectorCard() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    // 使用 remember 保存当前语言状态，从 SharedPreferences 读取
    var currentLanguage by remember { 
        mutableStateOf(LanguageManager.getCurrentLanguage(context)) 
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.language_setting_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlueDark
                )
            }

            // 语言选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 中文选项
                LanguageOptionButton(
                    languageCode = LanguageManager.LANG_ZH,
                    displayName = stringResource(R.string.chinese_language),
                    isSelected = currentLanguage == LanguageManager.LANG_ZH,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (currentLanguage != LanguageManager.LANG_ZH) {
                            // 先保存语言设置
                            LanguageManager.setLanguage(context, LanguageManager.LANG_ZH)
                            // 更新本地状态
                            currentLanguage = LanguageManager.LANG_ZH
                            // 重启 Activity 应用更改
                            activity?.let {
                                LanguageManager.restartActivitySafely(it)
                            }
                        }
                    }
                )

                // 英文选项
                LanguageOptionButton(
                    languageCode = LanguageManager.LANG_EN,
                    displayName = "English",
                    isSelected = currentLanguage == LanguageManager.LANG_EN,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (currentLanguage != LanguageManager.LANG_EN) {
                            // 先保存语言设置
                            LanguageManager.setLanguage(context, LanguageManager.LANG_EN)
                            // 更新本地状态
                            currentLanguage = LanguageManager.LANG_EN
                            // 重启 Activity 应用更改
                            activity?.let {
                                LanguageManager.restartActivitySafely(it)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * 语言选项按钮
 */
@Composable
private fun LanguageOptionButton(
    languageCode: String,
    displayName: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryBlue else PrimaryBlue.copy(alpha = 0.1f)
    val textColor = if (isSelected) Color.White else PrimaryBlueDark

    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        elevation = if (isSelected) {
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        } else {
            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        }
    ) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
