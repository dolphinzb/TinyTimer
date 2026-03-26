package com.tinytimer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {

    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingGroup = MutableStateFlow<GroupEntity?>(null)
    val editingGroup: StateFlow<GroupEntity?> = _editingGroup

    fun createGroup(name: String, color: Long) {
        viewModelScope.launch {
            val group = GroupEntity(name = name, color = color)
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
}
