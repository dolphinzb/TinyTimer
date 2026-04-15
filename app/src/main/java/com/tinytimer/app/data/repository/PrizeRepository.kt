package com.tinytimer.app.data.repository

import com.tinytimer.app.data.dao.PrizeDao
import com.tinytimer.app.data.entity.PrizeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 奖品数据仓库
 */
class PrizeRepository(private val prizeDao: PrizeDao) {
    fun getAllPrizes(): Flow<List<PrizeEntity>> = prizeDao.getAllPrizes()

    suspend fun getAllPrizesOnce(): List<PrizeEntity> = prizeDao.getAllPrizesOnce()

    suspend fun getPrizeById(id: Long): PrizeEntity? = prizeDao.getPrizeById(id)

    fun getPrizesByLevel(level: Int): Flow<List<PrizeEntity>> = prizeDao.getPrizesByLevel(level)

    fun getPrizesByGroupId(groupId: Long): Flow<List<PrizeEntity>> = prizeDao.getPrizesByGroupId(groupId)

    suspend fun insertPrize(prize: PrizeEntity): Long = prizeDao.insert(prize)

    suspend fun updatePrize(prize: PrizeEntity) = prizeDao.update(prize)

    suspend fun deletePrize(prize: PrizeEntity) = prizeDao.delete(prize)

    suspend fun deletePrizeById(id: Long) = prizeDao.deleteById(id)

    suspend fun insertAll(prizes: List<PrizeEntity>) = prizeDao.insertAll(prizes)
}