package com.tinytimer.app.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.PrizeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrizeBindGroupDialog(
    prize: PrizeEntity,
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onSave: (groupIds: List<Long>) -> Unit
) {
    val boundGroupIds = remember(prize) { prize.getBoundGroupIdsList().toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("绑定分组") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "选择要绑定到此奖品的分组：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (groups.isEmpty()) {
                    Text(
                        text = "暂无分组，请先创建分组",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(groups) { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = boundGroupIds.contains(group.id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            boundGroupIds.add(group.id)
                                        } else {
                                            boundGroupIds.remove(group.id)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(boundGroupIds.toList()) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}