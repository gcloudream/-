package com.silemore.sileme.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/**
 * 问候语组件 - 根据时间显示动态问候
 */
@Composable
fun GreetingHeader(
    nickname: String?,
    streakDays: Int = 0,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val hour = LocalTime.now().hour
    val (greeting, icon) = when (hour) {
        in 5..8 -> "早安" to "🌅"
        in 9..11 -> "上午好" to "☀️"
        in 12..13 -> "午安" to "🌤️"
        in 14..17 -> "下午好" to "☀️"
        in 18..20 -> "傍晚好" to "🌇"
        in 21..23 -> "晚上好" to "🌙"
        else -> "夜深了" to "✨"
    }
    
    val displayName = nickname?.takeIf { it.isNotBlank() } ?: "朋友"
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                slideInVertically(
                    animationSpec = tween(500),
                    initialOffsetY = { -it / 4 }
                )
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$icon $greeting，$displayName",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            if (streakDays > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "连续签到第 $streakDays 天 🔥",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
