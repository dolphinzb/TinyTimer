package com.tinytimer.app.data.model

/**
 * 奖励信息数据类
 * @param rank 排名（1-3为前三名）
 * @param prizeName 奖品名称
 * @param prizeImagePath 奖品图片本地路径
 */
data class RewardInfo(
    val rank: Int,
    val prizeName: String?,
    val prizeImagePath: String?
)
