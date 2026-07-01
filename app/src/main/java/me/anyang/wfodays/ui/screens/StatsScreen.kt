package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import me.anyang.wfodays.R
import me.anyang.wfodays.data.repository.MonthlyStatistics
import me.anyang.wfodays.ui.components.DonutChart
import me.anyang.wfodays.ui.components.MultiSegmentDonutChart
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.StatsViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var selectedMonthStats by remember { mutableStateOf<MonthlyStatistics?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
        delay(100)
        isVisible = true
    }

    // Update selected month when data loads
    LaunchedEffect(uiState.currentMonthStats) {
        if (selectedMonthStats == null) {
            selectedMonthStats = uiState.currentMonthStats
        }
    }

    val displayStats = selectedMonthStats ?: uiState.currentMonthStats

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trends",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Office Compliance with Donut Chart
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                displayStats?.let { stats ->
                    OfficeComplianceSection(
                        stats = stats,
                        targetPercentage = uiState.targetPercentage
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Trend Chart
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                MonthlyTrendChart(stats = uiState.allStats)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                HistorySection(
                    stats = uiState.allStats,
                    onMonthClick = { stats ->
                        selectedMonthStats = stats
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OfficeComplianceSection(stats: MonthlyStatistics, targetPercentage: Float) {
    val percentage = (stats.currentRate * 100).toFloat()
    val monthName = formatMonth(stats.yearMonth)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Text info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Office Compliance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${percentage.toInt()}",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = monthName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${stats.wfoDays} / ${stats.effectiveWorkdays} days",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target: ${targetPercentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = WarningOrange
            )
        }

        // Right side - Donut chart (empty inside)
        MultiSegmentDonutChart(
            wfoDays = stats.wfoDays,
            wfhDays = stats.wfhDays,
            leaveDays = stats.leaveDays,
            totalDays = stats.effectiveWorkdays,
            size = 120.dp,
            strokeWidth = 14f,
            showText = false
        )
    }
}

@Composable
private fun MonthlyTrendChart(stats: List<MonthlyStatistics>) {
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .padding(12.dp)
    ) {
        Text(
            text = "Monthly Trend",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Simple trend visualization using Canvas
        if (stats.isNotEmpty()) {
            val maxPercentage = 100f

            // Month labels with percentages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.takeLast(6).forEach { stat ->
                    val percentage = (stat.currentRate * 100).toInt()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBlue
                        )
                        Text(
                            text = formatMonthShort(stat.yearMonth),
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                val strokeWidthPx = with(density) { 2.dp.toPx() }
                val paddingPx = with(density) { 4.dp.toPx() }
                val width = size.width - paddingPx * 2
                val height = size.height - paddingPx * 2
                val stepX = if (stats.size > 1) width / (stats.size - 1) else width

                // Draw line chart
                val points = stats.takeLast(6).mapIndexed { index, stat ->
                    val x = paddingPx + index * stepX
                    val percentage = (stat.currentRate * 100).coerceIn(0f, maxPercentage)
                    val y = paddingPx + height - (percentage / maxPercentage * height)
                    Offset(x, y)
                }

                // Draw connecting lines
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = PrimaryBlue,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // Draw dots
                points.forEach { point ->
                    drawCircle(
                        color = PrimaryBlue,
                        radius = with(density) { 4.dp.toPx() },
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = with(density) { 2.dp.toPx() },
                        center = point
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(
    stats: List<MonthlyStatistics>,
    onMonthClick: (MonthlyStatistics) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (stats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400
                )
            }
        } else {
            stats.reversed().forEach { monthStats ->
                HistoryItem(
                    stats = monthStats,
                    onClick = { onMonthClick(monthStats) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistoryItem(
    stats: MonthlyStatistics,
    onClick: () -> Unit = {}
) {
    val percentage = (stats.currentRate * 100).toInt()
    val isGoalReached = stats.remainingDays <= 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundLight)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatMonth(stats.yearMonth),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$percentage%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGoalReached) SuccessGreen else PrimaryBlue
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${stats.wfoDays} / ${stats.effectiveWorkdays}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = if (isGoalReached) SuccessGreen else Gray400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatMonth(yearMonth: YearMonth): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${monthNames[yearMonth.monthValue - 1]} ${yearMonth.year}"
}

private fun formatMonthShort(yearMonth: YearMonth): String {
    val monthAbbrevs = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return monthAbbrevs[yearMonth.monthValue - 1]
}
