package com.tinytimer.app.data.dao

import androidx.room.*
import com.tinytimer.app.data.entity.PrizeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 奖品数据访问接口
 */
@Dao
interface PrizeDao {
    @Query("SELECT * FROM prizes ORDER BY level ASC, createdAt DESC")
    fun getAllPrizes(): Flow<List<PrizeEntity>>

    @Query("SELECT * FROM prizes ORDER BY level ASC, createdAt DESC")
    suspend fun getAllPrizesOnce(): List<PrizeEntity>

    @Query("SELECT * FROM prizes WHERE id = :id")
    suspend fun getPrizeById(id: Long): PrizeEntity?

    @Query("SELECT * FROM prizes WHERE level = :level ORDER BY createdAt DESC")
    fun getPrizesByLevel(level: Int): Flow<List<PrizeEntity>>

    @Query("SELECT * FROM prizes WHERE boundGroupIds LIKE '%' || :groupId || '%' ORDER BY level ASC, createdAt DESC")
    fun getPrizesByGroupId(groupId: Long): Flow<List<PrizeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prize: PrizeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prizes: List<PrizeEntity>)

    @Update
    suspend fun update(prize: PrizeEntity)

    @Delete
    suspend fun delete(prize: PrizeEntity)

    @Query("DELETE FROM prizes WHERE id = :id")
    suspend fun deleteById(id: Long)
}