package com.tinytimer.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tinytimer.app.data.dao.GroupDao
import com.tinytimer.app.data.dao.RecordDao
import com.tinytimer.app.data.dao.TimerStateDao
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.entity.TimerStateEntity

@Database(
    entities = [GroupEntity::class, RecordEntity::class, TimerStateEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun recordDao(): RecordDao
    abstract fun timerStateDao(): TimerStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timer_record_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
