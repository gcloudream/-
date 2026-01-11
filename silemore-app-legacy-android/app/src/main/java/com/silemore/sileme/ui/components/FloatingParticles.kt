package com.silemore.sileme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.silemore.sileme.ui.theme.CoralWarm
import com.silemore.sileme.ui.theme.MintFresh
import kotlin.random.Random

/**
 * 浮动粒子背景装饰
 * 在背景中渲染轻微飘动的光点，增加视觉层次感
 */
@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    baseColor: Color = CoralWarm
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 24 + 6,  // 6-30dp
                alpha = Random.nextFloat() * 0.35f + 0.15f,
                speed = Random.nextFloat() * 0.5f + 0.3f,
                phase = Random.nextFloat() * 360f,
                color = if (Random.nextBoolean()) baseColor else MintFresh
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // 计算浮动偏移 - ±80px 范围
            val floatOffset = kotlin.math.sin(
                Math.toRadians((time + particle.phase).toDouble())
            ).toFloat() * 80f
            
            val x = size.width * particle.x
            val y = size.height * particle.y + floatOffset
            
            // 呼吸效果 - 透明度变化更明显
            val breathAlpha = particle.alpha * (0.6f + 0.4f * kotlin.math.sin(
                Math.toRadians((time * particle.speed + particle.phase).toDouble())
            ).toFloat())
            
            drawCircle(
                color = particle.color.copy(alpha = breathAlpha),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
    val phase: Float,
    val color: Color
)
