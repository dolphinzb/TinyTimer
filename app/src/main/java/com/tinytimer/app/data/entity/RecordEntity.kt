package com.tinytimer.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 计时记录实体
 */
@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("groupId"), Index("startTime")]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long?,
    val startTime: Long,
    val endTime: Long?,
    val duration: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
