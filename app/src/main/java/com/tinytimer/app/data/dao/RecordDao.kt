package com.tinytimer.app.data.dao

import androidx.room.*
import com.tinytimer.app.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY startTime DESC")
    fun getAllRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records ORDER BY startTime DESC")
    suspend fun getAllRecordsOnce(): List<RecordEntity>

    @Query("SELECT * FROM records WHERE groupId = :groupId ORDER BY startTime DESC")
    fun getRecordsByGroup(groupId: Long): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE startTime >= :startOfDay AND startTime < :endOfDay ORDER BY startTime DESC")
    fun getRecordsByDate(startOfDay: Long, endOfDay: Long): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordEntity): Long

    @Update
    suspend fun update(record: RecordEntity)

    @Delete
    suspend fun delete(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM records ORDER BY duration ASC LIMIT 10")
    fun getTop10ShortestRecords(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE groupId = :groupId ORDER BY duration ASC LIMIT 10")
    fun getTop10ShortestRecordsByGroup(groupId: Long): Flow<List<RecordEntity>>
}
