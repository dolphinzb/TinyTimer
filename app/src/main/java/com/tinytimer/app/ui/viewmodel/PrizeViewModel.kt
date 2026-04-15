package com.tinytimer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.PrizeEntity
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.PrizeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PrizeViewModel : ViewModel() {
    private val prizeRepository = PrizeRepository(TinyTimerApp.instance.database.prizeDao())
    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())

    val prizes: StateFlow<List<PrizeEntity>> = prizeRepository.getAllPrizes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId

    val filteredPrizes: StateFlow<List<PrizeEntity>> = combine(
        prizes,
        _selectedGroupId
    ) { allPrizes, groupId ->
        if (groupId == null) {
            allPrizes
        } else {
            allPrizes.filter { prize ->
                prize.getBoundGroupIdsList().contains(groupId)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _editingPrize = MutableStateFlow<PrizeEntity?>(null)
    val editingPrize: StateFlow<PrizeEntity?> = _editingPrize

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog

    private val _showBindGroupDialog = MutableStateFlow(false)
    val showBindGroupDialog: StateFlow<Boolean> = _showBindGroupDialog

    private val _bindingPrize = MutableStateFlow<PrizeEntity?>(null)
    val bindingPrize: StateFlow<PrizeEntity?> = _bindingPrize

    fun setSelectedGroupId(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun createPrize(name: String, imagePath: String, level: Int) {
        viewModelScope.launch {
            val prize = PrizeEntity(
                name = name,
                imagePath = imagePath,
                level = level
            )
            prizeRepository.insertPrize(prize)
        }
    }

    fun updatePrize(prize: PrizeEntity) {
        viewModelScope.launch {
            prizeRepository.updatePrize(prize)
            _editingPrize.value = null
            _showEditDialog.value = false
        }
    }

    fun deletePrize(prize: PrizeEntity) {
        viewModelScope.launch {
            prizeRepository.deletePrize(prize)
        }
    }

    fun startEditing(prize: PrizeEntity) {
        _editingPrize.value = prize
        _showEditDialog.value = true
    }

    fun startCreating() {
        _editingPrize.value = null
        _showEditDialog.value = true
    }

    fun cancelEditing() {
        _editingPrize.value = null
        _showEditDialog.value = false
    }

    fun startBindingGroup(prize: PrizeEntity) {
        _bindingPrize.value = prize
        _showBindGroupDialog.value = true
    }

    fun cancelBindingGroup() {
        _bindingPrize.value = null
        _showBindGroupDialog.value = false
    }

    fun updatePrizeBoundGroups(prize: PrizeEntity, groupIds: List<Long>) {
        viewModelScope.launch {
            val updatedPrize = prize.setBoundGroupIdsList(groupIds)
            prizeRepository.updatePrize(updatedPrize)
            _bindingPrize.value = null
            _showBindGroupDialog.value = false
        }
    }

    fun getGroupNamesByIds(groupIds: List<Long>): List<String> {
        return groups.value.filter { it.id in groupIds }.map { it.name }
    }
}