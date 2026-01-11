package com.silemore.sileme.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.silemore.sileme.ui.theme.WarmBackground
import kotlin.math.abs

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    showParticles: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_bg")
    
    // 渐变偏移动画
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    // 计算渐变透明度
    val alpha = 0.3f + (abs(offset - 0.5f) * 0.6f)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WarmBackground,
                        WarmBackground.copy(alpha = 0.95f),
                        Color(0xFFFFF0E8).copy(alpha = alpha)
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        // 浮动粒子装饰层
        if (showParticles) {
            FloatingParticles()
        }
        
        content()
    }
}

