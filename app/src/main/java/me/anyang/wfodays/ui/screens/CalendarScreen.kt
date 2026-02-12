package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.components.CalendarView
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.stringResource
import me.anyang.wfodays.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var longPressedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showWeekendConfirm by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMonthData(YearMonth.now())
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
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.calendar_record),
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
                    containerColor = JoyOrange,
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
                            JoyOrange.copy(alpha = 0.08f),
                            JoyBackground,
                            JoyCardAccent1.copy(alpha = 0.2f)
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
                MonthNavigationCard(
                    yearMonth = uiState.currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                CalendarView(
                    yearMonth = uiState.currentMonth,
                    records = uiState.monthRecords,
                    onDateClick = { date ->
                        if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                        } else {
                            selectedDate = date
                        }
                    },
                    onDateLongPress = { date ->
                        longPressedDate = date
                        if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                            showWeekendConfirm = true
                        } else {
                            selectedDate = date
                        }
                    },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                LegendCard()
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(900)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                MonthSummaryCard(uiState = uiState)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showWeekendConfirm && longPressedDate != null) {
            WeekendConfirmDialog(
                date = longPressedDate!!,
                onConfirm = {
                    selectedDate = longPressedDate
                    showWeekendConfirm = false
                },
                onDismiss = {
                    showWeekendConfirm = false
                    longPressedDate = null
                }
            )
        }

        selectedDate?.let { date ->
            DateActionDialog(
                date = date,
                record = uiState.monthRecords.find {
                    java.time.Instant.ofEpochMilli(it.date)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate() == date
                },
                onDismiss = { selectedDate = null },
                onMarkWFO = {
                    viewModel.markWorkMode(date, WorkMode.WFO)
                    selectedDate = null
                },
                onMarkWFH = {
                    viewModel.markWorkMode(date, WorkMode.WFH)
                    selectedDate = null
                },
                onMarkLeave = {
                    viewModel.markWorkMode(date, WorkMode.LEAVE)
                    selectedDate = null
                },
                onDelete = {
                    viewModel.deleteRecord(date)
                    selectedDate = null
                }
            )
        }
    }
}

@Composable
private fun MonthNavigationCard(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(yearMonth) {
        isVisible = false
        delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "month_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = tween(300),
        label = "month_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyShadowOrange
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(JoyGradientPrimary)
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.previous_month),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = buildString {
                        append(yearMonth.year)
                        append(stringResource(R.string.year_format, 2024).replace("2024", ""))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = buildString {
                        append(yearMonth.year)
                        append(stringResource(R.string.year_format, 2024).replace("2024", ""))
                        append(getMonthName(yearMonth.monthValue))
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyOrange.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(24.dp)
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
                            Brush.linearGradient(JoyGradientPrimary),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.legend_explanation),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    icon = Icons.Default.Business,
                    gradientColors = JoyGradientWFO,
                    text = stringResource(R.string.wfo),
                    subtext = stringResource(R.string.company_work)
                )
                LegendItem(
                    icon = Icons.Default.HomeWork,
                    gradientColors = JoyGradientWFH,
                    text = stringResource(R.string.wfh),
                    subtext = stringResource(R.string.home_work)
                )
                LegendItem(
                    icon = Icons.Default.BeachAccess,
                    gradientColors = JoyGradientLeave,
                    text = stringResource(R.string.leave),
                    subtext = stringResource(R.string.enjoy_holiday)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(JoyCardAccent1.copy(alpha = 0.3f))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = JoyOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.long_press_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = JoyOrange,
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    text: String,
    subtext: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = gradientColors.first().copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradientColors)),
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
            color = gradientColors.first()
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = JoyOnSurfaceVariant
        )
    }
}

@Composable
private fun MonthSummaryCard(uiState: me.anyang.wfodays.ui.viewmodel.CalendarUiState) {
    val wfoCount = uiState.monthRecords.count { it.workMode == WorkMode.WFO }
    val wfhCount = uiState.monthRecords.count { it.workMode == WorkMode.WFH }
    val leaveCount = uiState.monthRecords.count { it.workMode == WorkMode.LEAVE }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = JoyPurple.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(24.dp)
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
                            Brush.linearGradient(listOf(JoyPurple, JoyLavender)),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Summarize,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.month_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    count = wfoCount,
                    label = "WFO",
                    gradientColors = JoyGradientWFO,
                    icon = Icons.Default.Business
                )
                SummaryStatItem(
                    count = wfhCount,
                    label = "WFH",
                    gradientColors = JoyGradientWFH,
                    icon = Icons.Default.HomeWork
                )
                SummaryStatItem(
                    count = leaveCount,
                    label = stringResource(R.string.leave_days_label),
                    gradientColors = JoyGradientLeave,
                    icon = Icons.Default.BeachAccess
                )
                SummaryStatItem(
                    count = uiState.monthRecords.size,
                    label = stringResource(R.string.total),
                    gradientColors = listOf(JoyGray500, JoyGray400),
                    icon = Icons.Default.CalendarMonth
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    count: Int,
    label: String,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(gradientColors.first().copy(alpha = 0.2f), gradientColors.first().copy(alpha = 0.05f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = gradientColors.first(),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = gradientColors.first()
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = JoyOnSurfaceVariant
        )
    }
}

