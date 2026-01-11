package com.silemore.sileme.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silemore.sileme.ui.LocalViewModelFactory
import com.silemore.sileme.ui.components.InkCard
import com.silemore.sileme.ui.components.SectionHeader
import com.silemore.sileme.util.formatDateTime
import com.silemore.sileme.viewmodel.SettingsViewModel
import com.silemore.sileme.viewmodel.SessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sessionViewModel: SessionViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val factory = LocalViewModelFactory.current
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showTimePicker by remember { mutableStateOf(false) }

    var alertDays by remember { mutableStateOf(3) }
    var reminderTime by remember { mutableStateOf("20:00") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var pauseDays by remember { mutableStateOf(7) }
    var pauseReason by remember { mutableStateOf("") }

    LaunchedEffect(state.profile?.id) {
        val profile = state.profile ?: return@LaunchedEffect
        alertDays = profile.alertDays
        reminderTime = profile.reminderTime
        reminderEnabled = profile.reminderEnabled
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            InkCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(
                        title = "提醒"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val alertOptions = listOf(1, 2, 3, 5, 7, 10)
                        val currentIndex = alertOptions.indexOf(alertDays).coerceAtLeast(0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "阈值",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$alertDays 天",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Slider(
                            value = currentIndex.toFloat(),
                            onValueChange = { index ->
                                val newIndex = index.toInt().coerceIn(0, alertOptions.lastIndex)
                                alertDays = alertOptions[newIndex]
                            },
                            valueRange = 0f..alertOptions.lastIndex.toFloat(),
                            steps = (alertOptions.size - 2).coerceAtLeast(0)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "时间",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                showTimePicker = true
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(reminderTime, style = MaterialTheme.typography.titleMedium)
                                Icon(Icons.Outlined.Schedule, contentDescription = "选择时间")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "提醒",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (reminderEnabled) "开" else "关",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.updateSettings(
                                alertDays = alertDays,
                                reminderTime = reminderTime,
                                reminderEnabled = reminderEnabled
                            ) { ok ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (ok) "设置已保存" else "保存失败，请稍后重试。"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("保存")
                    }
                }
            }

            InkCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(
                        title = "暂停"
                    )
                    val profile = state.profile
                    if (profile?.isPaused == true) {
                        InfoBlock(
                            label = "截止",
                            value = formatDateTime(profile.pauseUntil)
                        )
                        Button(
                            onClick = {
                                viewModel.resume { ok ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (ok) "已恢复签到" else "恢复失败"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("恢复")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val pauseOptions = listOf(1, 3, 7, 14, 30)
                            val pauseIndex = pauseOptions.indexOf(pauseDays).coerceAtLeast(0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "天数",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$pauseDays 天",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Slider(
                                value = pauseIndex.toFloat(),
                                onValueChange = { index ->
                                    val newIndex = index.toInt().coerceIn(0, pauseOptions.lastIndex)
                                    pauseDays = pauseOptions[newIndex]
                                },
                                valueRange = 0f..pauseOptions.lastIndex.toFloat(),
                                steps = (pauseOptions.size - 2).coerceAtLeast(0)
                            )
                        }
                        OutlinedTextField(
                            value = pauseReason,
                            onValueChange = { pauseReason = it },
                            label = { Text("说明（选填）") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 4
                        )
                        Button(
                            onClick = {
                                viewModel.pause(
                                    days = pauseDays,
                                    reason = pauseReason.trim().ifBlank { null }
                                ) { ok ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (ok) "暂停已开启" else "暂停失败"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("暂停")
                        }
                    }
                }
            }

            InkCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionHeader(
                        title = "账号"
                    )
                    val profile = state.profile
                    InfoBlock(label = "昵称", value = profile?.nickname ?: "-")
                    InfoBlock(label = "邮箱", value = profile?.email ?: "-")
                    InfoBlock(label = "守护人", value = "${profile?.contacts?.total ?: 0} 位")
                }
            }

            OutlinedButton(
                onClick = {
                    scope.launch {
                        sessionViewModel.clearSession()
                        onLoggedOut()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("退出登录")
            }

            if (!state.error.isNullOrBlank()) {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showTimePicker) {
        ReminderTimeSheet(
            initialTime = reminderTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { selected ->
                reminderTime = selected
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var allowSheetDismiss by remember { mutableStateOf(false) }
    val allowDismissState by rememberUpdatedState(allowSheetDismiss)
    val timeParts = initialTime.split(":")
    val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 20
    val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { value ->
            !(value == SheetValue.Hidden && !allowDismissState)
        }
    )
    val formattedTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)

    ModalBottomSheet(
        onDismissRequest = {
            if (allowDismissState) {
                onConfirm(formattedTime)
                allowSheetDismiss = false
                onDismiss()
            }
        },
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(sheetState) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.any { it.pressed }
                                if (pressed) {
                                    allowSheetDismiss = true
                                } else if (sheetState.targetValue != SheetValue.Hidden) {
                                    allowSheetDismiss = false
                                }
                            }
                        }
                    },

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BottomSheetDefaults.DragHandle()
                Text(
                    text = "提醒时间",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SwipeTimeColumn(
                        label = "小时",
                        value = timePickerState.hour,
                        range = 0..23,
                        onValueChange = { timePickerState.hour = it },
                        onInteractionStart = { allowSheetDismiss = false },
                        onInteractionEnd = { allowSheetDismiss = false }
                    )
                    SwipeTimeColumn(
                        label = "分钟",
                        value = timePickerState.minute,
                        range = 0..59,
                        onValueChange = { timePickerState.minute = it },
                        onInteractionStart = { allowSheetDismiss = false },
                        onInteractionEnd = { allowSheetDismiss = false }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(
                    onClick = { onConfirm(formattedTime) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("确定")
                }
            }
        }
    }
}

@Composable
private fun SwipeTimeColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    onInteractionStart: () -> Unit,
    onInteractionEnd: () -> Unit
) {
    val stepPx = with(LocalDensity.current) { 24.dp.toPx() }
    val previous = wrapValue(value - 1, range)
    val next = wrapValue(value + 1, range)
    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "timeWheelOffset"
    )

    Column(
        modifier = Modifier
            .width(108.dp)
            .height(176.dp)
            .padding(vertical = 8.dp)
            .pointerInput(value, range) {
                detectVerticalDragGestures(
                    onDragStart = {
                        dragOffset = 0f
                        onInteractionStart()
                    },
                    onDragEnd = {
                        dragOffset = 0f
                        onInteractionEnd()
                    },
                    onDragCancel = {
                        dragOffset = 0f
                        onInteractionEnd()
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount * 0.85f
                        var steps = 0
                        while (dragOffset <= -stepPx) {
                            dragOffset += stepPx
                            steps += 1
                        }
                        while (dragOffset >= stepPx) {
                            dragOffset -= stepPx
                            steps -= 1
                        }
                        dragOffset = dragOffset.coerceIn(-stepPx, stepPx)
                        if (steps != 0) {
                            onValueChange(wrapValue(value + steps, range))      
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.offset { IntOffset(0, animatedOffset.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%02d".format(previous),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.4f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "%02d".format(value),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "%02d".format(next),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.4f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun wrapValue(value: Int, range: IntRange): Int {
    val size = range.last - range.first + 1
    if (size <= 0) return range.first
    val offset = ((value - range.first) % size + size) % size
    return range.first + offset
}

@Composable
private fun InfoBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
