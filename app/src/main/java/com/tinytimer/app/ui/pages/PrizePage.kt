package com.tinytimer.app.ui.pages

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinytimer.app.data.entity.PrizeEntity
import com.tinytimer.app.data.entity.PrizeLevel
import com.tinytimer.app.ui.viewmodel.PrizeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrizePage(
    viewModel: PrizeViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val prizes by viewModel.filteredPrizes.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val editingPrize by viewModel.editingPrize.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showBindGroupDialog by viewModel.showBindGroupDialog.collectAsState()
    val bindingPrize by viewModel.bindingPrize.collectAsState()
    var groupFilterExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("奖品配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.startCreating() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加奖品")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ExposedDropdownMenuBox(
                expanded = groupFilterExpanded,
                onExpandedChange = { groupFilterExpanded = !groupFilterExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = groups.find { it.id == selectedGroupId }?.name ?: "全部奖品",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("按分组筛选") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupFilterExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = groupFilterExpanded,
                    onDismissRequest = { groupFilterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("全部奖品") },
                        onClick = {
                            viewModel.setSelectedGroupId(null)
                            groupFilterExpanded = false
                        }
                    )
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                viewModel.setSelectedGroupId(group.id)
                                groupFilterExpanded = false
                            }
                        )
                    }
                }
            }

            if (prizes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无奖品",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "点击 + 添加奖品",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(prizes) { prize ->
                        PrizeItem(
                            prize = prize,
                            boundGroups = viewModel.getGroupNamesByIds(prize.getBoundGroupIdsList()),
                            onEdit = { viewModel.startEditing(prize) },
                            onDelete = { viewModel.deletePrize(prize) },
                            onBindGroup = { viewModel.startBindingGroup(prize) }
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        PrizeEditDialog(
            prize = editingPrize,
            onDismiss = { viewModel.cancelEditing() },
            onSave = { name, imagePath, level ->
                if (editingPrize != null) {
                    viewModel.updatePrize(editingPrize!!.copy(name = name, imagePath = imagePath, level = level))
                } else {
                    viewModel.createPrize(name, imagePath, level)
                    viewModel.cancelEditing()
                }
            }
        )
    }

    if (showBindGroupDialog && bindingPrize != null) {
        PrizeBindGroupDialog(
            prize = bindingPrize!!,
            groups = groups,
            onDismiss = { viewModel.cancelBindingGroup() },
            onSave = { groupIds ->
                viewModel.updatePrizeBoundGroups(bindingPrize!!, groupIds)
            }
        )
    }
}

@Composable
private fun PrizeItem(
    prize: PrizeEntity,
    boundGroups: List<String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBindGroup: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val imageBitmap = remember(prize.imagePath) {
        if (prize.imagePath.isNotEmpty() && File(prize.imagePath).exists()) {
            BitmapFactory.decodeFile(prize.imagePath)
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = "奖品图片",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = prize.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = prize.getPrizeLevel().displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (boundGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "绑定: ${boundGroups.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onBindGroup) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = "绑定分组",
                    tint = MaterialTheme.colorScheme.primary
                )
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
            text = { Text("确定要删除奖品「${prize.name}」吗？") },
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