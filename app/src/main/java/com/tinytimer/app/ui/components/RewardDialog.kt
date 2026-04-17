package com.tinytimer.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinytimer.app.R
import com.tinytimer.app.data.model.RewardInfo
import com.tinytimer.app.data.model.SummaryRewardItem
import com.tinytimer.app.data.model.TimerRanking
import com.tinytimer.app.ui.viewmodel.RewardUiState
import java.io.File

/**
 * 奖励弹窗组件
 * 根据排名状态显示不同的弹窗内容
 */
@Composable
fun RewardDialog(
    state: RewardUiState,
    onDismiss: () -> Unit,
    formatTime: (Long) -> String
) {
    when (state) {
        is RewardUiState.Hidden -> return
        is RewardUiState.ShowRanking -> {
            RewardRankingDialog(
                ranking = state.ranking,
                rewardInfo = state.rewardInfo,
                onDismiss = onDismiss,
                formatTime = formatTime
            )
        }
        is RewardUiState.ShowQualified -> {
            QualifiedDialog(
                groupName = state.groupName,
                qualificationDuration = state.qualificationDuration,
                currentDuration = state.currentDuration,
                rewardInfo = state.rewardInfo,
                onDismiss = onDismiss,
                formatTime = formatTime
            )
        }
        is RewardUiState.ShowEncouragement -> {
            EncouragementDialog(
                groupName = state.groupName,
                qualificationDuration = state.qualificationDuration,
                currentDuration = state.currentDuration,
                onDismiss = onDismiss,
                formatTime = formatTime
            )
        }
        is RewardUiState.ShowSummary -> {
            SummaryRewardDialog(
                items = state.items,
                onDismiss = onDismiss,
                formatTime = formatTime
            )
        }
    }
}

/**
 * 排名弹窗（前三名）
 */
