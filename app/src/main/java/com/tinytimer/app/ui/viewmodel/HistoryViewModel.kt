package com.tinytimer.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel : ViewModel() {

    private val recordRepository = RecordRepository(TinyTimerApp.instance.database.recordDao())
    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _filterGroupId = MutableStateFlow<Long?>(null)
    val filterGroupId: StateFlow<Long?> = _filterGroupId

    private val _filterDate = MutableStateFlow<Long?>(null)
    val filterDate: StateFlow<Long?> = _filterDate

    val records: StateFlow<List<RecordEntity>> = combine(
        _filterGroupId,
        _filterDate
    ) { groupId, date ->
        Pair(groupId, date)
    }.flatMapLatest { (groupId, date) ->
        when {
            groupId != null -> recordRepository.getRecordsByGroup(groupId)
            date != null -> recordRepository.getRecordsByDate(date)
            else -> recordRepository.getAllRecords()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val topRecords: StateFlow<List<RecordEntity>> = _filterGroupId
        .flatMapLatest {
            if (it != null) {
                recordRepository.getTop10ShortestRecordsByGroup(it)
            } else {
                recordRepository.getTop10ShortestRecords()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage: StateFlow<String?> = _importExportMessage

    fun setFilterGroup(groupId: Long?) {
        _filterGroupId.value = groupId
    }

    fun setFilterDate(date: Long?) {
        _filterDate.value = date
    }

    fun clearFilters() {
        _filterGroupId.value = null
        _filterDate.value = null
    }

    fun deleteRecord(record: RecordEntity) {
        viewModelScope.launch {
            recordRepository.deleteRecord(record)
        }
    }

    fun updateRecordGroupId(recordId: Long, newGroupId: Long?) {
        viewModelScope.launch {
            val record = recordRepository.getRecordById(recordId)
            record?.let {
                recordRepository.updateRecord(it.copy(groupId = newGroupId))
            }
        }
    }

    fun deleteRecords(records: List<RecordEntity>) {
        viewModelScope.launch {
            recordRepository.deleteRecordsByIds(records.map { it.id })
        }
    }

    fun addManualRecord(groupId: Long?, startTime: Long, duration: Long) {
        viewModelScope.launch {
            val endTime = startTime + duration
            val record = RecordEntity(
                groupId = groupId,
                startTime = startTime,
                endTime = endTime,
                duration = duration
            )
            recordRepository.insertRecord(record)
            _importExportMessage.value = "记录添加成功"
        }
    }

    fun exportRecords(context: Context) {
        viewModelScope.launch {
            try {
                val csvContent = recordRepository.exportToCsv()
                val fileName = "TinyTimer_Records_Export.csv"
                val file = withContext(Dispatchers.IO) {
                    val exportDir = File(context.cacheDir, "exports")
                    if (!exportDir.exists()) exportDir.mkdirs()
                    val exportFile = File(exportDir, fileName)
                    exportFile.writeText(csvContent)
                    exportFile
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "导出历史记录"))
            } catch (e: Exception) {
                _importExportMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun importRecords(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvContent = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: throw Exception("无法读取文件")
                }
                val result = recordRepository.importFromCsv(csvContent)
                result.fold(
                    onSuccess = { count ->
                        _importExportMessage.value = "成功导入 $count 条记录"
                    },
                    onFailure = { e ->
                        _importExportMessage.value = "导入失败: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _importExportMessage.value = "导入失败: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _importExportMessage.value = null
    }
}