package com.tinytimer.app.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tinytimer.app.data.dao.GroupDao
import com.tinytimer.app.data.dao.PrizeDao
import com.tinytimer.app.data.dao.RecordDao
import com.tinytimer.app.data.dao.TimerStateDao
import com.tinytimer.app.data.entity.GroupEntity
import com.tinytimer.app.data.entity.PrizeEntity
import com.tinytimer.app.data.entity.RecordEntity
import com.tinytimer.app.data.entity.TimerStateEntity

/**
 * 数据库迁移：从版本2到版本3，添加 prizes 表
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("AppDatabase", "执行迁移 2->3: 创建 prizes 表")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS prizes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                imagePath TEXT NOT NULL DEFAULT '',
                level INTEGER NOT NULL DEFAULT 1,
                boundGroupIds TEXT NOT NULL DEFAULT '[]',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

/**
 * 数据库迁移：从版本3到版本4，groups 表添加 qualificationDuration 列
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("AppDatabase", "执行迁移 3->4: groups 表添加 qualificationDuration 列")
        db.execSQL("ALTER TABLE groups ADD COLUMN qualificationDuration INTEGER DEFAULT NULL")
    }
}

/**
 * 数据库迁移：从版本1到版本3，直接创建所有表
 */
val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("AppDatabase", "执行迁移 1->3: 创建所有表")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `groups` (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color TEXT NOT NULL DEFAULT '#2196F3',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS records (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                groupId INTEGER,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                duration INTEGER NOT NULL DEFAULT 0,
                note TEXT,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS timer_states (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                groupId INTEGER NOT NULL,
                startTime INTEGER NOT NULL,
                pausedTime INTEGER NOT NULL DEFAULT 0,
                accumulatedTime INTEGER NOT NULL DEFAULT 0,
                isPaused INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS prizes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                imagePath TEXT NOT NULL DEFAULT '',
                level INTEGER NOT NULL DEFAULT 1,
                boundGroupIds TEXT NOT NULL DEFAULT '[]',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

/**
 * 数据库迁移：从版本2到版本4，创建 prizes 表 + groups 表添加 qualificationDuration 列
 */
val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("AppDatabase", "执行迁移 2->4: 创建 prizes 表 + 添加 qualificationDuration 列")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS prizes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                imagePath TEXT NOT NULL DEFAULT '',
                level INTEGER NOT NULL DEFAULT 1,
                boundGroupIds TEXT NOT NULL DEFAULT '[]',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE groups ADD COLUMN qualificationDuration INTEGER DEFAULT NULL")
    }
}

/**
 * 数据库迁移：从版本1到版本4，直接创建所有表（含 qualificationDuration 列）
 */
val MIGRATION_1_4 = object : Migration(1, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.d("AppDatabase", "执行迁移 1->4: 创建所有表（含 qualificationDuration）")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `groups` (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                color TEXT NOT NULL DEFAULT '#2196F3',
                createdAt INTEGER NOT NULL DEFAULT 0,
                qualificationDuration INTEGER DEFAULT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS records (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                groupId INTEGER,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                duration INTEGER NOT NULL DEFAULT 0,
                note TEXT,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS timer_states (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                groupId INTEGER NOT NULL,
                startTime INTEGER NOT NULL,
                pausedTime INTEGER NOT NULL DEFAULT 0,
                accumulatedTime INTEGER NOT NULL DEFAULT 0,
                isPaused INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS prizes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                imagePath TEXT NOT NULL DEFAULT '',
                level INTEGER NOT NULL DEFAULT 1,
                boundGroupIds TEXT NOT NULL DEFAULT '[]',
                createdAt INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities = [GroupEntity::class, PrizeEntity::class, RecordEntity::class, TimerStateEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun prizeDao(): PrizeDao
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
                )
                    .addMigrations(MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_1_4, MIGRATION_2_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}