@Composable
private fun WeekendConfirmDialog(
    date: LocalDate,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(JoyGradientLeave),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.weekend_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
            }
        },
        text = {
            Text(
                text = stringResource(
                    R.string.weekend_confirm_message,
                    buildString {
                        append(date.year)
                        append(stringResource(R.string.year_format, 2024).replace("2024", ""))
                        append(getMonthName(date.monthValue))
                        append(date.dayOfMonth.toString().padStart(2, '0'))
                        if (stringResource(R.string.date_format, 2024, 1, 1).contains("/")) {
                            append(stringResource(R.string.day_format, 1).replace("1", ""))
                        }
                    },
                    getWeekdayName(date.dayOfWeek)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = JoyOnSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JoyOrange
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JoyOnSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DateActionDialog(
    date: LocalDate,
    record: me.anyang.wfodays.data.entity.AttendanceRecord?,
    onDismiss: () -> Unit,
    onMarkWFO: () -> Unit,
    onMarkWFH: () -> Unit,
    onMarkLeave: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = buildString {
                        append(date.year)
                        append(stringResource(R.string.year_format, 2024).replace("2024", ""))
                        append(getMonthName(date.monthValue))
                        append(date.dayOfMonth.toString().padStart(2, '0'))
                        if (stringResource(R.string.date_format, 2024, 1, 1).contains("/")) {
                            append("日")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = JoyOnBackground
                )
                if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Weekend,
                            contentDescription = null,
                            tint = LeaveYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getWeekdayName(date.dayOfWeek),
                            style = MaterialTheme.typography.bodySmall,
                            color = LeaveYellow,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        text = {
            Column {
                if (record != null) {
                    val (label, gradientColors, icon) = when (record.workMode) {
                        WorkMode.WFO -> Triple("WFO", JoyGradientWFO, Icons.Default.Business)
                        WorkMode.WFH -> Triple("WFH", JoyGradientWFH, Icons.Default.HomeWork)
                        WorkMode.LEAVE -> Triple(stringResource(R.string.leave_days_label), JoyGradientLeave, Icons.Default.BeachAccess)
                        else -> Triple(stringResource(R.string.unknown_status), listOf(JoyGray500, JoyGray400), Icons.AutoMirrored.Filled.Help)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(gradientColors))
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.current_status),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    record.note?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = JoyOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.note_prefix, it),
                                style = MaterialTheme.typography.bodySmall,
                                color = JoyOnSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.select_status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JoyOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusOptionButton(
                            text = stringResource(R.string.company_work_wfo),
                            gradientColors = JoyGradientWFO,
                            icon = Icons.Default.Business,
                            onClick = onMarkWFO
                        )

                        StatusOptionButton(
                            text = stringResource(R.string.home_work_wfh),
                            gradientColors = JoyGradientWFH,
                            icon = Icons.Default.HomeWork,
                            onClick = onMarkWFH
                        )

                        StatusOptionButton(
                            text = stringResource(R.string.leave_leave),
                            gradientColors = JoyGradientLeave,
                            icon = Icons.Default.BeachAccess,
                            onClick = onMarkLeave
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (record != null) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JoyError
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.delete_record))
                }
            } else {
                Box {}
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = JoyOnSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun StatusOptionButton(
    text: String,
    gradientColors: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun getWeekdayName(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> stringResource(R.string.weekday_monday)
        DayOfWeek.TUESDAY -> stringResource(R.string.weekday_tuesday)
        DayOfWeek.WEDNESDAY -> stringResource(R.string.weekday_wednesday)
        DayOfWeek.THURSDAY -> stringResource(R.string.weekday_thursday)
        DayOfWeek.FRIDAY -> stringResource(R.string.weekday_friday)
        DayOfWeek.SATURDAY -> stringResource(R.string.weekday_saturday)
        DayOfWeek.SUNDAY -> stringResource(R.string.weekday_sunday)
    }
}

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
