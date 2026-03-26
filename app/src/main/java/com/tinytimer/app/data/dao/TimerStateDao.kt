package com.tinytimer.app.data.dao

import androidx.room.*
import com.tinytimer.app.data.entity.TimerStateEntity

@Dao
interface TimerStateDao {
    @Query("SELECT * FROM timer_state WHERE id = 1")
    suspend fun getTimerState(): TimerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTimerState(state: TimerStateEntity)

    @Query("DELETE FROM timer_state")
    suspend fun clearTimerState()
}