@Composable
private fun RewardRankingDialog(
    ranking: TimerRanking,
    rewardInfo: RewardInfo?,
    onDismiss: () -> Unit,
    formatTime: (Long) -> String
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 庆祝动效层
        if (ranking.rank == 1) {
            FireworkAnimation(modifier = Modifier.fillMaxSize())
        } else {
            FlowerAnimation(modifier = Modifier.fillMaxSize())
        }

        // 弹窗内容
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(24.dp),
            title = null,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 排名图标（金银铜奖杯）
                    RankIcon(rank = ranking.rank)

                    Spacer(modifier = Modifier.height(12.dp))

                    // 排名文字
                    Text(
                        text = "第${ranking.rank}名",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (ranking.rank) {
                            1 -> Color(0xFFFFD700) // 金色
                            2 -> Color(0xFFC0C0C0) // 银色
                            else -> Color(0xFFCD7F32) // 铜色
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 分组名和时长
                    Text(
                        text = "${ranking.groupName} · ${formatTime(ranking.currentDuration)}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 奖品信息
                    if (rewardInfo != null && rewardInfo.prizeName != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "获得奖品",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 奖品图标
                        PrizeImage(
                            imagePath = rewardInfo.prizeImagePath,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = rewardInfo.prizeName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("知道了")
                }
            }
        )
    }
}

/**
 * 鼓励弹窗（时长超过合格线或排名4+且无合格线）
 */
@Composable
private fun EncouragementDialog(
    groupName: String,
    qualificationDuration: Long?,
    currentDuration: Long?,
    onDismiss: () -> Unit,
    formatTime: (Long) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "继续加油！",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                if (qualificationDuration != null && currentDuration != null) {
                    // 有合格线信息时显示未达到提示
                    Text(
                        text = "$groupName 未达到合格线 ${formatTime(qualificationDuration)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "$groupName 再接再厉，争取下次上榜！",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

/**
 * 合格奖弹窗（排名4+且时长低于合格线）
 */
@Composable
private fun QualifiedDialog(
    groupName: String,
    qualificationDuration: Long,
    currentDuration: Long,
    rewardInfo: RewardInfo?,
    onDismiss: () -> Unit,
    formatTime: (Long) -> String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 合格图标（绿色勾选）
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "达标",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .padding(12.dp)
                            .size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 达标文字
                Text(
                    text = "达标！",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 分组名和时长
                Text(
                    text = "$groupName · ${formatTime(currentDuration)}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 合格线说明
                Text(
                    text = "达到合格线 ${formatTime(qualificationDuration)}",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                )

                // 奖品信息
                if (rewardInfo != null && rewardInfo.prizeName != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "获得奖品",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PrizeImage(
                        imagePath = rewardInfo.prizeImagePath,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = rewardInfo.prizeName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

/**
 * 排名图标（金银铜奖杯）
 */
@Composable
private fun RankIcon(rank: Int) {
    val tint = when (rank) {
        1 -> Color(0xFFFFD700) // 金色
        2 -> Color(0xFFC0C0C0) // 银色
        else -> Color(0xFFCD7F32) // 铜色
    }
    Surface(
        shape = CircleShape,
        color = tint.copy(alpha = 0.15f),
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "第${rank}名",
            tint = tint,
            modifier = Modifier
                .padding(12.dp)
                .size(40.dp)
        )
    }
}

/**
 * 奖品图片加载
 * 使用 BitmapFactory.decodeFile() 从本地路径加载
 */
@Composable
private fun PrizeImage(
    imagePath: String?,
    modifier: Modifier = Modifier
) {
    val imageBitmap = remember(imagePath) {
        if (!imagePath.isNullOrEmpty() && File(imagePath).exists()) {
            BitmapFactory.decodeFile(imagePath)
        } else null
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap.asImageBitmap(),
            contentDescription = "奖品图片",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // 占位图：礼物盒图标
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_reward_placeholder),
                contentDescription = "奖品占位图",
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 汇总弹窗（多分组同时计时全部停止后）
 * 同时展示所有分组的排名及奖励情况
 */
@Composable
private fun SummaryRewardDialog(
    items: List<SummaryRewardItem>,
    onDismiss: () -> Unit,
    formatTime: (Long) -> String
) {
    // 判断庆祝动效：有第1名→烟火，有第2-3名但无第1名→鲜花
    val hasFirstPlace = items.any { it.rank == 1 }
    val hasTop3 = items.any { it.rank != null && it.rank <= 3 }

    Box(modifier = Modifier.fillMaxSize()) {
        // 庆祝动效层
        if (hasFirstPlace) {
            FireworkAnimation(modifier = Modifier.fillMaxSize())
        } else if (hasTop3) {
            FlowerAnimation(modifier = Modifier.fillMaxSize())
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "计时结果",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items, key = { it.groupId }) { item ->
                        SummaryRewardCard(
                            item = item,
                            formatTime = formatTime
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("知道了")
                }
            }
        )
    }
}

/**
 * 汇总弹窗中单个分组的卡片
 */
@Composable
private fun SummaryRewardCard(
    item: SummaryRewardItem,
    formatTime: (Long) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：状态标签（排名徽章 / 达标标签 / 鼓励文字）
            when {
                item.rank != null -> {
                    // 前三名排名徽章
                    val tint = when (item.rank) {
                        1 -> Color(0xFFFFD700) // 金色
                        2 -> Color(0xFFC0C0C0) // 银色
                        else -> Color(0xFFCD7F32) // 铜色
                    }
                    Surface(
                        shape = CircleShape,
                        color = tint.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${item.rank}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = tint
                            )
                        }
                    }
                }
                item.isQualified -> {
                    // 达标标签
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "达标",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                else -> {
                    // 鼓励状态
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "💪",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 中间：分组名、时长、状态文字
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.groupName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(item.currentDuration),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 达标时提示
                if (item.isQualified) {
                    Text(
                        text = "达到合格线 ${formatTime(item.qualificationDuration ?: 0)}",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50).copy(alpha = 0.8f)
                    )
                }
                // 超过合格线时提示
                if (item.exceedsQualification) {
                    Text(
                        text = "未达到合格线 ${formatTime(item.qualificationDuration ?: 0)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
                // 未进前三且未达标时鼓励
                if (item.rank == null && !item.isQualified && !item.exceedsQualification) {
                    Text(
                        text = "继续加油！",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // 右侧：奖品图标（如有）
            if (item.rewardInfo != null && item.rewardInfo.prizeName != null) {
                PrizeImage(
                    imagePath = item.rewardInfo.prizeImagePath,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
