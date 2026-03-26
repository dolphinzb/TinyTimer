package com.tinytimer.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分组实体
 */
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long = 0xFF2196F3,
    val createdAt: Long = System.currentTimeMillis()
)
