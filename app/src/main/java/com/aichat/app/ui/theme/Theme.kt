package com.aichat.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueSoft,
    onPrimaryContainer = BrandBlueDark,
    secondary = BrandBlue,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFF5F6F8),
    onSurfaceVariant = Color(0xFF8A9199),
    outline = Color(0xFFE3E6EA),
    outlineVariant = Color(0xFFF0F1F4),
    error = Color(0xFFD14343),
    onError = Color.White,
    // 纯白界面：弹窗、菜单等抬升表面也不再往主色偏
    surfaceTint = Color.White,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFF5F6F8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00394A),
    onPrimaryContainer = Color(0xFFBFE9FF),
    secondary = BrandBlue,
    onSecondary = Color.White,
    background = Color(0xFF0F1114),
    onBackground = Color(0xFFE8EBEE),
    surface = Color(0xFF0F1114),
    onSurface = Color(0xFFE8EBEE),
    surfaceVariant = Color(0xFF1B2026),
    onSurfaceVariant = Color(0xFF98A0A9),
    outline = Color(0xFF313840),
    outlineVariant = Color(0xFF23282E),
    error = Color(0xFFFF7979),
    onError = Color(0xFF2B0000),
    // 深色下保持中性灰，不用 Material 默认的偏紫表面
    surfaceTint = BrandBlue,
    surfaceBright = Color(0xFF2A3038),
    surfaceDim = Color(0xFF0B0D10),
    surfaceContainerLowest = Color(0xFF0B0D10),
    surfaceContainerLow = Color(0xFF121519),
    surfaceContainer = Color(0xFF171B20),
    surfaceContainerHigh = Color(0xFF1C2127),
    surfaceContainerHighest = Color(0xFF222831)
)

@Composable
fun AIChatTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
