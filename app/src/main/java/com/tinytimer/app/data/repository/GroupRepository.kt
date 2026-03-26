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
}
