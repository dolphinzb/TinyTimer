package com.tinytimer.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
        is RewardUiState.ShowEncouragement -> {
            EncouragementDialog(
                groupName = state.groupName,
                onDismiss = onDismiss
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
 * 鼓励弹窗（排名4+）
 */
@Composable
private fun EncouragementDialog(
    groupName: String,
    onDismiss: () -> Unit
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
            Text(
                text = "$groupName 再接再厉，争取下次上榜！",
                style = MaterialTheme.typography.bodyLarge
            )
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
