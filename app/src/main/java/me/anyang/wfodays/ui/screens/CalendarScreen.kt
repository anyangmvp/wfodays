package me.anyang.wfodays.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.components.CalendarView
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.SuccessGreen
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
    
    LaunchedEffect(Unit) {
        viewModel.loadMonthData(YearMonth.now())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历记录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 日历视图
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
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 图例说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "图例说明",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LegendItem(Icons.Default.Home, HSBCRed, "WFO - 在公司办公")
                    LegendItem(Icons.Default.LocationOn, SuccessGreen, "WFH - 在家办公")
                    LegendItem(Icons.Default.BeachAccess, Color(0xFFFFB800), "休假")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "* 周六日需长按才能记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 周末确认对话框
        if (showWeekendConfirm && longPressedDate != null) {
            AlertDialog(
                onDismissRequest = { 
                    showWeekendConfirm = false
                    longPressedDate = null
                },
                title = { Text("周末确认") },
                text = { 
                    Text("您选择的是周末（${longPressedDate!!.dayOfWeek}），确定要记录这一天吗？") 
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedDate = longPressedDate
                            showWeekendConfirm = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showWeekendConfirm = false
                            longPressedDate = null
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
        
        // 日期操作对话框
        selectedDate?.let { date ->
            val record = uiState.monthRecords.find { 
                java.time.Instant.ofEpochMilli(it.date)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate() == date 
            }
            
            AlertDialog(
                onDismissRequest = { selectedDate = null },
                title = {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column {
                        if (record != null) {
                            val (label, color) = when (record.workMode) {
                                WorkMode.WFO -> "WFO" to HSBCRed
                                WorkMode.WFH -> "WFH" to SuccessGreen
                                WorkMode.LEAVE -> "休假" to Color(0xFFFFB800)
                            }
                            Text(
                                text = "当前状态: $label",
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                            record.note?.let {
                                Text(
                                    text = "备注: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "当前状态: 未记录",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                confirmButton = {
                    if (record == null) {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.markWorkMode(date, WorkMode.WFO)
                                        selectedDate = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HSBCRed)
                                ) {
                                    Text("WFO")
                                }
                                Button(
                                    onClick = {
                                        viewModel.markWorkMode(date, WorkMode.WFH)
                                        selectedDate = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                ) {
                                    Text("WFH")
                                }
                                Button(
                                    onClick = {
                                        viewModel.markWorkMode(date, WorkMode.LEAVE)
                                        selectedDate = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800))
                                ) {
                                    Text("休假")
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteRecord(date)
                                selectedDate = null
                            }
                        ) {
                            Text("删除记录", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedDate = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
