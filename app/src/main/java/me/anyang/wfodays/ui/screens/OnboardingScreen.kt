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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val backgroundLocationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else null

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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
                        JoyOrange.copy(alpha = 0.08f),
                        JoyBackground,
                        JoyCardAccent1.copy(alpha = 0.25f)
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(500)) + scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            val pulseScale by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )

            val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(20000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation_angle"
            )

            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    JoyOrange.copy(alpha = 0.1f),
                                    JoyCoral.copy(alpha = 0.2f),
                                    JoyPeach.copy(alpha = 0.1f),
                                    JoyOrange.copy(alpha = 0.1f)
                                )
                            ),
                            CircleShape
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = JoyOrange.copy(alpha = 0.4f)
                        )
                        .background(
                            Brush.linearGradient(JoyGradientPrimary),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    color = JoyOnBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.welcome_subtitle, NativeLocationManager.OFFICE_NAME),
                    style = MaterialTheme.typography.titleMedium,
                    color = JoyOrange,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(JoyPurple, JoyLavender)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.permissions_needed_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = JoyOnBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(900, delayMillis = 500)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Column {
                PermissionGuideCard(
                    title = stringResource(R.string.location_permission_title),
                    description = stringResource(R.string.location_permission_desc),
                    isGranted = locationPermissionState.status.isGranted,
                    onRequest = { locationPermissionState.launchPermissionRequest() }
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        .height(58.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = if (locationPermissionState.status.isGranted) {
                                JoyOrange.copy(alpha = 0.4f)
                            } else JoyGray300.copy(alpha = 0.2f)
                        ),
                    enabled = locationPermissionState.status.isGranted,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JoyOrange,
                        disabledContainerColor = JoyGray300
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.start_using_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!locationPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(LeaveYellow.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = LeaveYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.location_permission_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = LeaveYellow,
                                fontWeight = FontWeight.Medium
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
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyMint.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(22.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(listOf(JoyMint, JoyTeal)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.main_features_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = JoyOnBackground
                )
            }

            FeatureItem(
                icon = Icons.Default.LocationOn,
                gradientColors = JoyGradientPrimary,
                title = stringResource(R.string.auto_detection_title),
                description = stringResource(R.string.auto_detection_desc, NativeLocationManager.OFFICE_NAME)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeatureItem(
                icon = Icons.Default.EditCalendar,
                gradientColors = JoyGradientWFH,
                title = stringResource(R.string.manual_recording_title),
                description = stringResource(R.string.manual_recording_desc)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FeatureItem(
                icon = Icons.Default.Notifications,
                gradientColors = JoyGradientLeave,
                title = stringResource(R.string.daily_reminder_title),
                description = stringResource(R.string.daily_reminder_desc)
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = gradientColors.first().copy(alpha = 0.35f)
                )
                .background(
                    Brush.linearGradient(gradientColors),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = JoyOnBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun LanguageSelectorCard() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    
    var currentLanguage by remember { 
        mutableStateOf(LanguageManager.getCurrentLanguage(context)) 
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = JoyPurple.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Brush.linearGradient(listOf(JoyPurple, JoyLavender)),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.language_setting_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageOptionButton(
                    languageCode = LanguageManager.LANG_ZH,
                    displayName = stringResource(R.string.chinese_language),
                    isSelected = currentLanguage == LanguageManager.LANG_ZH,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (currentLanguage != LanguageManager.LANG_ZH) {
                            LanguageManager.setLanguage(context, LanguageManager.LANG_ZH)
                            currentLanguage = LanguageManager.LANG_ZH
                            activity?.let {
                                LanguageManager.restartActivitySafely(it)
                            }
                        }
                    }
                )

                LanguageOptionButton(
                    languageCode = LanguageManager.LANG_EN,
                    displayName = "English",
                    isSelected = currentLanguage == LanguageManager.LANG_EN,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (currentLanguage != LanguageManager.LANG_EN) {
                            LanguageManager.setLanguage(context, LanguageManager.LANG_EN)
                            currentLanguage = LanguageManager.LANG_EN
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

@Composable
private fun LanguageOptionButton(
    languageCode: String,
    displayName: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradientColors = if (isSelected) listOf(JoyPurple, JoyLavender) else listOf(JoyGray100, JoyGray50)
    val textColor = if (isSelected) Color.White else JoyOnBackground

    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = JoyPurple.copy(alpha = 0.35f)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = textColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(gradientColors),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
