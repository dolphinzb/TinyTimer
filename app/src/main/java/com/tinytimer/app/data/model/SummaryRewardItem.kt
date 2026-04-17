package com.tinytimer.app.data.model

/**
 * 汇总弹窗中单个分组的奖励结果
 * @param groupId 分组ID
 * @param groupName 分组名称
 * @param currentDuration 当次计时时长（毫秒）
 * @param rank 排名（前三名有值，否则为 null）
 * @param rewardInfo 奖品信息（有对应奖品时有值）
 * @param isQualified 是否达标（排名4+且时长低于合格线）
 * @param exceedsQualification 是否超过合格线（时长超过合格线，不论排名）
 * @param qualificationDuration 合格线时长（毫秒），null 表示未设置
 */
data class SummaryRewardItem(
    val groupId: Long,
    val groupName: String,
    val currentDuration: Long,
    val rank: Int?,
    val rewardInfo: RewardInfo?,
    val isQualified: Boolean,
    val exceedsQualification: Boolean,
    val qualificationDuration: Long?
)
