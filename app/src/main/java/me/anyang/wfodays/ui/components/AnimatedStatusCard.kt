package me.anyang.wfodays.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.anyang.wfodays.data.entity.RecordType
import me.anyang.wfodays.data.entity.WorkMode
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.HSBCRedLight
import me.anyang.wfodays.ui.theme.SuccessGreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedStatusCard(
    workMode: WorkMode?,
    recordType: RecordType?,
    onWFOClick: () -> Unit,
    onWFHClick: () -> Unit,
    onLeaveClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "scale"
    )

    val (backgroundBrush, icon, title, subtitle) = when (workMode) {
        WorkMode.WFO -> Quad(
            Brush.verticalGradient(colors = listOf(HSBCRed.copy(alpha = 0.9f), HSBCRed)),
            Icons.Default.Home,
            "今日 WFO",
            "在公司办公"
        )
        WorkMode.WFH -> Quad(
            Brush.verticalGradient(colors = listOf(SuccessGreen.copy(alpha = 0.9f), SuccessGreen)),
            Icons.Default.LocationOn,
            "今日 WFH",
            "在家办公"
        )
        WorkMode.LEAVE -> Quad(
            Brush.verticalGradient(colors = listOf(Color(0xFFFFB800).copy(alpha = 0.9f), Color(0xFFFFB800))),
            Icons.Default.BeachAccess,
            "今日休假",
            "享受假期"
        )
        else -> Quad(
            Brush.verticalGradient(colors = listOf(HSBCRedLight.copy(alpha = 0.9f), HSBCRedLight)),
            Icons.Default.Home,
            "今日未记录",
            "请选择工作模式"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress?.invoke() }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() },
                    label = "icon"
                ) { iconImage ->
                    Icon(
                        imageVector = iconImage,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )

                if (recordType != null && workMode != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val typeText = when (recordType) {
                        RecordType.AUTO -> "自动检测"
                        RecordType.MANUAL -> "手动记录"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = typeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // 长按提示
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "长按可切换状态",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                if (workMode == null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Three buttons for WFO, WFH, LEAVE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            text = "WFO",
                            onClick = onWFOClick,
                            color = HSBCRed
                        )
                        ActionButton(
                            text = "WFH",
                            onClick = onWFHClick,
                            color = SuccessGreen
                        )
                        ActionButton(
                            text = "休假",
                            onClick = onLeaveClick,
                            color = Color(0xFFFFB800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
