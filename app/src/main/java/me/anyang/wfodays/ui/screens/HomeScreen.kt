package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.HomeViewModel
import me.anyang.wfodays.utils.GreetingHelper
import me.anyang.wfodays.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var triggerAutoLocateAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.todayRecord) {
        uiState.todayRecord?.let { record ->
            if (record.recordType == RecordType.AUTO) {
                triggerAutoLocateAnimation = true
            }
        }
    }

    Scaffold(
        containerColor = JoyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val infiniteTransition = rememberInfiniteTransition(label = "logo")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.08f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "logo_scale"
                        )
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "logo_rotation"
                        )
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(scale)
                                .rotate(rotation)
                                .background(
                                    Brush.linearGradient(JoyGradientPrimary),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "WFODays",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = JoyOnBackground
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    
                    IconButton(onClick = {
                        LanguageManager.toggleLanguage(context)
                        val activity = context as? android.app.Activity
                        activity?.let {
                            val intent = it.intent
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            it.finish()
                            it.startActivity(intent)
                            @Suppress("DEPRECATION")
                            it.overridePendingTransition(0, 0)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = JoyOrange
                        )
                    }
                    
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.title_calendar),
                            tint = JoyOrange
                        )
                    }
                    
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = stringResource(R.string.statistics),
                            tint = JoyOrange
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = JoyOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
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
                            JoyBackground,
                            JoyBackgroundLight,
                            JoyCardAccent1.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            GreetingSection()

            Spacer(modifier = Modifier.height(20.dp))

            TodayStatusCard(
                todayMode = uiState.todayRecord?.workMode,
                recordType = uiState.todayRecord?.recordType,
                isAutoLocateSuccess = triggerAutoLocateAnimation,
                onAnimationEnd = { triggerAutoLocateAnimation = false },
                onLongPress = {
                    scope.launch {
                        val success = viewModel.autoDetectAndRecordByLocation()
                        if (success) {
                            triggerAutoLocateAnimation = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            val stats = uiState.currentMonthStats
            if (stats != null) {
                MonthlyProgressCard(
                    stats = stats,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            QuickActionsSection(
                onWFOClick = { viewModel.manualCheckIn() },
                onWFHClick = { viewModel.markAsWFH() },
                onLeaveClick = { viewModel.markAsLeave() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TodayStatusCard(
    todayMode: WorkMode?,
    recordType: RecordType?,
    isAutoLocateSuccess: Boolean = false,
    onAnimationEnd: () -> Unit = {},
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundBrush, icon, title, subtitle, accentColor) = when (todayMode) {
        WorkMode.WFO -> Quintuple(
            Brush.linearGradient(JoyGradientWFO),
            Icons.Default.Business,
            stringResource(R.string.today_work),
            stringResource(R.string.office_wfo),
            WFOOrange
        )
        WorkMode.WFH -> Quintuple(
            Brush.linearGradient(JoyGradientWFH),
            Icons.Default.HomeWork,
            stringResource(R.string.today_work),
            stringResource(R.string.home_wfh),
            WFHMint
        )
        WorkMode.LEAVE -> Quintuple(
            Brush.linearGradient(JoyGradientLeave),
            Icons.Default.BeachAccess,
            stringResource(R.string.today_leave),
            stringResource(R.string.enjoy_holiday),
            LeaveYellow
        )
        null -> Quintuple(
            Brush.linearGradient(listOf(JoyGray400, JoyGray300)),
            Icons.AutoMirrored.Filled.Help,
            stringResource(R.string.today_not_recorded),
            stringResource(R.string.double_tap_to_auto_locate),
            JoyGray500
        )
    }

    var isPressed by remember { mutableStateOf(false) }
    var playSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isAutoLocateSuccess) {
        if (isAutoLocateSuccess) {
            playSuccessAnimation = true
            delay(2000)
            playSuccessAnimation = false
            onAnimationEnd()
        }
    }

    val cardScale by animateFloatAsState(
        targetValue = when {
            playSuccessAnimation -> 1.03f
            isPressed -> 0.97f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (playSuccessAnimation) 0.6f else 0f,
        animationSpec = tween(300, easing = EaseOutQuad),
        label = "glow_alpha"
    )

    val rotation by animateFloatAsState(
        targetValue = if (playSuccessAnimation) 360f else 0f,
        animationSpec = tween(800, easing = EaseOutBack),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .scale(cardScale)
            .shadow(
                elevation = if (playSuccessAnimation) 24.dp else 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = when {
                    playSuccessAnimation -> Color.White.copy(alpha = 0.5f)
                    todayMode == WorkMode.WFO -> JoyShadowOrange
                    todayMode == WorkMode.WFH -> JoyShadowMint
                    todayMode == WorkMode.LEAVE -> JoyShadowYellow
                    else -> JoyGray400.copy(alpha = 0.2f)
                }
            )
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundBrush)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onDoubleTap = { onLongPress() }
                )
            }
            .padding(28.dp)
    ) {
        if (playSuccessAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = glowAlpha),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val infiniteScale by rememberInfiniteTransition(label = "icon").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "icon_scale"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(if (playSuccessAnimation) 1.3f else infiniteScale)
                        .rotate(if (playSuccessAnimation) rotation else 0f)
                        .background(
                            Color.White.copy(alpha = if (playSuccessAnimation) 0.4f else 0.25f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playSuccessAnimation) Icons.Default.CheckCircle else icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (todayMode != null && recordType != null) {
                Spacer(modifier = Modifier.height(16.dp))
                RecordTypeBadge(
                    recordType = recordType,
                    workMode = todayMode,
                    playAnimation = playSuccessAnimation
                )
            }
        }
    }
}

@Composable
private fun RecordTypeBadge(
    recordType: RecordType,
    workMode: WorkMode,
    playAnimation: Boolean = false
) {
    val (icon, text, baseBackgroundColor) = when (workMode) {
        WorkMode.WFO -> when (recordType) {
            RecordType.AUTO -> Triple(
                Icons.Default.LocationOn,
                stringResource(R.string.auto_located),
                Color(0xFFC44D20).copy(alpha = 0.6f)
            )
            RecordType.MANUAL -> Triple(
                Icons.Default.TouchApp,
                stringResource(R.string.manually_recorded),
                WFOOrange.copy(alpha = 0.5f)
            )
        }
        WorkMode.WFH -> when (recordType) {
            RecordType.AUTO -> Triple(
                Icons.Default.LocationOn,
                stringResource(R.string.auto_located),
                Color(0xFF0D9488).copy(alpha = 0.6f)
            )
            RecordType.MANUAL -> Triple(
                Icons.Default.TouchApp,
                stringResource(R.string.manually_recorded),
                WFHMint.copy(alpha = 0.5f)
            )
        }
        WorkMode.LEAVE -> when (recordType) {
            RecordType.AUTO -> Triple(
                Icons.Default.LocationOn,
                stringResource(R.string.auto_located),
                Color(0xFFD97706).copy(alpha = 0.6f)
            )
            RecordType.MANUAL -> Triple(
                Icons.Default.TouchApp,
                stringResource(R.string.manually_recorded),
                LeaveYellow.copy(alpha = 0.5f)
            )
        }
    }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (playAnimation && recordType == RecordType.AUTO) {
            when (workMode) {
                WorkMode.WFO -> JoyCoral.copy(alpha = 0.8f)
                WorkMode.WFH -> JoyMintLight.copy(alpha = 0.8f)
                WorkMode.LEAVE -> LeaveYellowLight.copy(alpha = 0.8f)
            }
        } else {
            baseBackgroundColor
        },
        animationSpec = tween(300),
        label = "badge_bg_color"
    )

    val badgeScale by animateFloatAsState(
        targetValue = if (playAnimation) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "badge_scale"
    )

    Box(
        modifier = Modifier
            .wrapContentSize()
            .scale(badgeScale)
            .background(
                color = animatedBackgroundColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (playAnimation && recordType == RecordType.AUTO) {
                    Icons.Default.CheckCircle
                } else {
                    icon
                },
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (playAnimation && recordType == RecordType.AUTO) {
                    stringResource(R.string.location_success)
                } else {
                    text
                },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.95f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GreetingSection() {
    val context = LocalContext.current
    val currentLanguage = remember { LanguageManager.getCurrentLanguage(context) }
    var greeting by remember { mutableStateOf(GreetingHelper.getGreeting(currentLanguage)) }
    val timePeriodKey = GreetingHelper.getTimePeriod()
    val timePeriod = GreetingHelper.getLocalizedTimePeriod()

    LaunchedEffect(Unit) {
        greeting = GreetingHelper.getGreeting(currentLanguage)
    }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "greeting_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "greeting_alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = JoyOrange.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            JoyCardAccent1.copy(alpha = 0.3f),
                            JoyCardAccent4.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val timeIcon = when (timePeriodKey) {
                "morning" -> Icons.Default.WbSunny
                "noon" -> Icons.Default.BrightnessHigh
                "afternoon" -> Icons.Default.Brightness5
                "evening" -> Icons.Default.NightsStay
                "midnight" -> Icons.Default.Bedtime
                "weekend" -> Icons.Default.Weekend
                else -> Icons.Default.WbSunny
            }

            val iconColor = when (timePeriodKey) {
                "morning" -> JoyOrange
                "noon" -> JoyWarning
                "afternoon" -> JoyCoral
                "evening" -> JoyPurple
                "midnight" -> JoyLavender
                "weekend" -> JoyMint
                else -> JoyOrange
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(iconColor.copy(alpha = 0.2f), iconColor.copy(alpha = 0.1f))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = timeIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = timePeriod,
                    style = MaterialTheme.typography.labelMedium,
                    color = iconColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JoyOnBackground,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MonthlyProgressCard(
    stats: me.anyang.wfodays.data.repository.MonthlyStatistics,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "card_alpha"
    )

    val progress = if (stats.requiredDays > 0) {
        stats.wfoDays.toFloat() / stats.requiredDays
    } else 0f
    val isSuccess = progress >= 1f

    Card(
        modifier = modifier
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isSuccess) JoySuccess.copy(alpha = 0.25f) else JoyOrange.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isSuccess) JoyCardAccent2.copy(alpha = 0.3f) else JoyCardAccent1.copy(alpha = 0.3f),
                            Color.White
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(
                                if (isSuccess) JoyGradientSuccess else JoyGradientPrimary
                            ),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                    ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.monthly_attendance_goal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedProgressBar(
                progress = progress,
                isSuccess = isSuccess,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.wfoDays,
                    label = stringResource(R.string.wfo_completed),
                    icon = Icons.Default.CheckCircle,
                    color = WFOOrange,
                    delayMillis = 300
                )
                StatItem(
                    value = stats.remainingDays,
                    label = stringResource(R.string.wfo_needed),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = JoyMint,
                    delayMillis = 400
                )
                StatItem(
                    value = stats.remainingWorkdays,
                    label = stringResource(R.string.remaining_workdays),
                    icon = Icons.Default.CalendarToday,
                    color = JoyPurple,
                    delayMillis = 500
                )
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress"
    )

    val progressColor = when {
        isSuccess -> JoySuccess
        progress >= 0.7f -> JoyMint
        progress >= 0.4f -> JoyOrange
        else -> JoyWarning
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(JoyGray200, RoundedCornerShape(7.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(14.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(progressColor, progressColor.copy(alpha = 0.8f))
                        ),
                        RoundedCornerShape(7.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            if (isSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = JoySuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.goal_reached),
                        style = MaterialTheme.typography.bodySmall,
                        color = JoySuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    delayMillis: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "stat_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "stat_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .alpha(animatedAlpha)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = JoyOnBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = JoyOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickActionsSection(
    onWFOClick: () -> Unit,
    onWFHClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600)
        isVisible = true
    }

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "actions_offset"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "actions_alpha"
    )

    Column(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .alpha(animatedAlpha)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = JoyOrange,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.quick_record),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = JoyOnBackground
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                text = "WFO",
                icon = Icons.Default.Business,
                gradientColors = JoyGradientWFO,
                shadowColor = JoyShadowOrange,
                onClick = onWFOClick,
                modifier = Modifier.weight(1f),
                delayMillis = 700
            )
            QuickActionButton(
                text = "WFH",
                icon = Icons.Default.HomeWork,
                gradientColors = JoyGradientWFH,
                shadowColor = JoyShadowMint,
                onClick = onWFHClick,
                modifier = Modifier.weight(1f),
                delayMillis = 800
            )
            QuickActionButton(
                text = stringResource(R.string.leave),
                icon = Icons.Default.BeachAccess,
                gradientColors = JoyGradientLeave,
                shadowColor = JoyShadowYellow,
                onClick = onLeaveClick,
                modifier = Modifier.weight(1f),
                delayMillis = 900
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    shadowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isVisible -> 1f
            else -> 0.8f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "button_alpha"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = shadowColor
            )
            .clickable {
                isPressed = true
                onClick()
                scope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradientColors))
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            CircleShape
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// 辅助数据类
private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
