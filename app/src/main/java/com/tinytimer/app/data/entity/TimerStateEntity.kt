package com.tinytimer.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 计时状态实体，用于保存应用重启后需要恢复的计时状态
 */
@Entity(tableName = "timer_state")
data class TimerStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val startTime: Long,
    val accumulatedTime: Long,
    val isRunning: Boolean,
    val groupId: Long?,
    val isPaused: Boolean = false,
    val pausedAt: Long? = null
)
