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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import me.anyang.wfodays.R
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.theme.*
import me.anyang.wfodays.ui.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadMonthData(YearMonth.now())
        delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendar",
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

            // Month Navigation
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                MonthNavigation(
                    yearMonth = uiState.currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Grid
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                CalendarGrid(
                    yearMonth = uiState.currentMonth,
                    records = uiState.monthRecords,
                    selectedDate = selectedDate,
                    onDateClick = { date ->
                        selectedDate = if (selectedDate == date) null else date
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                LegendSection()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Date Detail
            selectedDate?.let { date ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)) + slideInVertically(
                        initialOffsetY = { 20 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                ) {
                    DateDetailCard(
                        date = date,
                        record = uiState.monthRecords.find {
                            java.time.Instant.ofEpochMilli(it.date)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate() == date
                        },
                        onMarkWFO = {
                            viewModel.markWorkMode(date, WorkMode.WFO)
                        },
                        onMarkWFH = {
                            viewModel.markWorkMode(date, WorkMode.WFH)
                        },
                        onMarkLeave = {
                            viewModel.markWorkMode(date, WorkMode.LEAVE)
                        },
                        onDelete = {
                            viewModel.deleteRecord(date)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthNavigation(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous",
                tint = PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    records: List<me.anyang.wfodays.data.entity.AttendanceRecord>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

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
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        val totalCells = firstDayOfWeek + lastDayOfMonth.dayOfMonth
        val rows = (totalCells + 6) / 7

        for (week in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0 until 7) {
                    val cellIndex = week * 7 + dayOfWeek
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..lastDayOfMonth.dayOfMonth) {
                        val date = yearMonth.atDay(dayOfMonth)
                        val record = records.find {
                            java.time.Instant.ofEpochMilli(it.date)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate() == date
                        }
                        val isToday = date == today
                        val isSelected = date == selectedDate
                        val isWeekend = date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

                        CalendarDay(
                            day = dayOfMonth,
                            record = record,
                            isToday = isToday,
                            isSelected = isSelected,
                            isWeekend = isWeekend,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    record: me.anyang.wfodays.data.entity.AttendanceRecord?,
    isToday: Boolean,
    isSelected: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor = when (record?.workMode) {
        WorkMode.WFO -> PrimaryBlue
        WorkMode.WFH -> SuccessGreen
        WorkMode.LEAVE -> WarningOrange
        null -> if (isWeekend) Gray300 else Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> PrimaryBlue
                    isToday -> PrimaryBlue.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> Color.White
                isToday -> PrimaryBlue
                isWeekend -> Gray400
                else -> TextPrimary
            }
        )

        // Status dot
        if (record != null || isWeekend) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

@Composable
private fun LegendSection() {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = PrimaryBlue, label = "WFO")
            LegendItem(color = SuccessGreen, label = "WFH")
            LegendItem(color = WarningOrange, label = "Leave")
            LegendItem(color = Gray300, label = "No Record")
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun DateDetailCard(
    date: LocalDate,
    record: me.anyang.wfodays.data.entity.AttendanceRecord?,
    onMarkWFO: () -> Unit,
    onMarkWFH: () -> Unit,
    onMarkLeave: () -> Unit,
    onDelete: () -> Unit
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
        // Date header
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (record != null) {
            // Status card with icon and details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundLight)
                    .padding(12.dp)
            ) {
                // Work mode icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (record.workMode) {
                                WorkMode.WFO -> PrimaryBlue
                                WorkMode.WFH -> SuccessGreen
                                WorkMode.LEAVE -> WarningOrange
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (record.workMode) {
                            WorkMode.WFO -> Icons.Default.Business
                            WorkMode.WFH -> Icons.Default.HomeWork
                            WorkMode.LEAVE -> Icons.Default.BeachAccess
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Status info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.workMode.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Checked in at 09:03",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionIcon(
                    icon = Icons.Default.Business,
                    label = "WFO",
                    color = PrimaryBlue,
                    onClick = onMarkWFO
                )
                QuickActionIcon(
                    icon = Icons.Default.HomeWork,
                    label = "WFH",
                    color = SuccessGreen,
                    onClick = onMarkWFH
                )
                QuickActionIcon(
                    icon = Icons.Default.BeachAccess,
                    label = "Leave",
                    color = WarningOrange,
                    onClick = onMarkLeave
                )
                QuickActionIcon(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    color = ErrorRed,
                    onClick = onDelete
                )
            }
        } else {
            // No record - show quick action icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionIcon(
                    icon = Icons.Default.Business,
                    label = "WFO",
                    color = PrimaryBlue,
                    onClick = onMarkWFO
                )
                QuickActionIcon(
                    icon = Icons.Default.HomeWork,
                    label = "WFH",
                    color = SuccessGreen,
                    onClick = onMarkWFH
                )
                QuickActionIcon(
                    icon = Icons.Default.BeachAccess,
                    label = "Leave",
                    color = WarningOrange,
                    onClick = onMarkLeave
                )
            }
        }
    }
}

@Composable
private fun QuickActionIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
