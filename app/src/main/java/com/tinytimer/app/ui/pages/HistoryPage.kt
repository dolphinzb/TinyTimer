package com.tinytimer.app.ui.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class ViewMode {
    LIST, CHART, RANKING
}

enum class SelectionMode {
    NONE, SELECTING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(
    viewModel: HistoryViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val topRecords by viewModel.topRecords.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val filterGroupId by viewModel.filterGroupId.collectAsState()
    val filterDate by viewModel.filterDate.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var selectionMode by remember { mutableStateOf(SelectionMode.NONE) }
    var selectedRecords by remember { mutableStateOf(setOf<Long>()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dateTimeFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.importExportMessage.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importRecords(context, it) }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode == SelectionMode.SELECTING) {
                Text("已选择 ${selectedRecords.size} 项")
                Row {
                    TextButton(
                        onClick = {
                            selectedRecords = if (selectedRecords.size == records.size) {
                                emptySet()
                            } else {
                                records.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(if (selectedRecords.size == records.size) "取消全选" else "全选")
                    }
                    if (selectedRecords.isNotEmpty()) {
                        IconButton(onClick = { showBatchDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "批量删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = {
                        selectionMode = SelectionMode.NONE
                        selectedRecords = emptySet()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "取消选择")
                    }
                }
            } else {
                Text(
                    text = "历史记录",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(
                        onClick = {
                            viewMode = when (viewMode) {
                                ViewMode.LIST -> ViewMode.CHART
                                ViewMode.CHART -> ViewMode.RANKING
                                ViewMode.RANKING -> ViewMode.LIST
                            }
                        }
                    ) {
                        Icon(
                            when (viewMode) {
                                ViewMode.LIST -> Icons.Default.Dashboard
                                ViewMode.CHART -> Icons.Default.Leaderboard
                                ViewMode.RANKING -> Icons.Default.List
                            },
                            contentDescription = when (viewMode) {
                                ViewMode.LIST -> "切换到图表"
                                ViewMode.CHART -> "切换到排名"
                                ViewMode.RANKING -> "切换到列表"
                            }
                        )
                    }
                    if (records.isNotEmpty()) {
                        IconButton(onClick = { selectionMode = SelectionMode.SELECTING }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "进入选择模式"
                            )
                        }
                    }
                    if (filterGroupId != null || filterDate != null) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("清除筛选")
                        }
                    }
                    IconButton(onClick = { viewModel.exportRecords(context) }) {
                        Icon(Icons.Default.Upload, contentDescription = "导出")
                    }
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "导入")
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                item {
                    FilterChip(
                        selected = filterGroupId == null && filterDate == null,
                        onClick = { viewModel.clearFilters() },
                        label = { Text("全部") }
                    )
                }
                item {
                    FilterChip(
                        selected = filterDate != null,
                        onClick = { showDatePicker = true },
                        label = {
                            Text(
                                if (filterDate != null) dateFormat.format(Date(filterDate!!))
                                else "按日期筛选"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                items(groups) { group ->
                    FilterChip(
                        selected = filterGroupId == group.id,
                        onClick = { viewModel.setFilterGroup(group.id) },
                        label = { Text(group.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(group.color))
                            )
                        }
                    )
                }
            }

            when (viewMode) {
                ViewMode.LIST -> {
                    ListViewContent(
                        records = records,
                        groups = groups,
                        dateTimeFormat = dateTimeFormat,
                        onDeleteRecord = { viewModel.deleteRecord(it) },
                        onUpdateGroup = { recordId, newGroupId -> viewModel.updateRecordGroupId(recordId, newGroupId) },
                        selectionMode = selectionMode,
                        selectedRecords = selectedRecords,
                        onToggleSelection = { recordId ->
                            selectedRecords = if (selectedRecords.contains(recordId)) {
                                selectedRecords - recordId
                            } else {
                                selectedRecords + recordId
                            }
                        }
                    )
                }
                ViewMode.CHART -> {
                    ChartViewContent(
                        records = records,
                        groups = groups
                    )
                }
                ViewMode.RANKING -> {
                    RankingViewContent(
                        records = topRecords,
                        groups = groups,
                        dateTimeFormat = dateTimeFormat,
                        onDeleteRecord = { viewModel.deleteRecord(it) },
                        onUpdateGroup = { recordId, newGroupId -> viewModel.updateRecordGroupId(recordId, newGroupId) }
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${selectedRecords.size} 条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val recordsToDelete = records.filter { it.id in selectedRecords }
                        viewModel.deleteRecords(recordsToDelete)
                        selectedRecords = emptySet()
                        selectionMode = SelectionMode.NONE
                        showBatchDeleteConfirm = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = filterDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFilterDate(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ListViewContent(
    records: List<RecordEntity>,
    groups: List<GroupEntity>,
    dateTimeFormat: SimpleDateFormat,
    onDeleteRecord: (RecordEntity) -> Unit,
    onUpdateGroup: (Long, Long?) -> Unit,
    selectionMode: SelectionMode = SelectionMode.NONE,
    selectedRecords: Set<Long> = emptySet(),
    onToggleSelection: (Long) -> Unit = {}
) {
    if (records.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                RecordItem(
                    record = record,
                    group = groups.find { it.id == record.groupId },
                    allGroups = groups,
                    dateTimeFormat = dateTimeFormat,
                    onDelete = { onDeleteRecord(record) },
                    onUpdateGroup = { newGroupId -> onUpdateGroup(record.id, newGroupId) },
                    selectionMode = selectionMode,
                    isSelected = selectedRecords.contains(record.id),
                    onToggleSelection = { onToggleSelection(record.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "暂无记录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class RecordChartPoint(
    val recordIndex: Int,
    val durationSeconds: Float,
    val startTime: Long,
    val groupId: Long?
)

@Composable
private fun ChartViewContent(
    records: List<RecordEntity>,
    groups: List<GroupEntity>
) {
    if (records.isEmpty()) {
        EmptyState()
        return
    }

    val dateTimeFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    val filteredRecords = remember(records) {
        records.filter { it.groupId != null }
    }

    if (filteredRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Dashboard,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "暂无分组数据",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val chartPoints = remember(filteredRecords) {
        filteredRecords.mapIndexed { index, record ->
            RecordChartPoint(
                recordIndex = index,
                durationSeconds = record.duration / 1000f,
                startTime = record.startTime,
                groupId = record.groupId
            )
        }
    }

    val groupedByGroup = remember(chartPoints) {
        chartPoints.groupBy { it.groupId }
    }

    val groupColorMap = remember(groups) {
        groups.associate { it.id to Color(it.color) }
    }

    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            groupedByGroup.keys.take(5).forEach { groupId ->
                val group = groups.find { it.id == groupId }
                if (group != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(groupColorMap[groupId] ?: Color.Blue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp)
            ) {
                if (chartPoints.isEmpty()) return@Canvas

                val maxDuration = chartPoints.maxOfOrNull { it.durationSeconds } ?: 1f
                val minDuration = 0f
                val totalPoints = chartPoints.size

                val paddingLeft = 50f
                val paddingBottom = 40f
                val chartWidth = size.width - paddingLeft
                val chartHeight = size.height - paddingBottom

                val gridColor = Color.Gray.copy(alpha = 0.3f)

                // 计算最大分钟数，向上取整
                val maxMinutes = Math.ceil((maxDuration / 60f).toDouble()).toInt()
                val ySteps = maxMinutes.coerceAtLeast(5)
                
                for (i in 0..ySteps) {
                    val y = chartHeight * i / ySteps
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                for (i in 0 until totalPoints) {
                    val x = paddingLeft + (chartWidth * i / (totalPoints - 1).coerceAtLeast(1))
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, chartHeight),
                        strokeWidth = 1f
                    )
                }

                for (i in 0..ySteps) {
                    val value = ySteps * 60f - (ySteps * 60f - minDuration) * i / ySteps
                    val y = chartHeight * i / ySteps
                    val label = if (value >= 3600) {
                        String.format("%.1f时", value / 3600f)
                    } else if (value >= 60) {
                        String.format("%.0f分", value / 60f)
                    } else {
                        String.format("%.0f秒", value)
                    }
                    val textResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(fontSize = 9.sp, color = Color.Gray)
                    )
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(0f, y - textResult.size.height / 2)
                    )
                }

                val xStep = chartWidth / (totalPoints - 1).coerceAtLeast(1)
                val maxYAxisDuration = ySteps * 60f

                groupedByGroup.forEach { (groupId, points) ->
                    val color = groupColorMap[groupId] ?: Color.Blue
                    val path = Path()

                    points.sortedBy { it.recordIndex }.forEachIndexed { index, point ->
                        val x = paddingLeft + xStep * point.recordIndex
                        val normalizedValue = if (maxYAxisDuration > minDuration) {
                            (point.durationSeconds - minDuration) / (maxYAxisDuration - minDuration)
                        } else 0.5f
                        val y = chartHeight * (1 - normalizedValue)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = 2f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    points.forEach { point ->
                        val x = paddingLeft + xStep * point.recordIndex
                        val normalizedValue = if (maxYAxisDuration > minDuration) {
                            (point.durationSeconds - minDuration) / (maxYAxisDuration - minDuration)
                        } else 0.5f
                        val y = chartHeight * (1 - normalizedValue)

                        drawCircle(
                            color = color,
                            radius = 5f,
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = Offset(x, y)
                        )
                    }
                }

                chartPoints.forEachIndexed { index, point ->
                    val x = paddingLeft + xStep * index
                    val label = dateTimeFormat.format(Date(point.startTime))
                    val durationLabel = formatDuration(point.durationSeconds.toLong())
                    val combinedLabel = "$label\n$durationLabel"

                    val textResult = textMeasurer.measure(
                        text = combinedLabel,
                        style = TextStyle(fontSize = 8.sp, color = Color.Gray)
                    )

                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(x - textResult.size.width / 2, chartHeight + 5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "横轴：记录次数（从早到晚）  纵轴：时长（秒）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(seconds: Long): String {
    return when {
        seconds >= 3600 -> String.format("%d时%d分%d秒", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
        seconds >= 60 -> String.format("%d分%d秒", seconds / 60, seconds % 60)
        else -> String.format("%d秒", seconds)
    }
}

@Composable
private fun RankingViewContent(
    records: List<RecordEntity>,
    groups: List<GroupEntity>,
    dateTimeFormat: SimpleDateFormat,
    onDeleteRecord: (RecordEntity) -> Unit,
    onUpdateGroup: (Long, Long?) -> Unit
) {
    if (records.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(records) { index, record ->
                RankingItem(
                    rank = index + 1,
                    record = record,
                    group = groups.find { it.id == record.groupId },
                    allGroups = groups,
                    dateTimeFormat = dateTimeFormat,
                    onDelete = { onDeleteRecord(record) },
                    onUpdateGroup = { newGroupId -> onUpdateGroup(record.id, newGroupId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RankingItem(
    rank: Int,
    record: RecordEntity,
    group: GroupEntity?,
    allGroups: List<GroupEntity>,
    dateTimeFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onUpdateGroup: (Long?) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    val hours = record.duration / 3600000
    val minutes = (record.duration % 3600000) / 60000
    val seconds = (record.duration % 60000) / 1000
    val durationText = String.format("%d小时%d分%d秒", hours, minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> Color(0xFFFFD700) // 金色
                                2 -> Color(0xFFC0C0C0) // 银色
                                3 -> Color(0xFFCD7F32) // 铜色
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                if (group != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(group.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { showGroupPicker = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    TextButton(
                        onClick = { showGroupPicker = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "未分组",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "开始: ${dateTimeFormat.format(Date(record.startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (record.endTime != null) {
                Text(
                    text = "结束: ${dateTimeFormat.format(Date(record.endTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!record.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "备注: ${record.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
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

    if (showGroupPicker) {
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = { Text("选择分组") },
            text = {
                Column {
                    allGroups.forEach { g ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = record.groupId == g.id,
                                onClick = {
                                    onUpdateGroup(g.id)
                                    showGroupPicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(g.color))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = g.name)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = record.groupId == null,
                            onClick = {
                                onUpdateGroup(null)
                                showGroupPicker = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "未分组")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordItem(
    record: RecordEntity,
    group: GroupEntity?,
    allGroups: List<GroupEntity>,
    dateTimeFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onUpdateGroup: (Long?) -> Unit,
    selectionMode: SelectionMode = SelectionMode.NONE,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    val hours = record.duration / 3600000
    val minutes = (record.duration % 3600000) / 60000
    val seconds = (record.duration % 60000) / 1000
    val durationText = String.format("%d小时%d分%d秒", hours, minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (selectionMode == SelectionMode.SELECTING) {
                onToggleSelection()
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectionMode == SelectionMode.SELECTING) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (group != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(group.color))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { showGroupPicker = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    TextButton(
                        onClick = { showGroupPicker = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(
                            text = "未分组",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (selectionMode == SelectionMode.NONE) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = durationText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(
                    text = "开始: ${dateTimeFormat.format(Date(record.startTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (record.endTime != null) {
                Text(
                    text = "结束: ${dateTimeFormat.format(Date(record.endTime))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!record.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "备注: ${record.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
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

    if (showGroupPicker) {
        AlertDialog(
            onDismissRequest = { showGroupPicker = false },
            title = { Text("选择分组") },
            text = {
                Column {
                    allGroups.forEach { g ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = record.groupId == g.id,
                                onClick = {
                                    onUpdateGroup(g.id)
                                    showGroupPicker = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(g.color))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = g.name)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = record.groupId == null,
                            onClick = {
                                onUpdateGroup(null)
                                showGroupPicker = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "未分组")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGroupPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}