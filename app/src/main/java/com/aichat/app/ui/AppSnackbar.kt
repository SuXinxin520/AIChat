package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 统一的提示条：内容居中，深色（深色主题下为浅色）圆角胶囊，
 * 背景与文字都取自主题色，切换浅色/深色时自动跟着变。
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = data.visuals.message,
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.onSurface)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = colors.background,
                textAlign = TextAlign.Center
            )
        }
    }
}
