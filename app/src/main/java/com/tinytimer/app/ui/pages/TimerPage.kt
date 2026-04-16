package com.tinytimer.app.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.tinytimer.app.ui.components.RewardDialog
import com.tinytimer.app.ui.viewmodel.RewardUiState
import com.tinytimer.app.ui.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerPage(
    viewModel: TimerViewModel = viewModel()
) {
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupIds by viewModel.selectedGroupIds.collectAsState()
    val stoppedGroupIds by viewModel.stoppedGroupIds.collectAsState()
    val sessionRecords by viewModel.sessionRecords.collectAsState()
    val rewardUiState by viewModel.rewardUiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                        selected = selectedGroupIds.isEmpty(),
                        onClick = { viewModel.clearSelectedGroups() },
                        label = { Text("无分组") }
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroupIds.contains(group.id),
                        onClick = { viewModel.selectGroup(group.id) },
                        label = { Text(group.name) },
                        leadingIcon = if (selectedGroupIds.contains(group.id)) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            if (sessionRecords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "本次计时记录",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(sessionRecords) { idx, record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = surfaceColor
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = record.groupName ?: "#${sessionRecords.size - idx}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = viewModel.formatTime(record.duration),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    !isRunning -> {
                        Button(
                            onClick = { viewModel.startTimer() },
                            modifier = Modifier.size(120.dp, 56.dp),
                            enabled = groups.isNotEmpty() || selectedGroupIds.isEmpty()
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
                        if (selectedGroupIds.size > 1) {
                            selectedGroupIds.forEach { groupId ->
                                val group = groups.find { it.id == groupId }
                                OutlinedButton(
                                    onClick = { viewModel.stopGroup(groupId) },
                                    modifier = Modifier.widthIn(min = 80.dp, max = 120.dp).height(56.dp),
                                    enabled = !stoppedGroupIds.contains(groupId)
                                ) {
                                    Text(
                                        text = group?.name ?: "?"
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.stopTimer() },
                                modifier = Modifier.size(120.dp, 56.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("停止")
                            }
                        }
                    }
                    selectedGroupIds.size > 1 -> {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.size(100.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("暂停")
                        }
                        selectedGroupIds.forEach { groupId ->
                            val group = groups.find { it.id == groupId }
                            OutlinedButton(
                                onClick = { viewModel.stopGroup(groupId) },
                                modifier = Modifier.widthIn(min = 80.dp, max = 120.dp).height(56.dp),
                                enabled = !stoppedGroupIds.contains(groupId)
                            ) {
                                Text(
                                    text = group?.name ?: "?"
                                )
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.size(100.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("暂停")
                        }
                        OutlinedButton(
                            onClick = { viewModel.markAndSave() },
                            modifier = Modifier.size(80.dp, 56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("标记")
                        }
                        OutlinedButton(
                            onClick = { viewModel.quickStop() },
                            modifier = Modifier.size(80.dp, 56.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("停止")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

    // 奖励弹窗
    RewardDialog(
        state = rewardUiState,
        onDismiss = { viewModel.dismissReward() },
        formatTime = { viewModel.formatTime(it) }
    )
}
