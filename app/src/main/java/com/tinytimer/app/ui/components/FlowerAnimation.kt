package com.tinytimer.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 鲜花花瓣粒子数据
 */
private data class FlowerPetal(
    val startX: Float,
    val startY: Float,
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    var x: Float = startX,
    var y: Float = startY,
    var alpha: Float = 1f,
    val decay: Float = Random.nextFloat() * 0.015f + 0.008f
)

/**
 * 鲜花粒子动画组件
 * 用于第2-3名庆祝效果
 */
@Composable
fun FlowerAnimation(
    modifier: Modifier = Modifier,
    durationMs: Long = 2000
) {
    val petals = remember { mutableListOf<FlowerPetal>() }
    var isPlaying by remember { mutableStateOf(true) }

    // 初始化花瓣（<=50个）
    LaunchedEffect(Unit) {
        val colors = listOf(
            Color(0xFFFFB6C1), // 浅粉
            Color(0xFFFF69B4), // 粉色
            Color(0xFFFF1493), // 深粉
            Color(0xFFFFC0CB), // 粉红
            Color(0xFFFFE4E1), // 薄雾玫瑰
            Color(0xFFFFD700), // 金色点缀
        )
        repeat(40) {
            val startX = Random.nextFloat() * 0.6f + 0.2f
            val startY = -0.05f
            petals.add(
                FlowerPetal(
                    startX = startX,
                    startY = startY,
                    angle = Math.PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * 0.5f,
                    speed = Random.nextFloat() * 0.008f + 0.003f,
                    color = colors.random(),
                    size = Random.nextFloat() * 8f + 4f,
                    rotation = Random.nextFloat() * 360f
                )
            )
        }
        delay(durationMs)
        isPlaying = false
    }

    if (isPlaying) {
        Canvas(modifier = modifier) {
            petals.forEach { petal ->
                // 更新位置（飘落效果）
                petal.x += cos(petal.angle) * petal.speed + sin(petal.rotation * 0.01f) * 0.001f
                petal.y += sin(petal.angle) * petal.speed
                petal.alpha -= petal.decay
                if (petal.alpha < 0) petal.alpha = 0f

                if (petal.alpha > 0) {
                    drawPetal(
                        center = Offset(
                            petal.x * size.width,
                            petal.y * size.height
                        ),
                        size = petal.size,
                        color = petal.color.copy(alpha = petal.alpha)
                    )
                }
            }
        }
    }
}

/**
 * 绘制花瓣形状
 */
private fun DrawScope.drawPetal(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticBezierTo(
            center.x + size * 0.7f, center.y - size * 0.3f,
            center.x, center.y + size * 0.5f
        )
        quadraticBezierTo(
            center.x - size * 0.7f, center.y - size * 0.3f,
            center.x, center.y - size
        )
    }
    drawPath(
        path = path,
        color = color
    )
}
