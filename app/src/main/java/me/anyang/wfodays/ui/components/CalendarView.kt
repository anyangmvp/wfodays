package me.anyang.wfodays.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.anyang.wfodays.data.entity.AttendanceRecord
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.SuccessGreen
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 月份选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "上个月"
                    )
                }
                
                Text(
                    text = "${yearMonth.year}年${yearMonth.monthValue}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下个月"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 星期标题
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
                daysOfWeek.forEachIndexed { index, day ->
                    val isWeekend = index == 0 || index == 6
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isWeekend) HSBCRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
    val (backgroundColor, icon, iconColor) = when (record?.workMode) {
        WorkMode.WFO -> Triple(HSBCRed, Icons.Default.Home, Color.White)
        WorkMode.WFH -> Triple(SuccessGreen, Icons.Default.LocationOn, Color.White)
        WorkMode.LEAVE -> Triple(Color(0xFFFFB800), Icons.Default.BeachAccess, Color.White)
        else -> Triple(Color.Transparent, null, Color.Transparent)
    }
    
    val textColor = when {
        record != null -> Color.White
        isWeekend -> HSBCRed
        isToday -> HSBCRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || record != null) FontWeight.Bold else FontWeight.Normal
            )
            
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(12.dp)
                )
            } else if (isWeekend && !isToday) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(HSBCRed.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }
}

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
