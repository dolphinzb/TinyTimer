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
import com.tinytimer.app.data.entity.PrizeEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.model.RewardInfo
import com.tinytimer.app.data.model.TimerRanking
import com.tinytimer.app.data.repository.GroupRepository
import com.tinytimer.app.data.repository.PrizeRepository
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

/**
 * 奖励弹窗状态
 */
sealed class RewardUiState {
    /** 隐藏状态 */
    data object Hidden : RewardUiState()
    /** 显示排名（前三名） */
    data class ShowRanking(
        val ranking: TimerRanking,
        val rewardInfo: RewardInfo?
    ) : RewardUiState()
    /** 显示鼓励提示（排名4+） */
    data class ShowEncouragement(
        val groupName: String
    ) : RewardUiState()
}

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val groupRepository = GroupRepository(TinyTimerApp.instance.database.groupDao())
    private val recordRepository = RecordRepository(TinyTimerApp.instance.database.recordDao())
    private val prizeRepository = PrizeRepository(TinyTimerApp.instance.database.prizeDao())

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

    private val _rewardUiState = MutableStateFlow<RewardUiState>(RewardUiState.Hidden)
    val rewardUiState: StateFlow<RewardUiState> = _rewardUiState

    // 跟踪待排名的分组信息（多分组场景）
    private data class PendingRanking(
        val groupId: Long,
        val groupName: String,
        val duration: Long
    )
    private val _pendingRankings = MutableStateFlow<List<PendingRanking>>(emptyList())

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

    fun stopTimer() {
        val totalTime = _elapsedTime.value
        val selectedIds = _selectedGroupIds.value
        timerService?.stopTimer()

        // 单分组或无分组计时
        val groupId = selectedIds.firstOrNull()
        val groupName = groupId?.let { gid -> groups.value.find { it.id == gid }?.name }

        val record = SessionRecord(duration = totalTime, groupId = groupId, groupName = groupName)
        _sessionRecords.value = listOf(record) + _sessionRecords.value
        _elapsedTime.value = 0

        // 触发排名计算
        if (groupId != null && totalTime > 0) {
            val name = groups.value.find { it.id == groupId }?.name ?: ""
            viewModelScope.launch {
                calculateRanking(groupId, name, totalTime)
            }
        }
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

        if (_selectedGroupIds.value.size > 1) {
            timerService?.markAndSave()
        }

        val record = SessionRecord(duration = currentElapsed)
        _sessionRecords.value = listOf(record) + _sessionRecords.value
    }

    fun stopGroup(groupId: Long) {
        val currentElapsed = _elapsedTime.value
        _stoppedGroupIds.value = _stoppedGroupIds.value + groupId

        val groupName = groups.value.find { it.id == groupId }?.name ?: ""

        viewModelScope.launch {
            val record = RecordEntity(
                groupId = groupId,
                startTime = System.currentTimeMillis() - currentElapsed,
                endTime = System.currentTimeMillis(),
                duration = currentElapsed
            )
            recordRepository.insertRecord(record)

            // 添加待排名信息
            val pending = PendingRanking(groupId, groupName, currentElapsed)
            _pendingRankings.value = _pendingRankings.value + pending

            // 检查是否所有分组都已停止
            val stoppedCount = _stoppedGroupIds.value.size
            val selectedCount = _selectedGroupIds.value.size
            if (stoppedCount >= selectedCount && selectedCount > 1) {
                // 所有分组都停止了，触发排名计算
                for (p in _pendingRankings.value) {
                    calculateRanking(p.groupId, p.groupName, p.duration)
                }
                _pendingRankings.value = emptyList()
            }
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
        val selectedIds = _selectedGroupIds.value
        val groupId = selectedIds.firstOrNull() ?: timerService?.timerState?.value?.groupId
        val groupName = groupId?.let { gid -> groups.value.find { it.id == gid }?.name }

        timerService?.quickStop()
        val record = SessionRecord(duration = totalTime, groupId = groupId, groupName = groupName)
        _sessionRecords.value = listOf(record) + _sessionRecords.value
        _elapsedTime.value = 0

        // 触发排名计算
        if (groupId != null && totalTime > 0) {
            val name = groupName ?: ""
            viewModelScope.launch {
                calculateRanking(groupId, name, totalTime)
            }
        }
    }

    fun confirmStop(saveRecord: Boolean, note: String? = null) {
        val totalTime = _elapsedTime.value
        val groupId = timerService?.timerState?.value?.groupId
        val groupName = groupId?.let { gid -> groups.value.find { it.id == gid }?.name }

        timerService?.confirmStop(saveRecord, note)
        if (!saveRecord) {
            _elapsedTime.value = 0
        }

        // 保存记录时触发排名计算
        if (saveRecord && groupId != null && totalTime > 0) {
            val name = groupName ?: ""
            viewModelScope.launch {
                calculateRanking(groupId, name, totalTime)
            }
        }
    }

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * 计算排名并更新奖励 UI 状态
     * 排名 = 该分组内比当次计时更短的记录数 + 1
     */
    private suspend fun calculateRanking(groupId: Long, groupName: String, duration: Long) {
        // 确保记录已保存（delay 等待 Service 异步保存完成）
        kotlinx.coroutines.delay(100)

        val shorterCount = recordRepository.countRecordsShorterThan(groupId, duration)
        val rank = shorterCount + 1

        val ranking = TimerRanking(
            groupId = groupId,
            groupName = groupName,
            currentDuration = duration,
            rank = rank
        )

        if (rank <= 3) {
            // 前三名，查找对应的奖品
            val prizes = prizeRepository.getPrizesByGroupIdOnce(groupId)
            val matchingPrize = prizes.find { it.level == rank }
            val rewardInfo = if (matchingPrize != null) {
                RewardInfo(
                    rank = rank,
                    prizeName = matchingPrize.name,
                    prizeImagePath = matchingPrize.imagePath
                )
            } else {
                RewardInfo(rank = rank, prizeName = null, prizeImagePath = null)
            }
            _rewardUiState.value = RewardUiState.ShowRanking(ranking, rewardInfo)
        } else {
            // 排名4+，显示鼓励提示
            _rewardUiState.value = RewardUiState.ShowEncouragement(groupName)
        }
    }

    /**
     * 关闭奖励弹窗
     */
    fun dismissReward() {
        _rewardUiState.value = RewardUiState.Hidden
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            getApplication<Application>().unbindService(serviceConnection)
            bound = false
        }
    }
}
