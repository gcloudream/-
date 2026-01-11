package com.silemore.sileme.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silemore.sileme.ui.theme.*

@Composable
fun WaveCircle(
    modifier: Modifier = Modifier,
    isCheckedIn: Boolean,
    isPaused: Boolean,
    onCheckIn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    // 点击弹跳动画
    var isPressed by remember { mutableStateOf(false) }
    
    // 签到成功庆祝动画
    var isCelebrating by remember { mutableStateOf(false) }
    val celebrationScale by animateFloatAsState(
        targetValue = when {
            isCelebrating -> 1.15f
            isPressed -> 0.92f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = if (isCelebrating) Spring.StiffnessLow else Spring.StiffnessMedium
        ),
        finishedListener = { 
            isPressed = false
            isCelebrating = false
        },
        label = "scale"
    )
    
    // 监听签到状态变化，触发庆祝动画
    var previousCheckedIn by remember { mutableStateOf(isCheckedIn) }
    LaunchedEffect(isCheckedIn) {
        if (isCheckedIn && !previousCheckedIn) {
            isCelebrating = true
        }
        previousCheckedIn = isCheckedIn
    }
    
    // 3层波纹，使用不同的相位偏移
    val wavePhases = listOf(0f, 0.33f, 0.66f)
    
    // 根据签到状态选择颜色
    val waveColor by animateColorAsState(
        targetValue = when {
            isPaused -> MistGray
            isCheckedIn -> MintFresh
            else -> CoralWarm
        },
        animationSpec = tween(500),
        label = "wave_color"
    )
    
    // 波纹动画速度 (已签到后变慢)
    val waveDuration = if (isCheckedIn) 3000 else 2000
    
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = celebrationScale
            scaleY = celebrationScale
        },
        contentAlignment = Alignment.Center
    ) {
        // 绘制3层波纹
        wavePhases.forEachIndexed { index, phase ->
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(waveDuration, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset((waveDuration * phase).toInt())
                ),
                label = "wave_$index"
            )
            
            Canvas(modifier = Modifier.size(280.dp)) {
                val waveScale = 1f + (progress * 0.3f)
                val alpha = (1f - progress) * (0.6f - index * 0.2f)
                
                drawCircle(
                    color = waveColor.copy(alpha = alpha),
                    radius = size.minDimension / 2 * waveScale,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // 中心可点击圆形
        ClickableCircle(
            isCheckedIn = isCheckedIn,
            isPaused = isPaused,
            waveColor = waveColor,
            onClick = {
                if (!isCheckedIn && !isPaused) {
                    isPressed = true
                    onCheckIn()
                }
            }
        )
    }
}

@Composable
private fun ClickableCircle(
    isCheckedIn: Boolean,
    isPaused: Boolean,
    waveColor: Color,
    onClick: () -> Unit
) {
    // 渐变色
    val gradientColors = when {
        isPaused -> listOf(MistGray, MistGray.copy(alpha = 0.7f))
        isCheckedIn -> listOf(MintLight, MintFresh)
        else -> listOf(CoralLight, CoralWarm)
    }
    
    Box(
        modifier = Modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = gradientColors,
                    center = Offset(110f, 110f),
                    radius = 220f
                )
            )
            .clickable(
                enabled = !isCheckedIn && !isPaused,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() }
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        waveColor.copy(alpha = 0.3f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // 只显示简洁的文字
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isCheckedIn) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
            }
            
            Text(
                text = when {
                    isPaused -> "暂停中"
                    isCheckedIn -> "已签到"
                    else -> "签到"
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 36.sp
            )
        }
    }
}
