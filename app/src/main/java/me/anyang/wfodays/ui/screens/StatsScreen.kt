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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import me.anyang.wfodays.data.repository.MonthlyStatistics
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.StatsViewModel
import androidx.compose.ui.res.stringResource
import me.anyang.wfodays.R
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.attendance_statistics),
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
            // 当前月统计卡片 - 带渐变和动画
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                uiState.currentMonthStats?.let { stats ->
                    CurrentMonthStatsCard(stats = stats)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 历史统计标题
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                    text = stringResource(R.string.history),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = PrimaryBlueDark
                )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 历史统计列表
            if (uiState.allStats.isEmpty()) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(700))
                ) {
                    EmptyHistoryCard()
                }
            } else {
                uiState.allStats.reversed().forEachIndexed { index, stats ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(700 + index * 100)) + slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    ) {
                        HistoryStatsItem(stats = stats, index = index)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CurrentMonthStatsCard(stats: MonthlyStatistics) {
    val progress = if (stats.requiredDays > 0) {
        (stats.wfoDays.toFloat() / stats.requiredDays).coerceIn(0f, 1.2f)
    } else 0f

    val isGoalReached = stats.remainingDays <= 0

    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        cardVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (cardVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "card_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isGoalReached) SuccessGreen.copy(alpha = 0.3f)
                else PrimaryBlue.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isGoalReached) {
                        listOf(SuccessGreen, SuccessGreen.copy(green = 0.8f))
                    } else {
                        listOf(PrimaryBlueDark, PrimaryBlue, PrimaryBlueLight)
                    }
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.monthly_wfo_statistics),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            append(stats.yearMonth.year)
                            append(stringResource(R.string.year_format, 2024).replace("2024", ""))
                            append(getMonthName(stats.yearMonth.monthValue))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // 完成状态图标
                if (isGoalReached) {
                    val infiniteScale by rememberInfiniteTransition(label = "check").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "check_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(infiniteScale)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 计算公式
            Text(
                text = stringResource(
                    R.string.goal_formula,
                    stats.totalWorkdays,
                    stats.leaveDays,
                    stats.requiredDays
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 环形进度条
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier.size(140.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.wfoDays}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "/ ${stats.requiredDays} ${stringResource(R.string.days_unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(stats.currentRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 统计详情网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsGridItem(
                    value = stats.totalWorkdays.toString(),
                    label = stringResource(R.string.total_workdays_label),
                    icon = Icons.Default.CalendarToday,
                    delayMillis = 300
                )
                StatsGridItem(
                    value = stats.leaveDays.toString(),
                    label = stringResource(R.string.leave_days_label),
                    icon = Icons.Default.BeachAccess,
                    delayMillis = 400
                )
                StatsGridItem(
                    value = stats.effectiveWorkdays.toString(),
                    label = stringResource(R.string.effective_workdays),
                    icon = Icons.Default.Work,
                    delayMillis = 500
                )
                StatsGridItem(
                    value = if (stats.remainingDays > 0) "${stats.remainingDays}" else stringResource(R.string.completed),
                    label = if (stats.remainingDays > 0) stringResource(R.string.remaining_days_label) else stringResource(R.string.status),
                    icon = if (stats.remainingDays > 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.CheckCircle,
                    delayMillis = 600,
                    isSuccess = isGoalReached
                )
            }

            // 目标达成提示
            if (isGoalReached) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = stringResource(R.string.congrats_goal_reached),
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGridItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMillis: Int = 0,
    isSuccess: Boolean = false
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "item_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .alpha(animatedAlpha)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
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
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F5F9))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                    text = stringResource(R.string.no_history_data),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
        }
    }
}

@Composable
private fun HistoryStatsItem(stats: MonthlyStatistics, index: Int) {
    val monthStr = buildString {
        append(stats.yearMonth.year)
        append(stringResource(R.string.year_format, 2024).replace("2024", ""))
        append(getMonthName(stats.yearMonth.monthValue))
    }
    val progress = stats.wfoDays.toFloat() / stats.effectiveWorkdays.coerceAtLeast(1)
    val isGoalReached = stats.remainingDays <= 0

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200 + index * 100L)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "history_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "history_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isGoalReached) SuccessGreen.copy(alpha = 0.2f)
                else PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 月份图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isGoalReached) SuccessGreen.copy(alpha = 0.1f)
                            else PrimaryBlue.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (isGoalReached) SuccessGreen else PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = monthStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.wfo_stats_format, stats.wfoDays, stats.effectiveWorkdays, (progress * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // 状态指示
            if (isGoalReached) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SuccessGreen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(WarningYellow.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.days_remaining_format, stats.remainingDays),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarningYellow,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// 缓动函数
private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

@Composable
private fun getMonthName(monthValue: Int): String {
    return when (monthValue) {
        1 -> stringResource(R.string.month_abbr_jan)
        2 -> stringResource(R.string.month_abbr_feb)
        3 -> stringResource(R.string.month_abbr_mar)
        4 -> stringResource(R.string.month_abbr_apr)
        5 -> stringResource(R.string.month_abbr_may)
        6 -> stringResource(R.string.month_abbr_jun)
        7 -> stringResource(R.string.month_abbr_jul)
        8 -> stringResource(R.string.month_abbr_aug)
        9 -> stringResource(R.string.month_abbr_sep)
        10 -> stringResource(R.string.month_abbr_oct)
        11 -> stringResource(R.string.month_abbr_nov)
        12 -> stringResource(R.string.month_abbr_dec)
        else -> monthValue.toString()
    }
}
