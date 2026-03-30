package com.tinytimer.app.data.repository

import com.tinytimer.app.data.dao.GroupDao
import com.tinytimer.app.data.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

class GroupRepository(private val groupDao: GroupDao) {
    fun getAllGroups(): Flow<List<GroupEntity>> = groupDao.getAllGroups()

    suspend fun getGroupById(id: Long): GroupEntity? = groupDao.getGroupById(id)

    suspend fun insertGroup(group: GroupEntity): Long = groupDao.insert(group)

    suspend fun updateGroup(group: GroupEntity) = groupDao.update(group)

    suspend fun deleteGroup(group: GroupEntity) = groupDao.delete(group)

    suspend fun deleteGroupById(id: Long) = groupDao.deleteById(id)

    suspend fun insertAll(groups: List<GroupEntity>) = groupDao.insertAll(groups)

    fun exportToCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("id,name,color,createdAt")
        return sb.toString()
    }

    suspend fun exportGroupsToCsv(): String {
        val groups = mutableListOf<GroupEntity>()
        groupDao.getAllGroupsOnce().forEach { groups.add(it) }
        val sb = StringBuilder()
        sb.appendLine("id,name,color,createdAt")
        groups.forEach { group ->
            sb.appendLine("${group.id},\"${group.name.replace("\"", "\"\"")}\",${group.color},${group.createdAt}")
        }
        return sb.toString()
    }

    suspend fun importFromCsv(csvContent: String): Result<Int> {
        return try {
            val lines = csvContent.trim().lines()
            if (lines.isEmpty()) {
                return Result.success(0)
            }
            val dataLines = if (lines.first().startsWith("id,")) lines.drop(1) else lines
            var importedCount = 0
            for (line in dataLines) {
                if (line.isBlank()) continue
                val parts = parseCsvLine(line)
                if (parts.size >= 3) {
                    val name = parts[1]
                    val color = parts[2].toLongOrNull() ?: 0xFF2196F3
                    val createdAt = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                    val group = GroupEntity(name = name, color = color, createdAt = createdAt)
                    groupDao.insert(group)
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
