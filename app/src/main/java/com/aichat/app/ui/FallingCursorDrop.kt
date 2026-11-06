package com.aichat.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aichat.app.data.AppPrefs
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 光标竖线在自身坐标里的位置（用于自绘渐隐光标）。 */
data class CaretInfo(val x: Float, val top: Float, val bottom: Float)

/**
 * 自己画一个“水滴”光标手柄：单击时光标处出现并下落（或原位淡出），
 * 长按 / 双击选词时改为显示系统选区手柄，不出现水滴。
 * 系统光标手柄弹层无法移动，所以光标手柄保持透明，由这里绘制。
 * 开启“光标渐隐渐现”时，也由这里绘制光标竖线。
 */
@Composable
fun FallingCursorDrop(
    modifier: Modifier = Modifier,
    dropOffset: ((tap: Offset, boxSize: IntSize) -> Offset)? = null,
    caretProvider: ((boxSize: IntSize) -> CaretInfo?)? = null,
    content: @Composable (handleColor: Color) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dropPos by remember { mutableStateOf<Offset?>(null) }
    var dropJob by remember { mutableStateOf<Job?>(null) }
    var falling by remember { mutableStateOf(true) }
    // 平时透明（隐藏光标手柄）；进入选区时用主色（显示选区手柄）
    var handleColor by remember { mutableStateOf<Color>(Color.Transparent) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val fadeEnabled = AppPrefs.cursorFadeEnabled(context)
    val caretInfo = if (fadeEnabled) caretProvider?.invoke(boxSize) else null
    val caretAlpha = remember { Animatable(0f) }
    val fall = remember { Animatable(0f) }
    val baseColors = LocalTextSelectionColors.current

    LaunchedEffect(caretInfo != null, fadeEnabled, dropJob) {
        if (caretInfo != null && fadeEnabled) {
            // 和水滴同时开始：每次点击都从 0 透明度重新渐显，
            // 不会以半透明/已显示的状态和水滴叠在一起
            while (true) {
                caretAlpha.snapTo(0f)
                // 0 → 1 约一秒，由快变慢
                caretAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearOutSlowInEasing
                    )
                )
                // 1 → 0 再慢慢消失
                caretAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = FastOutLinearInEasing
                    )
                )
            }
        } else {
            caretAlpha.snapTo(0f)
        }
    }

    CompositionLocalProvider(
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = handleColor,
            backgroundColor = baseColors.backgroundColor
        )
    ) {
        Box(
            modifier = modifier
                .onSizeChanged { boxSize = it }
                .pointerInput(Unit) {
                awaitPointerEventScope {
                    var down: Offset? = null
                    var moved = false
                    var downTime = 0L
                    var lastTapUp = 0L
                    var longPressJob: Job? = null
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.changedToDownIgnoreConsumed()) {
                            val isDoubleTap = change.uptimeMillis - lastTapUp <=
                                viewConfiguration.doubleTapTimeoutMillis
                            down = change.position
                            moved = false
                            downTime = change.uptimeMillis
                            // 双击选词时让选区手柄可见，普通点击保持透明
                            handleColor =
                                if (isDoubleTap) colors.primary else Color.Transparent
                            longPressJob?.cancel()
                            longPressJob = scope.launch {
                                delay(
                                    (viewConfiguration.longPressTimeoutMillis - 80)
                                        .coerceAtLeast(1L)
                                )
                                // 还按着且没移动：长按选择开始，提前把手柄切成主色
                                if (down != null && !moved) {
                                    handleColor = colors.primary
                                }
                            }
                        }
                        val start = down ?: continue
                        if ((change.position - start).getDistance() >
                            viewConfiguration.touchSlop
                        ) {
                            moved = true
                        }
                        if (change.changedToUpIgnoreConsumed()) {
                            val duration = change.uptimeMillis - downTime
                            val isDoubleTap = change.uptimeMillis - lastTapUp <=
                                viewConfiguration.doubleTapTimeoutMillis
                            longPressJob?.cancel()
                            down = null
                            // 只有短按才算“点击”；长按、双击选词、拖动都不出水滴
                            val isTap = !moved &&
                                duration < viewConfiguration.longPressTimeoutMillis &&
                                !isDoubleTap
                            if (isTap) {
                                dropPos = dropOffset?.invoke(change.position, size)
                                    ?: change.position
                                val withFall = AppPrefs.cursorDropEnabled(context)
                                falling = withFall
                                dropJob?.cancel()
                                dropJob = scope.launch {
                                    fall.snapTo(0f)
                                    fall.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = if (withFall) 650 else 450,
                                            easing = LinearEasing
                                        )
                                    )
                                }
                            }
                            lastTapUp = change.uptimeMillis
                        }
                    }
                }
            }
        ) {
            content(handleColor)
            Canvas(Modifier.matchParentSize()) {
                val caret = caretInfo
                if (caret != null && caretAlpha.value > 0.01f) {
                    // 与系统光标尺寸一致：宽 2dp、上下端平齐
                    val width = 2.dp.toPx()
                    drawRect(
                        color = colors.primary.copy(alpha = caretAlpha.value),
                        topLeft = Offset(caret.x, caret.top),
                        size = Size(width, caret.bottom - caret.top)
                    )
                }
                val position = dropPos
                if (position != null) {
                    drawFallingDrop(
                        x = position.x,
                        targetTop = position.y,
                        progress = fall.value,
                        color = colors.primary,
                        animated = falling
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFallingDrop(
    x: Float,
    targetTop: Float,
    progress: Float,
    color: Color,
    animated: Boolean
) {
    if (!animated) {
        // 关闭下落动画：原地从 1 淡到 0
        drawDropShape(x = x, top = targetTop, alpha = 1f - progress, color = color)
        return
    }

    val rise = 14.dp.toPx()
    val fallBelow = 24.dp.toPx()
    val total = rise + fallBelow
    val peakAt = rise / total
    // 从目标上方落下，经过目标位置后继续下落
    val y = targetTop - rise + total * progress
    // 0 → 1 → 0，在到达目标位置时透明度最大
    val alpha = if (progress <= peakAt) {
        progress / peakAt
    } else {
        (1f - progress) / (1f - peakAt)
    }.coerceIn(0f, 1f)
    val stretch = 1f + progress * 0.35f
    val squeeze = 1f - progress * 0.1f

    scale(scaleX = squeeze, scaleY = stretch, pivot = Offset(x, y)) {
        drawDropShape(x = x, top = y, alpha = alpha, color = color)
    }
}

private fun DrawScope.drawDropShape(
    x: Float,
    top: Float,
    alpha: Float,
    color: Color
) {
    val radius = 6.5.dp.toPx()
    val centerY = top + radius * 1.9f
    val bodyRect = Rect(
        left = x - radius,
        top = top + radius * 0.9f,
        right = x + radius,
        bottom = top + radius * 2.9f
    )
    // 单一路径画完整水滴，避免两笔叠加导致半透明时出现分层
    val path = Path().apply {
        moveTo(x, top)
        cubicTo(
            x - radius * 0.06f, top + radius * 0.9f,
            x - radius, top + radius * 1.2f,
            x - radius, centerY
        )
        arcTo(bodyRect, startAngleDegrees = 180f, sweepAngleDegrees = -180f, forceMoveTo = false)
        cubicTo(
            x + radius, top + radius * 1.2f,
            x + radius * 0.06f, top + radius * 0.9f,
            x, top
        )
        close()
    }

    val light = lerp(Color.White, color, 0.42f)
    val dark = lerp(color, Color(0xFF006A93), 0.48f)
    // 中心浅、边缘深，像水珠的透光感
    val bodyBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0f to Color(0xFFF2FDFF),
            0.5f to light,
            0.82f to color,
            1f to dark
        ),
        center = Offset(x, top + radius * 2.05f),
        radius = radius * 1.85f
    )
    val a = alpha.coerceIn(0f, 1f)
    drawPath(path = path, brush = bodyBrush, alpha = a)

    clipPath(path) {
        // 左上沿边的月牙反光
        val arcRadius = radius * 0.74f
        drawArc(
            color = Color.White.copy(alpha = 0.78f),
            startAngle = 132f,
            sweepAngle = 82f,
            useCenter = false,
            topLeft = Offset(x - arcRadius, centerY - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = radius * 0.22f, cap = StrokeCap.Round),
            alpha = a
        )
        // 右下的小亮点
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = radius * 0.13f,
            center = Offset(x + radius * 0.34f, top + radius * 1.5f),
            alpha = a
        )
    }
}
