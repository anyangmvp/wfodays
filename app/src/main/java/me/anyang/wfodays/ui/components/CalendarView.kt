package me.anyang.wfodays.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarView(
    yearMonth: YearMonth,
    records: List<AttendanceRecord>,
    onDateClick: (LocalDate) -> Unit,
    onDateLongPress: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
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
        label = "calendar_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = tween(300),
        label = "calendar_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(animatedAlpha)
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
            // 星期标题
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
                daysOfWeek.forEachIndexed { index, day ->
                    val isWeekend = index == 0 || index == 6
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isWeekend) WarningYellow else PrimaryBlueDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 分隔线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PrimaryBlue.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 日历网格
            val days = generateCalendarDays(yearMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp)
            ) {
                items(days) { dayDate ->
                    if (dayDate != null) {
                        val record = records.find {
                            java.time.Instant.ofEpochMilli(it.date)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate() == dayDate
                        }
                        val isWeekend = dayDate.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
                        CalendarDayCell(
                            date = dayDate,
                            record = record,
                            isToday = dayDate == LocalDate.now(),
                            isWeekend = isWeekend,
                            onClick = { onDateClick(dayDate) },
                            onLongPress = { onDateLongPress(dayDate) }
                        )
                    } else {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    record: AttendanceRecord?,
    isToday: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val cellState = when (record?.workMode) {
        WorkMode.WFO -> CellState(
            backgroundColor = PrimaryBlue,
            icon = Icons.Default.Business,
            iconColor = Color.White,
            showBorder = false
        )
        WorkMode.WFH -> CellState(
            backgroundColor = SuccessGreen,
            icon = Icons.Default.HomeWork,
            iconColor = Color.White,
            showBorder = false
        )
        WorkMode.LEAVE -> CellState(
            backgroundColor = WarningYellow,
            icon = Icons.Default.BeachAccess,
            iconColor = Color.White,
            showBorder = false
        )
        else -> CellState(
            backgroundColor = Color.Transparent,
            icon = null,
            iconColor = Color.Transparent,
            showBorder = isToday
        )
    }

    val textColor = when {
        record != null -> Color.White
        isToday -> PrimaryBlue
        isWeekend -> WarningYellow.copy(alpha = 0.8f)
        else -> PrimaryBlueDark
    }

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "day_scale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(cellState.backgroundColor)
            .then(
                if (cellState.showBorder) {
                    Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isPressed = true
                        onClick()
                    },
                    onLongPress = {
                        isPressed = true
                        onLongPress()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 日期数字
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || record != null) FontWeight.Bold else FontWeight.Normal
            )

            // 状态图标
            if (cellState.icon != null) {
                Icon(
                    imageVector = cellState.icon,
                    contentDescription = null,
                    tint = cellState.iconColor,
                    modifier = Modifier.size(14.dp)
                )
            } else if (isWeekend && !isToday) {
                // 周末标记点
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(WarningYellow.copy(alpha = 0.5f), CircleShape)
                )
            } else if (isToday) {
                // 今天标记点
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(PrimaryBlue, CircleShape)
                )
            }
        }
    }
}

// 辅助数据类用于日历单元格状态
private data class CellState(
    val backgroundColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val iconColor: Color,
    val showBorder: Boolean
)

private fun generateCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val days = mutableListOf<LocalDate?>()
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()

    val dayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7
    repeat(dayOfWeekValue) {
        days.add(null)
    }

    for (day in 1..lastDayOfMonth.dayOfMonth) {
        days.add(yearMonth.atDay(day))
    }

    return days
}
