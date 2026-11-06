package com.aichat.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.data.AppPrefs

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val baseColors = LocalTextSelectionColors.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val measureStyle = TextStyle(fontSize = 14.sp)
    val textPad = with(density) { 16.dp.toPx() }
    // M3 带 label 的 OutlinedTextField 会在顶部多留 8sp（BodySmallLineHeight / 2），
    // 文字区域整体下移，这里按同样的偏移修正自绘光标的位置。
    val outlinedTopPad = with(density) { 8.sp.toPx() }
    val cursorWidth = with(density) { 2.dp.toPx() }
    val fadeEnabled = AppPrefs.cursorFadeEnabled(context) &&
        visualTransformation == VisualTransformation.None

    var tfv by remember { mutableStateOf(TextFieldValue(value)) }
    var focused by remember { mutableStateOf(false) }
    if (tfv.text != value) {
        tfv = tfv.copy(
            text = value,
            selection = TextRange(tfv.selection.start.coerceIn(0, value.length))
        )
    }

    FallingCursorDrop(
        modifier = modifier,
        dropOffset = { tap, boxSize ->
            if (visualTransformation != VisualTransformation.None) {
                tap
            } else {
                val layout = measurer.measure(
                    text = AnnotatedString(value),
                    style = measureStyle,
                    maxLines = 1,
                    softWrap = false
                )
                val localX = (tap.x - textPad)
                    .coerceIn(0f, layout.size.width.toFloat())
                val charOffset = layout.getOffsetForPosition(
                    Offset(localX, layout.size.height / 2f)
                )
                val caret = layout.getCursorRect(charOffset)
                // 与系统光标一致：到达文本末尾时向内收 2dp
                val caretLeft = caret.left.coerceIn(
                    0f,
                    (layout.size.width - cursorWidth).coerceAtLeast(0f)
                )
                val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
                // 水滴尖要对准竖线中心，而不是竖线左边
                Offset(textPad + caretLeft + cursorWidth / 2f, textTop + caret.bottom)
            }
        },
        caretProvider = caret@{ boxSize ->
            if (!fadeEnabled || !focused || !tfv.selection.collapsed) return@caret null
            val layout = measurer.measure(
                text = AnnotatedString(tfv.text),
                style = measureStyle,
                maxLines = 1,
                softWrap = false
            )
            val caret = layout.getCursorRect(tfv.selection.start)
            val caretLeft = caret.left.coerceIn(
                0f,
                (layout.size.width - cursorWidth).coerceAtLeast(0f)
            )
            val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
            CaretInfo(
                x = textPad + caretLeft,
                top = textTop + caret.top,
                bottom = textTop + caret.bottom
            )
        }
    ) { handleColor ->
        OutlinedTextField(
            value = tfv,
            onValueChange = { updated ->
                tfv = updated
                if (updated.text != value) onValueChange(updated.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            label = { Text(text = label, fontSize = 13.sp) },
            placeholder = { Text(text = placeholder, fontSize = 14.sp, color = colors.onSurfaceVariant) },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = colors.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant,
                cursorColor = if (fadeEnabled) Color.Transparent else colors.primary,
                selectionColors = TextSelectionColors(
                    handleColor = handleColor,
                    backgroundColor = baseColors.backgroundColor
                )
            )
        )
    }
}
