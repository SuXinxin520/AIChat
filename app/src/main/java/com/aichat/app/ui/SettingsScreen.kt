package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R

private data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val screen: Screen
)

@Composable
fun SettingsScreen(onBack: () -> Unit, onOpen: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val placeholderBody = stringResource(R.string.placeholder_body)

    val items = listOf(
        SettingItem(stringResource(R.string.settings_general), Icons.Outlined.Tune, Screen.General),
        SettingItem(stringResource(R.string.settings_api), Icons.Outlined.Key, Screen.ApiList),
        SettingItem(stringResource(R.string.settings_language), Icons.Outlined.Translate, Screen.Language),
        SettingItem(stringResource(R.string.settings_theme), Icons.Outlined.Palette, Screen.Theme),
        SettingItem(
            stringResource(R.string.settings_permissions),
            Icons.Outlined.Shield,
            Screen.Placeholder(stringResource(R.string.settings_permissions), placeholderBody)
        ),
        SettingItem(stringResource(R.string.settings_storage), Icons.Outlined.Storage, Screen.Storage),
        SettingItem(stringResource(R.string.settings_about), Icons.Outlined.Info, Screen.About)
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.settings),
                onBack = onBack,
                showDivider = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            items.forEachIndexed { index, item ->
                SettingRow(item = item, onClick = { onOpen(item.screen) })
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 20.dp),
                        thickness = 0.6.dp,
                        color = colors.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = item.title,
            fontSize = 15.sp,
            color = colors.onSurface
        )

        Spacer(Modifier.weight(1f))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
