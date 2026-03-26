package com.tinytimer.app.data.repository

import com.tinytimer.app.data.dao.TimerStateDao
import com.tinytimer.app.data.entity.TimerStateEntity

class TimerStateRepository(private val timerStateDao: TimerStateDao) {
    suspend fun getTimerState(): TimerStateEntity? = timerStateDao.getTimerState()

    suspend fun saveTimerState(state: TimerStateEntity) = timerStateDao.saveTimerState(state)

    suspend fun clearTimerState() = timerStateDao.clearTimerState()
}
