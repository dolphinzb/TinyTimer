package com.tinytimer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.RecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
}
