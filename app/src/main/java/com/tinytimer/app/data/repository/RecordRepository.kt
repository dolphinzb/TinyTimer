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

    suspend fun exportToCsv(): String {
        val records = recordDao.getAllRecordsOnce()
        val sb = StringBuilder()
        sb.appendLine("id,groupId,startTime,endTime,duration,note,createdAt")
        records.forEach { record ->
            val note = record.note?.replace("\"", "\"\"") ?: ""
            sb.appendLine("${record.id},${record.groupId ?: ""},${record.startTime},${record.endTime ?: ""},${record.duration},\"$note\",${record.createdAt}")
        }
        return sb.toString()
    }

    suspend fun importFromCsv(csvContent: String): Result<Int> {
        return try {
            val lines = csvContent.trim().lines()
            if (lines.isEmpty()) return Result.success(0)
            val dataLines = if (lines.first().startsWith("id,")) lines.drop(1) else lines
            var importedCount = 0
            for (line in dataLines) {
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.size >= 5) {
                    val groupId = parts[1].toLongOrNull()
                    val startTime = parts[2].toLongOrNull() ?: continue
                    val endTime = parts.getOrNull(3)?.toLongOrNull()
                    val duration = parts[4].toLongOrNull() ?: continue
                    val note = parts.getOrNull(5)
                    val createdAt = parts.getOrNull(6)?.toLongOrNull() ?: System.currentTimeMillis()
                    val record = RecordEntity(
                        groupId = groupId,
                        startTime = startTime,
                        endTime = endTime,
                        duration = duration,
                        note = note,
                        createdAt = createdAt
                    )
                    recordDao.insert(record)
                    importedCount++
                }
            }
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '"' -> {
                    if (inQuotes && current.length > 0 && current[current.length - 1] == '"') {
                        current.append('"')
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
