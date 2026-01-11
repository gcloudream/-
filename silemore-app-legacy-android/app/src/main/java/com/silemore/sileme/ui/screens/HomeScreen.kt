package com.silemore.sileme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silemore.sileme.ui.LocalViewModelFactory
import com.silemore.sileme.ui.components.InkCard
import com.silemore.sileme.viewmodel.HomeViewModel
import com.silemore.sileme.util.formatDateTime

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
    val refreshTransition = rememberInfiniteTransition()
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing)
        )
    )
    val loadingAlpha by animateFloatAsState(
        targetValue = if (state.isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
    )
    val loadingScale by animateFloatAsState(
        targetValue = if (state.isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
    )

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
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading && state.today == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val today = state.today
                    if (today == null) {
                        InkCard(modifier = Modifier.fillMaxWidth()) {
                            Text("无法获取今日状态", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        val hasCheckedIn = today.hasCheckedIn
                        val lastCheckIn = formatDateTime(today.stats.lastCheckInAt)
                        val isPaused = state.profile?.isPaused == true
                        CheckInCircle(
                            statusText = if (hasCheckedIn) "已完成签到" else "今天尚未签到",
                            timeText = if (hasCheckedIn) "签到时间：$lastCheckIn" else "上次签到：$lastCheckIn",
                            buttonText = if (isPaused) "暂停中" else if (hasCheckedIn) "已签到" else "立即签到",
                            enabled = !hasCheckedIn && !isPaused,
                            onCheckIn = viewModel::checkIn
                        )
                    }
                }
            }

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

@Composable
private fun CheckInCircle(
    statusText: String,
    timeText: String,
    buttonText: String,
    enabled: Boolean,
    onCheckIn: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val circleSize = if (maxWidth < 280.dp) maxWidth else 280.dp
        Surface(
            modifier = Modifier.size(circleSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(statusText, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCheckIn,
                    enabled = enabled,
                    modifier = Modifier
                        .widthIn(min = 140.dp, max = 180.dp)
                        .height(48.dp)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}
