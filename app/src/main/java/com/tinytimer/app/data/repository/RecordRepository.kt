package com.tinytimer.app.data.repository

import com.tinytimer.app.data.dao.RecordDao
import com.tinytimer.app.data.entity.RecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class RecordRepository(private val recordDao: RecordDao) {
    fun getAllRecords(): Flow<List<RecordEntity>> = recordDao.getAllRecords()

    fun getRecordsByGroup(groupId: Long): Flow<List<RecordEntity>> =
        recordDao.getRecordsByGroup(groupId)

    fun getRecordsByDate(date: Long): Flow<List<RecordEntity>> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        return recordDao.getRecordsByDate(startOfDay, endOfDay)
    }

    suspend fun getRecordById(id: Long): RecordEntity? = recordDao.getRecordById(id)

    suspend fun insertRecord(record: RecordEntity): Long = recordDao.insert(record)

    suspend fun updateRecord(record: RecordEntity) = recordDao.update(record)

    suspend fun deleteRecord(record: RecordEntity) = recordDao.delete(record)

    suspend fun deleteRecordById(id: Long) = recordDao.deleteById(id)

    suspend fun deleteRecordsByIds(ids: List<Long>) = recordDao.deleteByIds(ids)

    fun getTop10ShortestRecords(): Flow<List<RecordEntity>> = recordDao.getTop10ShortestRecords()

    fun getTop10ShortestRecordsByGroup(groupId: Long): Flow<List<RecordEntity>> = recordDao.getTop10ShortestRecordsByGroup(groupId)
}
