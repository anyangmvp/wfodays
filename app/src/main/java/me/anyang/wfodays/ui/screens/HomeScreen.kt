package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
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
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo 动画
                        val scale by rememberInfiniteTransition(label = "logo").animateFloat(
                            initialValue = 1f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "logo_scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(scale)
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryBlue, PrimaryBlueLight)
                                    ),
                                    RoundedCornerShape(12.dp)
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
                            color = Color(0xFF1E293B)
                        )
                    }
                },
                actions = {
                    // 语言切换图标
                    val context = LocalContext.current
                    
                    IconButton(onClick = {
                        LanguageManager.toggleLanguage(context)
                        // 重启Activity以应用语言更改 - 使用Intent方式避免生命周期异常
                        val activity = context as? android.app.Activity
                        activity?.let {
                            val intent = it.intent
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            it.finish()
                            it.startActivity(intent)
                            // 禁用动画使切换更平滑
                            it.overridePendingTransition(0, 0)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = PrimaryBlue
                        )
                    }
                    
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = stringResource(R.string.statistics),
                            tint = PrimaryBlue
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            val scale by rememberInfiniteTransition(label = "fab").animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "fab_pulse"
            )

            FloatingActionButton(
                onClick = onNavigateToCalendar,
                containerColor = PrimaryBlue,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.scale(scale)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = stringResource(R.string.title_calendar),
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 问候语
            GreetingSection()

            Spacer(modifier = Modifier.height(16.dp))

            // 今日状态卡片 - 渐变背景
            TodayStatusCard(
                todayMode = uiState.todayRecord?.workMode,
                onToggle = {
                    viewModel.toggleTodayStatus()
                    scope.launch {
                        delay(300)
                        showSuccessAnimation = true
                        delay(1500)
                        showSuccessAnimation = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 本月目标进度 - 商务卡片
            val stats = uiState.currentMonthStats
            if (stats != null) {
                BusinessInfoCard(
                    title = stringResource(R.string.monthly_attendance_goal),
                    icon = Icons.Default.Flag,
                    delayMillis = 200
                ) {
                    val progress = if (stats.requiredDays > 0) {
                        stats.wfoDays.toFloat() / stats.requiredDays
                    } else 0f

                    AnimatedProgressBar(
                        progress = progress,
                        isSuccess = progress >= 1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BusinessStatItem(
                            value = stats.wfoDays,
                            label = stringResource(R.string.wfo_completed),
                            icon = Icons.Default.CheckCircle,
                            delayMillis = 300
                        )
                        BusinessStatItem(
                            value = stats.remainingDays,
                            label = stringResource(R.string.wfo_needed),
                            icon = Icons.Default.TrendingUp,
                            delayMillis = 400
                        )
                        BusinessStatItem(
                            value = stats.remainingDays,
                            label = stringResource(R.string.remaining_workdays),
                            icon = Icons.Default.CalendarToday,
                            delayMillis = 500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 快捷操作
            QuickActionsSection(
                onWFOClick = {
                    viewModel.manualCheckIn()
                    scope.launch {
                        delay(300)
                        showSuccessAnimation = true
                        delay(1500)
                        showSuccessAnimation = false
                    }
                },
                onWFHClick = {
                    viewModel.markAsWFH()
                    scope.launch {
                        delay(300)
                        showSuccessAnimation = true
                        delay(1500)
                        showSuccessAnimation = false
                    }
                },
                onLeaveClick = {
                    viewModel.markAsLeave()
                    scope.launch {
                        delay(300)
                        showSuccessAnimation = true
                        delay(1500)
                        showSuccessAnimation = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 成功动画
    AnimatedVisibility(
        visible = showSuccessAnimation,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        SuccessOverlay(onDismiss = { showSuccessAnimation = false })
    }
}

@Composable
private fun TodayStatusCard(
    todayMode: WorkMode?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundBrush, icon, title, subtitle) = when (todayMode) {
        WorkMode.WFO -> Quad(
            Brush.linearGradient(listOf(PrimaryBlueDark, PrimaryBlue, PrimaryBlueLight)),
            Icons.Default.Business,
            stringResource(R.string.today_work),
            stringResource(R.string.office_wfo)
        )
        WorkMode.WFH -> Quad(
            Brush.linearGradient(listOf(SuccessGreen, SuccessGreen.copy(green = 0.7f))),
            Icons.Default.HomeWork,
            stringResource(R.string.today_work),
            stringResource(R.string.home_wfh)
        )
        WorkMode.LEAVE -> Quad(
            Brush.linearGradient(listOf(WarningYellow, WarningYellow.copy(red = 0.9f))),
            Icons.Default.BeachAccess,
            stringResource(R.string.today_leave),
            stringResource(R.string.enjoy_holiday)
        )
        null -> Quad(
            Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF94A3B8))),
            Icons.Default.Help,
            stringResource(R.string.today_not_recorded),
            stringResource(R.string.click_to_set_status)
        )
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = when (todayMode) {
                    WorkMode.WFO -> PrimaryBlue.copy(alpha = 0.4f)
                    WorkMode.WFH -> SuccessGreen.copy(alpha = 0.4f)
                    WorkMode.LEAVE -> WarningYellow.copy(alpha = 0.4f)
                    null -> Color.Gray.copy(alpha = 0.3f)
                }
            )
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .clickable {
                isPressed = true
                onToggle()
            }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

            }

            // 图标动画
            val infiniteScale by rememberInfiniteTransition(label = "icon").animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "icon_scale"
            )

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(infiniteScale)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun GreetingSection() {
    var greeting by remember { mutableStateOf(GreetingHelper.getGreeting()) }
    val timePeriodKey = GreetingHelper.getTimePeriod()
    val timePeriod = GreetingHelper.getLocalizedTimePeriod()

    // 每次重新组合时更新问候语（如果需要）
    LaunchedEffect(Unit) {
        greeting = GreetingHelper.getGreeting()
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
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间段图标
            val timeIcon = when (timePeriodKey) {
                "morning" -> Icons.Default.WbSunny
                "noon" -> Icons.Default.BrightnessHigh
                "afternoon" -> Icons.Default.Brightness5
                "evening" -> Icons.Default.NightsStay
                "midnight" -> Icons.Default.Bedtime
                "weekend" -> Icons.Default.Weekend
                else -> Icons.Default.WbSunny
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryBlue.copy(alpha = 0.2f), PrimaryBlueLight.copy(alpha = 0.1f))
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = timeIcon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = timePeriod,
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BusinessInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .alpha(animatedAlpha)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
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
        isSuccess -> SuccessGreen
        progress >= 0.7f -> PrimaryBlue
        else -> WarningYellow
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(12.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(progressColor, progressColor.copy(alpha = 0.8f))
                        ),
                        RoundedCornerShape(6.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = progressColor
        )
    }
}

@Composable
private fun BusinessStatItem(
    value: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                .size(48.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlueDark
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
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
        Text(
            text = stringResource(R.string.quick_record),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                text = "WFO",
                icon = Icons.Default.Business,
                color = PrimaryBlue,
                onClick = onWFOClick,
                modifier = Modifier.weight(1f),
                delayMillis = 700
            )
            QuickActionButton(
                text = "WFH",
                icon = Icons.Default.HomeWork,
                color = SuccessGreen,
                onClick = onWFHClick,
                modifier = Modifier.weight(1f),
                delayMillis = 800
            )
            QuickActionButton(
                text = stringResource(R.string.leave),
                icon = Icons.Default.BeachAccess,
                color = WarningYellow,
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
    color: Color,
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
            isPressed -> 0.95f
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
            .clickable {
                isPressed = true
                onClick()
                scope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun SuccessOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            label = "success_scale"
        )

        Card(
            modifier = Modifier
                .scale(scale)
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 成功动画圆圈
                val infiniteScale by rememberInfiniteTransition(label = "success").animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "success_pulse"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(infiniteScale)
                        .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.record_success),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

// 辅助数据类
private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// 缓动函数
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
