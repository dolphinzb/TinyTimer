package com.tinytimer.app.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tinytimer.app.data.entity.GroupEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * 手动添加计时记录的对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualRecordDialog(
    groups: List<GroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (groupId: Long?, startTime: Long, duration: Long) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("0") }
    var seconds by remember { mutableStateOf("0") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var groupDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val selectedDate = remember(selectedDateMillis) {
        dateFormat.format(Date(selectedDateMillis))
    }
    val selectedTime = remember(selectedHour, selectedMinute) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    }

    fun validate(): Boolean {
        val h = hours.toLongOrNull() ?: 0
        val m = minutes.toLongOrNull() ?: 0
        val s = seconds.toLongOrNull() ?: 0
        val totalSeconds = h * 3600 + m * 60 + s

        if (totalSeconds <= 0) {
            errorMessage = "时长必须大于 0"
            return false
        }

        val selectedCalendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (selectedCalendar.timeInMillis > System.currentTimeMillis()) {
            errorMessage = "开始时间不能晚于当前时间"
            return false
        }

        errorMessage = null
        return true
    }

    fun handleConfirm() {
        if (validate()) {
            val h = hours.toLongOrNull() ?: 0
            val m = minutes.toLongOrNull() ?: 0
            val s = seconds.toLongOrNull() ?: 0
            val duration = (h * 3600 + m * 60 + s) * 1000

            val startCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            onConfirm(selectedGroupId, startCalendar.timeInMillis, duration)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("手动添加记录") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "输入历史计时记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("日期") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("时间") },
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.AccessTime, contentDescription = "选择时间")
                            }
                        }
                    )
                }

                Text(
                    text = "计时时长",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.all { c -> c.isDigit() }) hours = it },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(":")
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) minutes = it },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Text(":")
                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) seconds = it },
                        label = { Text("秒") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = groups.find { it.id == selectedGroupId }?.name ?: "无分组",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分组") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { groupDropdownExpanded = true }) {
                                Icon(
                                    if (groupDropdownExpanded) Icons.Filled.ArrowDropUp
                                    else Icons.Filled.ArrowDropDown,
                                    contentDescription = "选择分组"
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = groupDropdownExpanded,
                        onDismissRequest = { groupDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("无分组") },
                            onClick = {
                                selectedGroupId = null
                                groupDropdownExpanded = false
                            }
                        )
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    groupDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { handleConfirm() }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
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

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}