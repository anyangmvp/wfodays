package me.anyang.wfodays.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历记录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
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
        ) {
            // 月份导航卡片 - 带渐变背景
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

            // 日历视图 - 带入场动画
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
                            // 周六日不允许普通点击
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

            // 图例说明 - 商务风格卡片
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

            // 本月统计概览
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(900)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                MonthSummaryCard(uiState = uiState)
            }
        }

        // 周末确认对话框
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

        // 日期操作对话框
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
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryBlueDark, PrimaryBlue, PrimaryBlueLight)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "上月",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${yearMonth.year}年",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "${yearMonth.monthValue}月",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "下月",
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
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .background(PrimaryBlue, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "图例说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = PrimaryBlueDark
                )
            }

            // 图例项
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    icon = Icons.Default.Business,
                    color = PrimaryBlue,
                    text = "WFO",
                    subtext = "公司办公"
                )
                LegendItem(
                    icon = Icons.Default.HomeWork,
                    color = SuccessGreen,
                    text = "WFH",
                    subtext = "居家办公"
                )
                LegendItem(
                    icon = Icons.Default.BeachAccess,
                    color = WarningYellow,
                    text = "休假",
                    subtext = "享受假期"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 提示文字
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "长按日期可记录，周末需长按确认",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlue.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    text: String,
    subtext: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = color.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
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
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
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
                shape = RoundedCornerShape(20.dp),
                spotColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .background(PrimaryBlue, RoundedCornerShape(2.dp))
                )
                Text(
                    text = "本月概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                    color = PrimaryBlueDark
                )
            }

            // 统计数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    count = wfoCount,
                    label = "WFO",
                    color = PrimaryBlue,
                    icon = Icons.Default.Business
                )
                SummaryStatItem(
                    count = wfhCount,
                    label = "WFH",
                    color = SuccessGreen,
                    icon = Icons.Default.HomeWork
                )
                SummaryStatItem(
                    count = leaveCount,
                    label = "休假",
                    color = WarningYellow,
                    icon = Icons.Default.BeachAccess
                )
                SummaryStatItem(
                    count = uiState.monthRecords.size,
                    label = "总计",
                    color = Color.Gray,
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
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
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
                        .size(40.dp)
                        .background(WarningYellow.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningYellow
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "周末确认",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
            }
        },
        text = {
            Text(
                text = "您选择的是 ${date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}（${getWeekdayName(date.dayOfWeek)}），确定要记录这一天吗？",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("取消")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
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
                    text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlueDark
                )
                if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                    Text(
                        text = "周末",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarningYellow,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        text = {
            Column {
                if (record != null) {
                    val (label, color, icon) = when (record.workMode) {
                        WorkMode.WFO -> Triple("WFO", PrimaryBlue, Icons.Default.Business)
                        WorkMode.WFH -> Triple("WFH", SuccessGreen, Icons.Default.HomeWork)
                        WorkMode.LEAVE -> Triple("休假", WarningYellow, Icons.Default.BeachAccess)
                        else -> Triple("未知", Color.Gray, Icons.Default.Help)
                    }

                    // 当前状态卡片
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(color.copy(alpha = 0.1f))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(
                                modifier = Modifier.padding(start = 16.dp)
                            ) {
                                Text(
                                    text = "当前状态",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }

                    record.note?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "备注: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // 未记录状态 - 显示选择按钮
                    Text(
                        text = "选择状态",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 使用垂直布局显示三个选项
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WFO 按钮
                        StatusOptionButton(
                            text = "公司办公 (WFO)",
                            color = PrimaryBlue,
                            icon = Icons.Default.Business,
                            onClick = onMarkWFO
                        )

                        // WFH 按钮
                        StatusOptionButton(
                            text = "居家办公 (WFH)",
                            color = SuccessGreen,
                            icon = Icons.Default.HomeWork,
                            onClick = onMarkWFH
                        )

                        // 休假按钮
                        StatusOptionButton(
                            text = "休假 (Leave)",
                            color = WarningYellow,
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
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除记录")
                }
            } else {
                // 未记录时不显示确认按钮
                Box {}
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("取消")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun StatusOptionButton(
    text: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(text)
    }
}

private fun getWeekdayName(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}
