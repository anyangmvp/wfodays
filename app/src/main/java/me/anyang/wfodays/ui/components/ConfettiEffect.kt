package me.anyang.wfodays.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    colors: List<Color> = listOf(
        Color(0xFF1976D2),
        Color(0xFF4CAF50),
        Color(0xFFFF6B35),
        Color(0xFFFFC107),
        Color(0xFF9C27B0)
    )
) {
    val density = LocalDensity.current
    val particles = remember { mutableStateListOf<Particle>() }
    
    LaunchedEffect(Unit) {
        repeat(particleCount) {
            delay(Random.nextLong(50, 200))
            particles.add(createRandomParticle(density.density))
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawParticle(particle)
            }
        }
    }
}

private fun createRandomParticle(density: Float): Particle {
    return Particle(
        x = Random.nextFloat() * 1000,
        y = -50f,
        size = Random.nextFloat() * 8 + 4,
        color = listOf(
            Color(0xFF1976D2),
            Color(0xFF4CAF50),
            Color(0xFFFF6B35),
            Color(0xFFFFC107),
            Color(0xFF9C27B0)
        ).random(),
        velocityX = Random.nextFloat() * 4 - 2,
        velocityY = Random.nextFloat() * 5 + 3,
        rotation = Random.nextFloat() * 360,
        rotationSpeed = Random.nextFloat() * 10 - 5
    )
}

private fun DrawScope.drawParticle(particle: Particle) {
    drawCircle(
        color = particle.color,
        radius = particle.size,
        center = Offset(particle.x, particle.y)
    )
}

data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val color: Color,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    onAnimationEnd: () -> Unit = {}
) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
        delay(1000)
        onAnimationEnd()
    }
    
    ConfettiEffect(modifier = modifier)
}
