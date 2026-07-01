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
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
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
                        text = stringResource(R.string.wfo_days),
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
                        remainingWfoDays = stats.remainingDays,
                        remainingWorkdays = stats.remainingWorkdays,
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
    var showStatusDialog by remember { mutableStateOf(false) }

    Column {
        // Date
        Text(
            text = stringResource(R.string.today),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy · EEEE", Locale.ENGLISH)),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Today Status Card - Click to change status
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
                .clickable { showStatusDialog = true }
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.today_status),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = todayRecord?.workMode?.name ?: stringResource(R.string.no_status),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (todayRecord != null) TextPrimary else Gray400
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (todayRecord != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.tap_to_change),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.tap_to_set_status),
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
                    null -> Gray300
                }
                val icon = when (todayRecord?.workMode) {
                    WorkMode.WFO -> Icons.Default.Business
                    WorkMode.WFH -> Icons.Default.HomeWork
                    WorkMode.LEAVE -> Icons.Default.BeachAccess
                    null -> Icons.Default.Business
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Status Selection Dialog - iOS Style
        if (showStatusDialog) {
            Dialog(onDismissRequest = { showStatusDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundWhite)
                ) {
                    Column {
                        // Title
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.change_status),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.select_status_for_today),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // Options
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            // WFO option
                            DialogOption(
                                label = stringResource(R.string.wfo),
                                subtitle = stringResource(R.string.work_from_office),
                                icon = Icons.Default.Business,
                                color = PrimaryBlue,
                                onClick = {
                                    onWFOClick()
                                    showStatusDialog = false
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(Gray200)
                            )

                            // WFH option
                            DialogOption(
                                label = stringResource(R.string.wfh),
                                subtitle = stringResource(R.string.work_from_home),
                                icon = Icons.Default.HomeWork,
                                color = SuccessGreen,
                                onClick = {
                                    onWFHClick()
                                    showStatusDialog = false
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(Gray200)
                            )

                            // Leave option
                            DialogOption(
                                label = stringResource(R.string.leave),
                                subtitle = stringResource(R.string.on_leave),
                                icon = Icons.Default.BeachAccess,
                                color = WarningOrange,
                                onClick = {
                                    onLeaveClick()
                                    showStatusDialog = false
                                }
                            )
                        }

                        // Cancel button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(Gray200)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStatusDialog = false }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogOption(
    label: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
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
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .padding(12.dp)
    ) {
        // Title row with info icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.monthly_office_compliance),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.info),
                tint = Gray400,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onInfoClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                size = 140.dp,
                strokeWidth = 14f,
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
            label = stringResource(R.string.wfo_days),
            icon = Icons.Default.Business,
            color = PrimaryBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = wfhDays,
            label = stringResource(R.string.wfh_days),
            icon = Icons.Default.HomeWork,
            color = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = remainingDays,
            label = stringResource(R.string.remaining_days_label),
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
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            fontSize = 18.sp,
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
    remainingWfoDays: Int,
    remainingWorkdays: Int,
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
                text = stringResource(R.string.insight),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (remainingWfoDays > 0) {
                pluralStringResource(
                    R.plurals.wfo_days_needed_format,
                    remainingWfoDays,
                    remainingWfoDays,
                    targetPercentage.toInt()
                )
            } else {
                stringResource(R.string.goal_reached_office_target)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
        )

        if (remainingWfoDays > 0 && remainingWorkdays > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.working_days_left_format, remainingWorkdays),
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
    }
}
