package me.anyang.wfodays.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import me.anyang.wfodays.ui.components.AnimatedStatusCard
import me.anyang.wfodays.ui.components.SuccessAnimation
import me.anyang.wfodays.ui.components.TargetProgressCard
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.SuccessGreen
import me.anyang.wfodays.ui.viewmodel.HomeViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuccessAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = HSBCRed
                        )
                        Text(
                            text = "WFODays",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "统计"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCalendar,
                containerColor = HSBCRed
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "日历",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 今日状态卡片
            AnimatedStatusCard(
                workMode = uiState.todayRecord?.workMode,
                recordType = uiState.todayRecord?.recordType,
                onWFOClick = {
                    viewModel.manualCheckIn()
                    showSuccessAnimation = true
                },
                onWFHClick = {
                    viewModel.markAsWFH()
                    showSuccessAnimation = true
                },
                onLeaveClick = {
                    viewModel.markAsLeave()
                    showSuccessAnimation = true
                },
                onLongPress = {
                    // 长按切换今日状态
                    viewModel.toggleTodayStatus()
                    showSuccessAnimation = true
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 本月进度
            uiState.currentMonthStats?.let { stats ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TargetProgressCard(
                            presentDays = stats.wfoDays,
                            requiredDays = stats.requiredDays,
                            remainingDays = stats.remainingDays,
                            totalWorkdays = stats.effectiveWorkdays
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 统计详情
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBadge(
                                value = stats.totalWorkdays.toString(),
                                label = "总工作日",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatBadge(
                                value = stats.leaveDays.toString(),
                                label = "休假",
                                color = Color(0xFFFFB800)
                            )
                            StatBadge(
                                value = stats.effectiveWorkdays.toString(),
                                label = "有效工作日",
                                color = HSBCRed
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 智能提示
                        if (stats.remainingDays > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = HSBCRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "还需WFO ${stats.remainingDays} 天即可达标 (已扣除${stats.leaveDays}天休假)",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = HSBCRed,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "🎉 恭喜！本月已满足60% WFO要求",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 最近记录
            Text(
                text = "最近记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.recentRecords.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "暂无记录",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                uiState.recentRecords.take(5).forEach { record ->
                    val date = java.time.Instant.ofEpochMilli(record.date)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    val dateStr = date.format(DateTimeFormatter.ofPattern("MM月dd日"))
                    
                    val (icon, color, label) = when (record.workMode) {
                        WorkMode.WFO -> Triple(Icons.Default.Home, HSBCRed, "WFO")
                        WorkMode.WFH -> Triple(Icons.Default.LocationOn, SuccessGreen, "WFH")
                        WorkMode.LEAVE -> Triple(Icons.Default.BeachAccess, Color(0xFFFFB800), "休假")
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "$dateStr - $label",
                                    modifier = Modifier.padding(start = 12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = if (record.recordType == me.anyang.wfodays.data.entity.RecordType.AUTO) "自动" else "手动",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // 成功动画
        AnimatedVisibility(
            visible = showSuccessAnimation,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SuccessAnimation(
                onAnimationEnd = { showSuccessAnimation = false }
            )
        }
    }
}

@Composable
private fun StatBadge(
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
