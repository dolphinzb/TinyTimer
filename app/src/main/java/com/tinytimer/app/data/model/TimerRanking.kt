package com.tinytimer.app.data.model

/**
 * 计时排名数据类
 * @param groupId 分组ID
 * @param groupName 分组名称
 * @param currentDuration 当次计时时长（毫秒）
 * @param rank 排名（1-based，时长越短排名越高）
 */
data class TimerRanking(
    val groupId: Long,
    val groupName: String,
    val currentDuration: Long,
    val rank: Int
)
