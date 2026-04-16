package com.tinytimer.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 烟花粒子数据
 */
private data class FireworkParticle(
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    var x: Float = startX,
    var y: Float = startY,
    var alpha: Float = 1f,
    val decay: Float = Random.nextFloat() * 0.02f + 0.01f
)

/**
 * 烟花粒子动画组件
 * 用于第1名庆祝效果
 */
@Composable
fun FireworkAnimation(
    modifier: Modifier = Modifier,
    durationMs: Long = 2000
) {
    val particles = remember { mutableListOf<FireworkParticle>() }
    var isPlaying by remember { mutableStateOf(true) }

    // 初始化粒子（<=50个）
    LaunchedEffect(Unit) {
        val centerX = 0.5f
        val centerY = 0.4f
        val colors = listOf(
            Color(0xFFFFD700), // 金色
            Color(0xFFFF6347), // 红色
            Color(0xFF00CED1), // 青色
            Color(0xFFFF69B4), // 粉色
            Color(0xFF7CFC00), // 绿色
            Color(0xFFFFA500), // 橙色
        )
        repeat(50) {
            particles.add(
                FireworkParticle(
                    startX = centerX,
                    startY = centerY,
                    angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                    speed = Random.nextFloat() * 0.015f + 0.005f,
                    color = colors.random(),
                    size = Random.nextFloat() * 4f + 2f
                )
            )
        }
        delay(durationMs)
        isPlaying = false
    }

    if (isPlaying) {
        Canvas(modifier = modifier) {
            particles.forEach { particle ->
                // 更新位置
                particle.x += cos(particle.angle) * particle.speed
                particle.y += sin(particle.angle) * particle.speed
                particle.alpha -= particle.decay
                if (particle.alpha < 0) particle.alpha = 0f

                if (particle.alpha > 0) {
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        radius = particle.size,
                        center = Offset(
                            particle.x * size.width,
                            particle.y * size.height
                        )
                    )
                }
            }
        }
    }
}
