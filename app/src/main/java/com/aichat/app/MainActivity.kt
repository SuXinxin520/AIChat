package com.aichat.app

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.Storage
import com.aichat.app.ui.AppRoot
import com.aichat.app.ui.StorageSetupScreen
import com.aichat.app.ui.theme.AIChatTheme
import com.aichat.app.util.Locales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // 任何配置/记录变化都标记为待同步
        Storage.sharedPrefs(this).registerOnSharedPreferenceChangeListener { _, _ ->
            Storage.markDirty(this)
        }

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var themeMode by remember { mutableStateOf(AppPrefs.theme(context)) }
            var hasFolder by remember { mutableStateOf(Storage.hasFolder(context)) }

            val darkTheme = when (themeMode) {
                AppPrefs.THEME_LIGHT -> false
                AppPrefs.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            val folderPicker = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocumentTree()
            ) { uri ->
                if (uri != null) {
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) { Storage.setFolder(context, uri) }
                        if (ok) hasFolder = Storage.hasFolder(context)
                    }
                }
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
                if (hasFolder) {
                    AppRoot(
                        themeMode = themeMode,
                        onThemeChange = { selected ->
                            AppPrefs.setTheme(context, selected)
                            themeMode = selected
                        }
                    )
                } else {
                    StorageSetupScreen(onPick = { folderPicker.launch(null) })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Storage.flushAsync(this)
    }
}
