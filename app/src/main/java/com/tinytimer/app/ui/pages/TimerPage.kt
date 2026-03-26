package com.tinytimer.app.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinytimer.app.ui.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerPage(
    viewModel: TimerViewModel = viewModel(),
    onNavigateToGroups: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    var noteText by remember { mutableStateOf("") }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("保存记录") },
            text = {
                Column {
                    Text("是否保存此次计时记录？")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmStop(saveRecord = true, note = noteText.ifBlank { null })
                        noteText = ""
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmStop(saveRecord = false)
                        noteText = ""
                    }
                ) {
                    Text("不保存")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TinyTimer") },
                actions = {
                    IconButton(onClick = onNavigateToGroups) {
                        Icon(Icons.Default.Folder, contentDescription = "分组")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "历史")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = viewModel.formatTime(elapsedTime),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    isRunning && isPaused -> "已暂停"
                    isRunning -> "计时中..."
                    else -> "就绪"
                },
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "选择分组",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = { viewModel.selectGroup(null) },
                        label = { Text("无分组") }
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroupId == group.id,
                        onClick = { viewModel.selectGroup(group.id) },
                        label = { Text(group.name) },
                        leadingIcon = if (selectedGroupId == group.id) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    !isRunning -> {
                        Button(
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier.size(120.dp, 56.dp),
                            enabled = groups.isNotEmpty() || selectedGroupId == null
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("开始")
                        }
                    }
                    isPaused -> {
                        Button(
                            onClick = { viewModel.resumeTimer() },
                            modifier = Modifier.size(120.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("继续")
                        }
                        OutlinedButton(
                            onClick = { viewModel.requestStop() },
                            modifier = Modifier.size(120.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("停止")
                        }
                    }
                    else -> {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.size(120.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("暂停")
                        }
                        OutlinedButton(
                            onClick = { viewModel.requestStop() },
                            modifier = Modifier.size(120.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("停止")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
