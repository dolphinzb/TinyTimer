package com.tinytimer.app.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.ui.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPage(
    viewModel: GroupViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    val editingGroup by viewModel.editingGroup.collectAsState()
    val message by viewModel.importExportMessage.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogName by remember { mutableStateOf("") }
    var dialogColor by remember { mutableStateOf(0xFF2196F3) }
    var dialogQualificationHours by remember { mutableStateOf("") }
    var dialogQualificationMinutes by remember { mutableStateOf("") }
    var dialogQualificationSeconds by remember { mutableStateOf("") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importGroups(context, it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val colorOptions = listOf(
        0xFF2196F3, 0xFF4CAF50, 0xFFF44336, 0xFFFF9800,
        0xFF9C27B0, 0xFF00BCD4, 0xFFE91E63, 0xFF607D8B
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分组管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportGroups(context) }) {
                        Icon(Icons.Default.Upload, contentDescription = "导出分组")
                    }
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "导入分组")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogName = ""
                    dialogColor = 0xFF2196F3
                    dialogQualificationHours = ""
                    dialogQualificationMinutes = ""
                    dialogQualificationSeconds = ""
                    viewModel.cancelEditing()
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加分组")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无分组",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "点击 + 添加分组",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups) { group ->
                    GroupItem(
                        group = group,
                        onEdit = {
                            dialogName = group.name
                            dialogColor = group.color
                            // 回显合格线时长
                            val qd = group.qualificationDuration
                            if (qd != null && qd > 0) {
                                val totalSeconds = qd / 1000
                                dialogQualificationHours = (totalSeconds / 3600).toString()
                                dialogQualificationMinutes = ((totalSeconds % 3600) / 60).toString()
                                dialogQualificationSeconds = (totalSeconds % 60).toString()
                            } else {
                                dialogQualificationHours = ""
                                dialogQualificationMinutes = ""
                                dialogQualificationSeconds = ""
                            }
                            viewModel.startEditing(group)
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteGroup(group) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.cancelEditing()
            },
            title = { Text(if (editingGroup != null) "编辑分组" else "新建分组") },
            text = {
                Column {
                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("分组名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("选择颜色")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .clickable { dialogColor = color }
                                    .then(
                                        if (dialogColor == color) {
                                            Modifier.padding(2.dp)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dialogColor == color) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("合格线时长（留空表示不设置）")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dialogQualificationHours,
                            onValueChange = { if (it.length <= 2) dialogQualificationHours = it.filter { c -> c.isDigit() } },
                            label = { Text("时") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = dialogQualificationMinutes,
                            onValueChange = { if (it.length <= 2) dialogQualificationMinutes = it.filter { c -> c.isDigit() } },
                            label = { Text("分") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Text(":", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = dialogQualificationSeconds,
                            onValueChange = { if (it.length <= 2) dialogQualificationSeconds = it.filter { c -> c.isDigit() } },
                            label = { Text("秒") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dialogName.isNotBlank()) {
                            // 计算合格线时长（毫秒）
                            val hours = dialogQualificationHours.toLongOrNull() ?: 0L
                            val minutes = dialogQualificationMinutes.toLongOrNull() ?: 0L
                            val seconds = dialogQualificationSeconds.toLongOrNull() ?: 0L
                            val totalMs = (hours * 3600 + minutes * 60 + seconds) * 1000
                            val qualificationDuration = if (totalMs > 0) totalMs else null

                            if (editingGroup != null) {
                                viewModel.updateGroup(editingGroup!!.copy(name = dialogName, color = dialogColor, qualificationDuration = qualificationDuration))
                            } else {
                                viewModel.createGroup(dialogName, dialogColor, qualificationDuration)
                            }
                            showDialog = false
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        viewModel.cancelEditing()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun GroupItem(
    group: GroupEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(group.color))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (group.qualificationDuration != null && group.qualificationDuration > 0) {
                    val qd = group.qualificationDuration
                    val totalSec = qd / 1000
                    val h = totalSec / 3600
                    val m = (totalSec % 3600) / 60
                    val s = totalSec % 60
                    Text(
                        text = "合格线 ${String.format("%02d:%02d:%02d", h, m, s)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除分组「${group.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}
