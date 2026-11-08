package com.aichat.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aichat.app.R
import com.aichat.app.data.AppPrefs

@Composable
fun ThemeScreen(current: String, onSelect: (String) -> Unit, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    val options = listOf(
        AppPrefs.THEME_SYSTEM to stringResource(R.string.theme_system),
        AppPrefs.THEME_LIGHT to stringResource(R.string.theme_light),
        AppPrefs.THEME_DARK to stringResource(R.string.theme_dark)
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.theme_title),
                onBack = onBack,
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
                    onClick = { onSelect(value) }
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
