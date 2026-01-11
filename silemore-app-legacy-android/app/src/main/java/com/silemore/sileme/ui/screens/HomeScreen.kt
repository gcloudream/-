package com.silemore.sileme.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silemore.sileme.ui.LocalViewModelFactory
import com.silemore.sileme.ui.components.AnimatedGradientBackground
import com.silemore.sileme.ui.components.GreetingHeader
import com.silemore.sileme.ui.components.InkCard
import com.silemore.sileme.ui.components.TodayStatsCard
import com.silemore.sileme.ui.components.WaveCircle
import com.silemore.sileme.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenHistory: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val factory = LocalViewModelFactory.current
    val viewModel: HomeViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val refreshTransition = rememberInfiniteTransition(label = "refresh")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        ),
        label = "rotation"
    )
    val loadingAlpha by animateFloatAsState(
        targetValue = if (state.isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "loading_alpha"
    )
    val loadingScale by animateFloatAsState(
        targetValue = if (state.isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "loading_scale"
    )

    AnimatedGradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("今日") },
                    actions = {
                        val rotation = if (state.isLoading) refreshRotation else 0f
                        IconButton(
                            onClick = viewModel::refresh,
                            enabled = !state.isLoading
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = "刷新",
                                modifier = Modifier.graphicsLayer { rotationZ = rotation }
                            )
                        }
                        IconButton(onClick = onOpenHistory) {
                            Icon(Icons.Outlined.History, contentDescription = "记录")
                        }
                        IconButton(onClick = onOpenContacts) {
                            Icon(Icons.Outlined.Group, contentDescription = "守护人")
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "设置")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { padding ->
            if (state.isLoading && state.today == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 加载进度条
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0.5f, 0f)
                                scaleY = loadingScale
                            }
                            .alpha(loadingAlpha),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                
                // 问候语
                item {
                    val nickname = state.profile?.nickname
                    val streakDays = state.today?.stats?.currentStreak ?: 0
                    GreetingHeader(
                        nickname = nickname,
                        streakDays = streakDays
                    )
                }
                
                // 签到圆圈
                item {
                    val today = state.today
                    if (today == null) {
                        InkCard(modifier = Modifier.fillMaxWidth()) {
                            Text("无法获取今日状态", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        val hasCheckedIn = today.hasCheckedIn
                        val isPaused = state.profile?.isPaused == true
                        
                        WaveCircle(
                            isCheckedIn = hasCheckedIn,
                            isPaused = isPaused,
                            onCheckIn = viewModel::checkIn,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
                
                // 今日统计卡片
                item {
                    val streakDays = state.today?.stats?.currentStreak ?: 0
                    val guardianCount = state.profile?.contacts?.total ?: 0
                    TodayStatsCard(
                        streakDays = streakDays,
                        guardianCount = guardianCount
                    )
                }

                // 错误信息
                if (!state.error.isNullOrBlank()) {
                    item {
                        Text(
                            text = state.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
