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
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.RecordRepository
import com.tinytimer.app.service.TimerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())
    private val recordRepository = RecordRepository(TinyTimerApp.instance.database.recordDao())

    val groups: StateFlow<List<GroupEntity>> = groupRepository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupId: StateFlow<Long?> = _selectedGroupId

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog

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
                if (state.groupId != null) {
                    _selectedGroupId.value = state.groupId
                }
            }
        }
        viewModelScope.launch {
            timerService?.showSaveDialog?.collect { show ->
                _showSaveDialog.value = show
            }
        }
    }

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun startTimer() {
        val intent = Intent(getApplication(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        getApplication<Application>().startForegroundService(intent)
        _selectedGroupId.value?.let { groupId ->
            timerService?.startTimer(groupId)
        } ?: timerService?.startTimer(null)
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
