package me.anyang.wfodays.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.anyang.wfodays.R
import me.anyang.wfodays.ui.theme.HSBCRed
import me.anyang.wfodays.ui.theme.HSBCRedLight

@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(12.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(HSBCRed, HSBCRedLight)
                        )
                    )
            )
        }
    }
}

@Composable
fun TargetProgressCard(
    presentDays: Int,
    requiredDays: Int,
    remainingDays: Int,
    totalWorkdays: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (requiredDays > 0) {
        (presentDays.toFloat() / requiredDays).coerceIn(0f, 1f)
    } else 0f
    
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.monthly_attendance_progress),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .height(16.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                if (remainingDays <= 0) Color(0xFF4CAF50) else HSBCRed,
                                if (remainingDays <= 0) Color(0xFF81C784) else HSBCRedLight
                            )
                        )
                    )
            )
        }
        
        Text(
            text = "$presentDays / $requiredDays 天 (${(progress * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
