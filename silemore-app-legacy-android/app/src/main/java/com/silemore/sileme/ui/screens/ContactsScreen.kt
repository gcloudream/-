package com.silemore.sileme.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.silemore.sileme.ui.LocalViewModelFactory
import com.silemore.sileme.ui.components.InkCard
import com.silemore.sileme.ui.theme.CoralWarm
import com.silemore.sileme.ui.theme.MintFresh
import com.silemore.sileme.ui.theme.MistGray
import com.silemore.sileme.viewmodel.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onAdd: () -> Unit,
    onBack: () -> Unit
) {
    val factory = LocalViewModelFactory.current
    val viewModel: ContactsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    var deletingId by remember { mutableStateOf<Long?>(null) }
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
                title = { Text("守护人") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = CoralWarm,
                contentColor = Color.White
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = "添加")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val contacts = state.contacts
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
                InkCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "联系人概览",
                            style = MaterialTheme.typography.titleSmall
                        )
                        val total = contacts?.total ?: 0
                        val verified = contacts?.contacts?.count { it.isVerified } ?: 0
                        val remaining = contacts?.remaining ?: 0
                        val summary = "已添加 $total 位 · 已验证 $verified 位 · 剩余 $remaining 位"
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "联系人列表",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (contacts == null || contacts.contacts.isEmpty()) {
                item {
                    InkCard {
                        Text(
                            "暂无联系人，点击右下角添加守护人。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(contacts.contacts) { contact ->
                    InkCard {
                        val contactSummary = buildString {
                            append(contact.name)
                            append(" · ")
                            append(contact.email)
                            if (!contact.relationship.isNullOrBlank()) {
                                append(" · ")
                                append(contact.relationship)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = contactSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(
                                text = if (contact.isVerified) "已验证" else "待验证",
                                isVerified = contact.isVerified
                            )
                            IconButton(onClick = { deletingId = contact.id }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
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

    if (deletingId != null) {
        ConfirmDeleteDialog(
            onConfirm = {
                val id = deletingId
                if (id != null) {
                    viewModel.deleteContact(id) { }
                }
                deletingId = null
            },
            onDismiss = { deletingId = null }
        )
    }
}

@Composable
private fun StatusPill(text: String, isVerified: Boolean) {
    val background = if (isVerified) {
        MintFresh.copy(alpha = 0.15f)
    } else {
        MistGray.copy(alpha = 0.15f)
    }
    val foreground = if (isVerified) {
        MintFresh
    } else {
        MistGray
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = background
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("删除") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
        title = { Text("删除联系人") },
        text = { Text("确认删除该联系人吗？删除后将不再接收提醒邮件。") }
    )
}
