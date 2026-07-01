package me.anyang.wfodays.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.anyang.wfodays.ui.theme.*

@Composable
fun DonutChart(
    percentage: Float,
    daysCompleted: Int,
    totalDays: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Float = 20f,
    trackColor: Color = Color(0xFFE2E8F0),
    progressColor: Color = PrimaryBlue
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.dp.toPx()
            val diameter = size.toPx() - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = (percentage.coerceIn(0f, 100f) / 100f) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$daysCompleted / $totalDays days",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MultiSegmentDonutChart(
    wfoDays: Int,
    wfhDays: Int,
    leaveDays: Int,
    totalDays: Int,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Float = 18f,
    showText: Boolean = true
) {
    val wfoColor = PrimaryBlue
    val wfhColor = SuccessGreen
    val leaveColor = WarningOrange
    val remainingColor = Color(0xFFE2E8F0)

    val total = totalDays.coerceAtLeast(1)
    val wfoSweep = (wfoDays.toFloat() / total) * 360f
    val wfhSweep = (wfhDays.toFloat() / total) * 360f
    val leaveSweep = (leaveDays.toFloat() / total) * 360f
    val remainingSweep = 360f - wfoSweep - wfhSweep - leaveSweep

    val percentage = if (total > 0) (wfoDays.toFloat() / total * 100) else 0f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.dp.toPx()
            val diameter = size.toPx() - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(diameter, diameter)

            var currentAngle = -90f

            // Remaining (gray)
            if (remainingSweep > 0) {
                drawArc(
                    color = remainingColor,
                    startAngle = currentAngle,
                    sweepAngle = remainingSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                currentAngle += remainingSweep
            }

            // WFO (blue)
            if (wfoSweep > 0) {
                drawArc(
                    color = wfoColor,
                    startAngle = currentAngle,
                    sweepAngle = wfoSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                currentAngle += wfoSweep
            }

            // WFH (green)
            if (wfhSweep > 0) {
                drawArc(
                    color = wfhColor,
                    startAngle = currentAngle,
                    sweepAngle = wfhSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
                currentAngle += wfhSweep
            }

            // Leave (orange)
            if (leaveSweep > 0) {
                drawArc(
                    color = leaveColor,
                    startAngle = currentAngle,
                    sweepAngle = leaveSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
            }
        }

        // Only show text if showText is true
        if (showText) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$wfoDays / $totalDays days",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun DonutChartWithTarget(
    percentage: Float,
    daysCompleted: Int,
    totalDays: Int,
    targetPercentage: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Float = 20f,
    trackColor: Color = Color(0xFFE2E8F0),
    progressColor: Color = PrimaryBlue,
    targetIndicatorColor: Color = WarningOrange
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.dp.toPx()
            val diameter = size.toPx() - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            val sweepAngle = (percentage.coerceIn(0f, 100f) / 100f) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Target indicator line
            val targetAngle = -90f + (targetPercentage.coerceIn(0f, 100f) / 100f) * 360f
            val radius = diameter / 2
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val radians = Math.toRadians(targetAngle.toDouble())
            val innerRadius = radius - stroke
            val outerRadius = radius

            val startX = centerX + (innerRadius * kotlin.math.cos(radians)).toFloat()
            val startY = centerY + (innerRadius * kotlin.math.sin(radians)).toFloat()
            val endX = centerX + (outerRadius * kotlin.math.cos(radians)).toFloat()
            val endY = centerY + (outerRadius * kotlin.math.sin(radians)).toFloat()

            drawLine(
                color = targetIndicatorColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${percentage.toInt()}%",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$daysCompleted / $totalDays days",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target: ${targetPercentage.toInt()}%",
                fontSize = 10.sp,
                color = targetIndicatorColor
            )
        }
    }
}
