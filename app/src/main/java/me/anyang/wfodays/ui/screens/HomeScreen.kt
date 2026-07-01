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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.components.DonutChartWithTarget
import me.anyang.wfodays.ui.components.MultiSegmentDonutChart
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.HomeViewModel
import me.anyang.wfodays.utils.LanguageManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val insightSectionIndex = 4 // Index of the Insight section (0=Today, 1=Compliance, 2=Stats, 3=Spacer, 4=Insight)

    // Load data and trigger auto-location detection
    LaunchedEffect(Unit) {
        viewModel.loadData()
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WFO Days",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Today Section with Manual Check-in
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                TodaySection(
                    todayRecord = uiState.todayRecord,
                    onWFOClick = {
                        scope.launch {
                            viewModel.manualCheckIn()
                        }
                    },
                    onWFHClick = {
                        scope.launch {
                            viewModel.markAsWFH()
                        }
                    },
                    onLeaveClick = {
                        scope.launch {
                            viewModel.markAsLeave()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Office Compliance
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                uiState.currentMonthStats?.let { stats ->
                    MonthlyComplianceCard(
                        wfoDays = stats.wfoDays,
                        wfhDays = stats.wfhDays,
                        leaveDays = stats.leaveDays,
                        totalDays = stats.effectiveWorkdays,
                        onInfoClick = {
                            scope.launch {
                                // Scroll to approximate position of Insight section
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                uiState.currentMonthStats?.let { stats ->
                    StatsRow(
                        wfoDays = stats.wfoDays,
                        wfhDays = stats.wfhDays,
                        remainingDays = stats.remainingDays
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Insight Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                uiState.currentMonthStats?.let { stats ->
                    InsightCard(
                        remainingDays = stats.remainingDays,
                        targetPercentage = uiState.targetPercentage
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TodaySection(
    todayRecord: me.anyang.wfodays.data.entity.AttendanceRecord?,
    onWFOClick: () -> Unit,
    onWFHClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    Column {
        // Date
        Text(
            text = "Today",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy · EEEE", Locale.ENGLISH)),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Today Status Card
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
                text = "Today Status",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayRecord?.workMode?.name ?: "No Record",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (todayRecord != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Checked in",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Status Icon
                val iconColor = when (todayRecord?.workMode) {
                    WorkMode.WFO -> PrimaryBlue
                    WorkMode.WFH -> SuccessGreen
                    WorkMode.LEAVE -> WarningOrange
                    null -> Gray400
                }
                val icon = when (todayRecord?.workMode) {
                    WorkMode.WFO -> Icons.Default.Business
                    WorkMode.WFH -> Icons.Default.HomeWork
                    WorkMode.LEAVE -> Icons.Default.BeachAccess
                    null -> Icons.Default.Business
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Manual Check-in Buttons
            if (todayRecord == null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quick Record",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(
                        label = "WFO",
                        icon = Icons.Default.Business,
                        color = PrimaryBlue,
                        onClick = onWFOClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionChip(
                        label = "WFH",
                        icon = Icons.Default.HomeWork,
                        color = SuccessGreen,
                        onClick = onWFHClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionChip(
                        label = "Leave",
                        icon = Icons.Default.BeachAccess,
                        color = WarningOrange,
                        onClick = onLeaveClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = color,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MonthlyComplianceCard(
    wfoDays: Int,
    wfhDays: Int,
    leaveDays: Int,
    totalDays: Int,
    onInfoClick: () -> Unit = {}
) {
    val percentage = if (totalDays > 0) (wfoDays.toFloat() / totalDays * 100) else 0f

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
        // Title row with info icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Office Compliance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Gray400,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onInfoClick() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Multi-segment donut chart
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MultiSegmentDonutChart(
                wfoDays = wfoDays,
                wfhDays = wfhDays,
                leaveDays = leaveDays,
                totalDays = totalDays,
                size = 180.dp,
                strokeWidth = 18f,
                showText = true
            )
        }
    }
}

@Composable
private fun StatsRow(
    wfoDays: Int,
    wfhDays: Int,
    remainingDays: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = wfoDays,
            label = "WFO Days",
            icon = Icons.Default.Business,
            color = PrimaryBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = wfhDays,
            label = "WFH Days",
            icon = Icons.Default.HomeWork,
            color = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = remainingDays,
            label = "Remaining",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = WarningOrange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun InsightCard(
    remainingDays: Int,
    targetPercentage: Float
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Insight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (remainingDays > 0) {
                "You need $remainingDays more WFO day${if (remainingDays > 1) "s" else ""} to meet your ${targetPercentage.toInt()}% office target."
            } else {
                "Great job! You've met your office target."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
        )

        if (remainingDays > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "7 working days left this month",
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
    }
}
