package com.tinytimer.app.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GroupViewModel : ViewModel() {

    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingGroup = MutableStateFlow<GroupEntity?>(null)
    val editingGroup: StateFlow<GroupEntity?> = _editingGroup

    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage: StateFlow<String?> = _importExportMessage

    fun createGroup(name: String, color: Long, qualificationDuration: Long? = null) {
        viewModelScope.launch {
            val group = GroupEntity(name = name, color = color, qualificationDuration = qualificationDuration)
            groupRepository.insertGroup(group)
        }
    }

    fun updateGroup(group: GroupEntity) {
        viewModelScope.launch {
            groupRepository.updateGroup(group)
            _editingGroup.value = null
        }
    }

    fun deleteGroup(group: GroupEntity) {
        viewModelScope.launch {
            groupRepository.deleteGroup(group)
        }
    }

    fun startEditing(group: GroupEntity) {
        _editingGroup.value = group
    }

    fun cancelEditing() {
        _editingGroup.value = null
    }

    fun exportGroups(context: Context) {
        viewModelScope.launch {
            try {
                val csvContent = groupRepository.exportGroupsToCsv()
                val fileName = "TinyTimer_Groups_Export.csv"
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
                context.startActivity(Intent.createChooser(shareIntent, "导出分组"))
            } catch (e: Exception) {
                _importExportMessage.value = "导出失败: ${e.message}"
            }
        }
    }

    fun importGroups(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val csvContent = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: throw Exception("无法读取文件")
                }
                val result = groupRepository.importFromCsv(csvContent)
                result.fold(
                    onSuccess = { count ->
                        _importExportMessage.value = "成功导入 $count 个分组"
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
