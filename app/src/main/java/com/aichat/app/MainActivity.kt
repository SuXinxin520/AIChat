package com.aichat.app

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.aichat.app.data.AppPrefs
import com.aichat.app.ui.AppRoot
import com.aichat.app.ui.theme.AIChatTheme
import com.aichat.app.util.Locales

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Locales.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(AppPrefs.theme(context)) }
            val darkTheme = when (themeMode) {
                AppPrefs.THEME_LIGHT -> false
                AppPrefs.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            AIChatTheme(darkTheme = darkTheme) {
                AppRoot(
                    themeMode = themeMode,
                    onThemeChange = { selected ->
                        AppPrefs.setTheme(context, selected)
                        themeMode = selected
                    }
                )
            }
        }
    }
}
