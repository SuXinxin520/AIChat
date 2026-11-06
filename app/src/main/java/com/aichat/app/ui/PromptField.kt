package com.aichat.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.data.AppPrefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 固定高度的多行输入框：内容超出时在框内滚动。
 * 右侧滚动条可拖动快速定位，且只在滚动时显示。
 */
@Composable
fun PromptField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val fadeEnabled = AppPrefs.cursorFadeEnabled(context)
    var focused by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(12.dp)

    // BasicTextField 聚焦时会把光标（默认在文本开头）带进视野，
    // 长文本下会把框内滚动拉回顶部。这里自己维护选区：
    // 没聚焦时选区跟着可见区域走，点进输入框就不会跳。
    var textValue by remember { mutableStateOf(TextFieldValue(value)) }
    var textLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var viewportHeight by remember { mutableIntStateOf(0) }

    if (textValue.text != value) {
        textValue = textValue.copy(
            text = value,
            selection = TextRange(textValue.selection.end.coerceIn(0, value.length))
        )
    }

    LaunchedEffect(scrollState.value, focused, textLayout, viewportHeight) {
        if (focused) return@LaunchedEffect
        val layout = textLayout ?: return@LaunchedEffect
        if (viewportHeight <= 0) return@LaunchedEffect
        val y = (scrollState.value + viewportHeight / 2f)
            .coerceIn(0f, (layout.size.height - 1).toFloat())
        val offset = layout.getOffsetForPosition(Offset(0f, y))
            .coerceIn(0, textValue.text.length)
        if (textValue.selection.start != offset || !textValue.selection.collapsed) {
            textValue = textValue.copy(selection = TextRange(offset))
        }
    }

    var showBar by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.value, scrollState.isScrollInProgress, scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            showBar = true
            delay(1000)
            if (!scrollState.isScrollInProgress) showBar = false
        } else {
            showBar = false
        }
    }
    val barAlpha by animateFloatAsState(
        targetValue = if (showBar) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "scrollBar"
    )

    // 标签动画：空且未聚焦时待在框内，聚焦或有内容后浮到上边框
    val floating = focused || textValue.text.isNotEmpty()
    val floatProgress by animateFloatAsState(
        targetValue = if (floating) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "promptLabel"
    )
    val labelDensity = LocalDensity.current
    var labelHeightPx by remember { mutableIntStateOf(0) }
    val labelRestTopPx = with(labelDensity) { 12.dp.toPx() }
    val labelTopPx = labelRestTopPx + (-labelHeightPx / 2f - labelRestTopPx) * floatProgress
    val labelTopDp = with(labelDensity) { labelTopPx.toDp() }

    Column(modifier = modifier) {
        // 预留浮动标签越过边框的高度，输入框本体高度和原来保持一致
        Spacer(Modifier.height(21.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(colors.background)
                    .border(1.dp, if (focused) colors.primary else colors.outline, shape)
                    .onSizeChanged { viewportHeight = it.height }
            ) {
            val density = LocalDensity.current
            val padStart = with(density) { 14.dp.toPx() }
            val padTop = with(density) { 12.dp.toPx() }
            // 竖线宽 2dp，水滴尖对准它的中心
            val caretHalf = with(density) { 1.dp.toPx() }
            FallingCursorDrop(
                modifier = Modifier.fillMaxSize(),
                caretProvider = caret@{ boxSize ->
                    if (!fadeEnabled || !focused || !textValue.selection.collapsed) {
                        return@caret null
                    }
                    val layout = textLayout ?: return@caret null
                    // layout 可能落后一帧，先夹取到合法范围
                    val offset = textValue.selection.start
                        .coerceIn(0, layout.layoutInput.text.length)
                    val caret = layout.getCursorRect(offset)
                    val top = padTop - scrollState.value + caret.top
                    val bottom = padTop - scrollState.value + caret.bottom
                    if (bottom < 0f || top > boxSize.height) {
                        null
                    } else {
                        CaretInfo(x = padStart + caret.left, top = top, bottom = bottom)
                    }
                },
                dropOffset = { tap, _ ->
                    val layout = textLayout
                    if (layout == null) {
                        tap
                    } else {
                        val offset = layout.getOffsetForPosition(
                            Offset(
                                x = tap.x - padStart,
                                y = tap.y - padTop + scrollState.value
                            )
                        )
                        val caret = layout.getCursorRect(offset)
                        Offset(
                            x = padStart + caret.left + caretHalf,
                            y = padTop - scrollState.value + caret.bottom
                        )
                    }
                }
            ) { _ ->
                BasicTextField(
                    value = textValue,
                    onValueChange = { updated ->
                        textValue = updated
                        if (updated.text != value) onValueChange(updated.text)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(start = 14.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = colors.onSurface),
                    cursorBrush = SolidColor(
                        if (fadeEnabled) Color.Transparent else colors.primary
                    ),
                    onTextLayout = { textLayout = it },
                    decorationBox = { innerTextField ->
                        Box {
                            // 标签浮起时提示语才渐显，避免和标签重叠
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.alpha(floatProgress)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (scrollState.maxValue > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(18.dp)
                        .pointerInput(scrollState.maxValue) {
                            detectVerticalDragGestures { change, _ ->
                                change.consume()
                                val pad = 8.dp.toPx()
                                val trackHeight = (size.height - pad * 2).coerceAtLeast(1f)
                                val y = (change.position.y - pad).coerceIn(0f, trackHeight)
                                val target = (y / trackHeight) * scrollState.maxValue
                                scope.launch { scrollState.scrollTo(target.toInt()) }
                            }
                        },
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Canvas(
                        modifier = Modifier
                            .padding(end = 7.dp, top = 8.dp, bottom = 8.dp)
                            .width(3.dp)
                            .fillMaxHeight()
                            .alpha(barAlpha)
                    ) {
                        val trackHeight = size.height
                        val contentHeight = trackHeight + scrollState.maxValue
                        val thumbHeight = (trackHeight * trackHeight / contentHeight)
                            .coerceAtLeast(24.dp.toPx())
                        val maxScroll = scrollState.maxValue.coerceAtLeast(1)
                        val top = (trackHeight - thumbHeight) *
                            (scrollState.value.toFloat() / maxScroll)

                        drawRoundRect(
                            color = colors.outlineVariant,
                            cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
                        )
                        drawRoundRect(
                            color = colors.onSurfaceVariant,
                            topLeft = Offset(0f, top),
                            size = Size(size.width, thumbHeight),
                            cornerRadius = CornerRadius(size.width / 2f, size.width / 2f)
                        )
                    }
                }
            }
            }

            // 浮动标签：背景色盖住上边框形成缺口
            Text(
                text = label,
                fontSize = 12.5.sp,
                color = if (focused) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier
                    .offset(x = 10.dp, y = labelTopDp)
                    .background(colors.background)
                    .padding(horizontal = 4.dp)
                    .onSizeChanged { labelHeightPx = it.height }
            )
        }
    }
}
