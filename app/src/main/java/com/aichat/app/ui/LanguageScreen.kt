package com.aichat.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aichat.app.R
import com.aichat.app.data.AppPrefs
import com.aichat.app.util.findActivity

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var current by remember { mutableStateOf(AppPrefs.language(context)) }

    val options = listOf(
        AppPrefs.LANGUAGE_SYSTEM to stringResource(R.string.language_system),
        "zh" to stringResource(R.string.language_zh),
        "en" to stringResource(R.string.language_en)
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.language_title),
                onBack = onBack,
                showDivider = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            options.forEachIndexed { index, (value, label) ->
                ChoiceRow(
                    label = label,
                    selected = current == value,
                    onClick = {
                        if (current != value) {
                            AppPrefs.setLanguage(context, value)
                            current = value
                            // 语言切换后重建界面以套用新资源
                            context.findActivity()?.recreate()
                        }
                    }
                )
                if (index != options.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        thickness = 0.6.dp,
                        color = colors.outlineVariant
                    )
                }
            }
        }
    }
}
