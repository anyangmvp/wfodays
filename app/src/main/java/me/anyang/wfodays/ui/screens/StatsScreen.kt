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
        containerColor = JoyBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.attendance_statistics),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JoyPurple,
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
                            JoyPurple.copy(alpha = 0.08f),
                            JoyBackground,
                            JoyCardAccent2.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
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
                            .size(42.dp)
                            .background(
                                Brush.linearGradient(listOf(JoyCoral, JoyPeach)),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp),
                        color = JoyOnBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

    val gradientColors = if (isGoalReached) {
        listOf(JoyMint, JoyMintDark, JoyTeal)
    } else {
        listOf(JoyPurple, JoyViolet, JoyLavender)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isGoalReached) JoyMint.copy(alpha = 0.3f)
                else JoyPurple.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(gradientColors)
            )
            .padding(24.dp)
    ) {
        Column {
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
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                if (isGoalReached) {
                    val infiniteScale by rememberInfiniteTransition(label = "check").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
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
                            .background(Color.White.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(
                    R.string.goal_formula,
                    stats.totalWorkdays,
                    stats.leaveDays,
                    stats.requiredDays
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress.coerceAtMost(1f) },
                    modifier = Modifier.size(140.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
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
                        color = Color.White.copy(alpha = 0.85f)
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

            if (isGoalReached) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Celebration,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.congrats_goal_reached),
                            modifier = Modifier.padding(start = 10.dp),
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
                    Color.White.copy(alpha = 0.25f),
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
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun EmptyHistoryCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = JoyGray300.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        JoyCardAccent1.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = JoyOrange,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.no_history_data),
                style = MaterialTheme.typography.bodyLarge,
                color = JoyOnSurfaceVariant
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

    val gradientColors = if (isGoalReached) {
        listOf(JoyMint, JoyMintLight)
    } else {
        listOf(JoyOrange, JoyCoral)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isGoalReached) JoyMint.copy(alpha = 0.2f)
                else JoyOrange.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = gradientColors.first().copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = monthStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = JoyOnBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.wfo_stats_format, stats.wfoDays, stats.effectiveWorkdays, (progress * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyOnSurfaceVariant
                    )
                }
            }

            if (isGoalReached) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(JoyMint.copy(alpha = 0.3f), JoyMint.copy(alpha = 0.1f))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = JoyMint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(listOf(LeaveYellow, LeaveYellowLight))
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.days_remaining_format, stats.remainingDays),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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
