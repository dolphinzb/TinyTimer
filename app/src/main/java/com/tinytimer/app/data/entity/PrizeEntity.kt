package com.tinytimer.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 奖品等级枚举
 */
enum class PrizeLevel(val value: Int, val displayName: String) {
    FIRST(1, "一等奖"),
    SECOND(2, "二等奖"),
    THIRD(3, "三等奖"),
    QUALIFIED(4, "合格奖");

    companion object {
        fun fromValue(value: Int): PrizeLevel {
            return entries.find { it.value == value } ?: FIRST
        }
    }
}

/**
 * 奖品实体
 */
@Entity(tableName = "prizes")
data class PrizeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val imagePath: String = "",
    val level: Int = PrizeLevel.FIRST.value,
    val boundGroupIds: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取奖品等级
     */
    fun getPrizeLevel(): PrizeLevel {
        return PrizeLevel.fromValue(level)
    }

    /**
     * 获取绑定的分组 ID 列表（自定义方法，避免与 Room 生成的方法冲突）
     */
    fun getBoundGroupIdsList(): List<Long> {
        return try {
            if (boundGroupIds.isBlank() || boundGroupIds == "[]") {
                emptyList()
            } else {
                boundGroupIds.removeSurrounding("[", "]")
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map { it.trim().toLong() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 设置绑定的分组 ID 列表（自定义方法，避免与 Room 生成的方法冲突）
     */
    fun setBoundGroupIdsList(groupIds: List<Long>): PrizeEntity {
        return copy(boundGroupIds = groupIds.joinToString(",", "[", "]"))
    }
}