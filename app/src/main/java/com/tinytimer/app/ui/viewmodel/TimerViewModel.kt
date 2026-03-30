package com.tinytimer.app.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.RecordRepository
import com.tinytimer.app.service.TimerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionRecord(
    val duration: Long,
    val groupId: Long? = null,
    val groupName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())
    private val recordRepository = RecordRepository(TinyTimerApp.instance.database.recordDao())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedGroupIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedGroupIds: StateFlow<Set<Long>> = _selectedGroupIds

    private val _stoppedGroupIds = MutableStateFlow<Set<Long>>(emptySet())
    val stoppedGroupIds: StateFlow<Set<Long>> = _stoppedGroupIds

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog

    private val _sessionRecords = MutableStateFlow<List<SessionRecord>>(emptyList())
    val sessionRecords: StateFlow<List<SessionRecord>> = _sessionRecords

    private var timerService: TimerService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val intent = Intent(getApplication(), TimerService::class.java)
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun observeServiceState() {
        viewModelScope.launch {
            timerService?.elapsedTime?.collect { time ->
                _elapsedTime.value = time
            }
        }
        viewModelScope.launch {
            timerService?.timerState?.collect { state ->
                _isRunning.value = state.isRunning
                _isPaused.value = state.isPaused
            }
        }
        viewModelScope.launch {
            timerService?.showSaveDialog?.collect { show ->
                _showSaveDialog.value = show
            }
        }
    }

    fun selectGroup(groupId: Long) {
        val current = _selectedGroupIds.value
        _selectedGroupIds.value = if (current.contains(groupId)) {
            current - groupId
        } else {
            current + groupId
        }
    }

    fun clearSelectedGroups() {
        _selectedGroupIds.value = emptySet()
        _stoppedGroupIds.value = emptySet()
    }

    fun startTimer() {
        _sessionRecords.value = emptyList()
        _stoppedGroupIds.value = emptySet()
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        getApplication<Application>().startForegroundService(intent)
        val selectedIds = _selectedGroupIds.value
        if (selectedIds.isNotEmpty()) {
            timerService?.startTimer(selectedIds.first())
        } else {
            timerService?.startTimer(null)
        }
    }

    fun pauseTimer() {
        timerService?.pauseTimer()
    }

    fun resumeTimer() {
        timerService?.resumeTimer()
    }

    fun requestStop() {
        timerService?.requestStop()
    }

    fun markAndSave() {
        val currentElapsed = if (_isPaused.value) {
            _elapsedTime.value
        } else {
            val service = timerService
            if (service != null) {
                val state = service.timerState.value
                val accumulated = state.accumulatedTime
                val running = state.isRunning
                val paused = state.isPaused
                if (running && !paused) {
                    accumulated + (System.currentTimeMillis() - state.startTime)
                } else {
                    accumulated
                }
            } else {
                _elapsedTime.value
            }
        }
        timerService?.markAndSave()
        val record = SessionRecord(duration = currentElapsed)
        _sessionRecords.value = listOf(record) + _sessionRecords.value
    }

    fun stopGroup(groupId: Long) {
        val currentElapsed = _elapsedTime.value
        _stoppedGroupIds.value = _stoppedGroupIds.value + groupId

        val groupName = groups.value.find { it.id == groupId }?.name

        viewModelScope.launch {
            val record = RecordEntity(
                groupId = groupId,
                startTime = System.currentTimeMillis() - currentElapsed,
                endTime = System.currentTimeMillis(),
                duration = currentElapsed
            )
            recordRepository.insertRecord(record)
        }

        val sessionRecord = SessionRecord(
            duration = currentElapsed,
            groupId = groupId,
            groupName = groupName
        )
        _sessionRecords.value = listOf(sessionRecord) + _sessionRecords.value

        val stoppedCount = _stoppedGroupIds.value.size
        val selectedCount = _selectedGroupIds.value.size
        if (stoppedCount >= selectedCount && selectedCount > 1) {
            timerService?.stopImmediately()
            _elapsedTime.value = 0
            clearSelectedGroups()
        }
    }

    fun quickStop() {
        val totalTime = _elapsedTime.value
        timerService?.quickStop()
        val record = SessionRecord(duration = totalTime)
        _sessionRecords.value = listOf(record) + _sessionRecords.value
        _elapsedTime.value = 0
    }

    fun confirmStop(saveRecord: Boolean, note: String? = null) {
        timerService?.confirmStop(saveRecord, note)
        if (!saveRecord) {
            _elapsedTime.value = 0
        }
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            getApplication<Application>().unbindService(serviceConnection)
            bound = false
        }
    }
}
