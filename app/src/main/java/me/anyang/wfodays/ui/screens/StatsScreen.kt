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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.anyang.wfodays.data.repository.MonthlyStatistics
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.HSBCRedLight
import me.anyang.wfodays.ui.theme.SuccessGreen
import me.anyang.wfodays.ui.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "出勤统计",
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
                .verticalScroll(rememberScrollState())
        ) {
            // 当前月统计卡片
            uiState.currentMonthStats?.let { stats ->
                CurrentMonthStatsCard(stats = stats)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 历史统计
            Text(
                text = "历史统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (uiState.allStats.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "暂无历史数据",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                uiState.allStats.reversed().forEach { stats ->
                    HistoryStatsItem(stats = stats)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CurrentMonthStatsCard(stats: MonthlyStatistics) {
    val progress = if (stats.requiredDays > 0) {
        (stats.wfoDays.toFloat() / stats.requiredDays).coerceIn(0f, 1.2f)
    } else 0f
    
    val isGoalReached = stats.remainingDays <= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "本月 WFO 统计",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "计算公式: (总工作日 ${stats.totalWorkdays} - 休假 ${stats.leaveDays}) × 60% = 需WFO ${stats.requiredDays} 天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceAtMost(1f))
                        .height(20.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = if (isGoalReached) {
                                    listOf(SuccessGreen, Color(0xFF81C784))
                                } else {
                                    listOf(HSBCRed, HSBCRedLight)
                                }
                            ),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stats.wfoDays} / ${stats.requiredDays} 天 WFO",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(stats.currentRate * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isGoalReached) SuccessGreen else HSBCRed
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 统计详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.totalWorkdays.toString(),
                    label = "总工作日",
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatItem(
                    value = stats.leaveDays.toString(),
                    label = "休假",
                    color = Color(0xFFFFB800)
                )
                StatItem(
                    value = stats.effectiveWorkdays.toString(),
                    label = "有效工作日",
                    color = HSBCRed
                )
                StatItem(
                    value = stats.wfoDays.toString(),
                    label = "已WFO",
                    color = HSBCRed
                )
            }
            
            if (isGoalReached) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen
                        )
                        Text(
                            text = "🎉 恭喜！本月已满足60% WFO要求",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
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

@Composable
private fun HistoryStatsItem(stats: MonthlyStatistics) {
    val monthStr = "${stats.yearMonth.year}年${stats.yearMonth.monthValue}月"
    val progress = stats.wfoDays.toFloat() / stats.effectiveWorkdays.coerceAtLeast(1)
    val isGoalReached = stats.remainingDays <= 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = monthStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "WFO: ${stats.wfoDays}/${stats.effectiveWorkdays}天 (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isGoalReached) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SuccessGreen, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Text(
                    text = "还需${stats.remainingDays}天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HSBCRed,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
