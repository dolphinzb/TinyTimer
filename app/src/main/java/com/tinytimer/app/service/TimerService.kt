package com.tinytimer.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tinytimer.app.R
import com.tinytimer.app.TinyTimerApp
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.entity.TimerStateEntity
import com.tinytimer.app.data.repository.RecordRepository
import com.tinytimer.app.data.repository.TimerStateRepository
import com.tinytimer.app.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimerService : Service() {

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var recordRepository: RecordRepository
    private lateinit var timerStateRepository: TimerStateRepository

    private var startTime: Long = 0
    private var accumulatedTime: Long = 0
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    private var pausedAt: Long = 0
    private var currentGroupId: Long? = null

    private var timerJob: Job? = null

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        recordRepository = RecordRepository(TinyTimerApp.instance.database.recordDao())
        timerStateRepository = TimerStateRepository(TinyTimerApp.instance.database.timerStateDao())
        restoreTimerState()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> requestStop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        saveTimerState()
        serviceScope.cancel()
    }

    fun startTimer(groupId: Long? = null) {
        if (isRunning) return
        currentGroupId = groupId
        startTime = System.currentTimeMillis()
        accumulatedTime = 0
        isRunning = true
        isPaused = false
        startForeground(TinyTimerApp.NOTIFICATION_ID, createNotification())
        startTimerJob()
        saveTimerState()
    }

    fun pauseTimer() {
        if (!isRunning || isPaused) return
        isPaused = true
        pausedAt = System.currentTimeMillis()
        accumulatedTime += pausedAt!! - startTime
        timerJob?.cancel()
        _timerState.value = TimerState(
            startTime = startTime,
            accumulatedTime = accumulatedTime,
            isRunning = isRunning,
            isPaused = isPaused,
            groupId = currentGroupId
        )
        updateNotification()
        saveTimerState()
    }

    fun resumeTimer() {
        if (!isRunning || !isPaused) return
        isPaused = false
        startTime = System.currentTimeMillis()
        startTimerJob()
        _timerState.value = TimerState(
            startTime = startTime,
            accumulatedTime = accumulatedTime,
            isRunning = isRunning,
            isPaused = isPaused,
            groupId = currentGroupId
        )
        updateNotification()
        saveTimerState()
    }

    fun requestStop() {
        if (!isRunning) return
        timerJob?.cancel()
        if (!isPaused) {
            accumulatedTime += System.currentTimeMillis() - startTime
        }
        isRunning = false
        _elapsedTime.value = accumulatedTime
        _showSaveDialog.value = true
    }

    fun markAndSave() {
        if (!isRunning) return
        val currentElapsed = if (isPaused) {
            accumulatedTime
        } else {
            accumulatedTime + (System.currentTimeMillis() - startTime)
        }
        val endTime = System.currentTimeMillis()
        val markStartTime = endTime - currentElapsed
        serviceScope.launch {
            val record = RecordEntity(
                groupId = currentGroupId,
                startTime = markStartTime,
                endTime = endTime,
                duration = currentElapsed,
                note = null
            )
            recordRepository.insertRecord(record)
        }
    }

    fun quickStop() {
        if (!isRunning) return
        timerJob?.cancel()
        if (!isPaused) {
            accumulatedTime += System.currentTimeMillis() - startTime
        }
        val endTime = System.currentTimeMillis()
        val totalTime = accumulatedTime
        serviceScope.launch {
            val record = RecordEntity(
                groupId = currentGroupId,
                startTime = startTime,
                endTime = endTime,
                duration = totalTime,
                note = null
            )
            recordRepository.insertRecord(record)
        }
        doStop()
    }

    fun stopImmediately() {
        if (!isRunning) return
        timerJob?.cancel()
        doStop()
    }

    private fun doStop() {
        isRunning = false
        isPaused = false
        accumulatedTime = 0
        _elapsedTime.value = 0
        _timerState.value = TimerState()
        serviceScope.launch {
            timerStateRepository.clearTimerState()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun confirmStop(saveRecord: Boolean, note: String? = null) {
        _showSaveDialog.value = false

        val endTime = System.currentTimeMillis()
        val totalTime = accumulatedTime

        if (saveRecord) {
            serviceScope.launch {
                val record = RecordEntity(
                    groupId = currentGroupId,
                    startTime = startTime,
                    endTime = endTime,
                    duration = totalTime,
                    note = note
                )
                recordRepository.insertRecord(record)
            }
        }

        doStop()
    }

    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                val currentElapsed = if (isPaused) {
                    accumulatedTime
                } else {
                    accumulatedTime + (System.currentTimeMillis() - startTime)
                }
                _elapsedTime.value = currentElapsed
                _timerState.value = TimerState(
                    startTime = startTime,
                    accumulatedTime = accumulatedTime,
                    isRunning = isRunning,
                    isPaused = isPaused,
                    groupId = currentGroupId
                )
                delay(100)
            }
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeIntent = Intent(this, TimerService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this, 1, pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = formatTime(_elapsedTime.value)
        val pauseResumeText = if (isPaused) getString(R.string.action_resume) else getString(R.string.action_pause)

        return NotificationCompat.Builder(this, TinyTimerApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(timeText)
            .setContentText(if (isPaused) "已暂停" else "计时中...")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .addAction(0, pauseResumeText, pauseResumePendingIntent)
            .addAction(0, getString(R.string.action_stop), stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(TinyTimerApp.NOTIFICATION_ID, notification)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun saveTimerState() {
        serviceScope.launch {
            val state = TimerStateEntity(
                startTime = startTime,
                accumulatedTime = accumulatedTime,
                isRunning = isRunning,
                groupId = currentGroupId,
                isPaused = isPaused,
                pausedAt = pausedAt
            )
            timerStateRepository.saveTimerState(state)
        }
    }

    private fun restoreTimerState() {
        serviceScope.launch {
            val state = timerStateRepository.getTimerState() ?: return@launch
            if (!state.isRunning) return@launch

            currentGroupId = state.groupId
            accumulatedTime = state.accumulatedTime
            isPaused = state.isPaused

            if (isPaused) {
                pausedAt = state.pausedAt ?: System.currentTimeMillis()
                startTime = state.startTime
                _elapsedTime.value = accumulatedTime
                isRunning = true
            } else {
                startTime = state.startTime
                val elapsed = accumulatedTime + (System.currentTimeMillis() - startTime)
                _elapsedTime.value = elapsed
                isRunning = true
                startTimerJob()
            }

            _timerState.value = TimerState(
                startTime = state.startTime,
                accumulatedTime = state.accumulatedTime,
                isRunning = isRunning,
                isPaused = isPaused,
                groupId = currentGroupId
            )

            startForeground(TinyTimerApp.NOTIFICATION_ID, createNotification())
        }
    }

    companion object {
        const val ACTION_START = "com.tinytimer.ACTION_START"
        const val ACTION_PAUSE = "com.tinytimer.ACTION_PAUSE"
        const val ACTION_RESUME = "com.tinytimer.ACTION_RESUME"
        const val ACTION_STOP = "com.tinytimer.ACTION_STOP"
    }
}

data class TimerState(
    val startTime: Long = 0,
    val accumulatedTime: Long = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val groupId: Long? = null
)
