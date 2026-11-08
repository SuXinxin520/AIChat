package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.ApiConfig
import com.aichat.app.data.ApiConfigStore

@Composable
fun ApiListScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var configs by remember { mutableStateOf(ApiConfigStore.loadAll(context)) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.api_title),
                onBack = onBack,
                action = if (configs.isNotEmpty()) {
                    {
                        TextButton(onClick = onAdd) {
                            Text(
                                text = stringResource(R.string.api_add),
                                fontSize = 14.sp,
                                color = colors.primary,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                } else {
                    null
                }
            )
        }
    ) { innerPadding ->
        if (configs.isEmpty()) {
            EmptyApiState(
                onAdd = onAdd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(configs, key = { it.id }) { config ->
                    ApiConfigRow(
                        config = config,
                        onClick = { onEdit(config.id) },
                        onToggle = { checked ->
                            ApiConfigStore.upsert(context, config.copy(enabled = checked))
                            configs = ApiConfigStore.loadAll(context)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                        thickness = 0.6.dp,
                        color = colors.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyApiState(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.api_empty_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.api_empty_subtitle),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 13.dp)
        ) {
            Text(text = stringResource(R.string.api_add), fontSize = 15.sp)
        }
    }
}

@Composable
private fun ApiConfigRow(
    config: ApiConfig,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .alpha(if (config.enabled) 1f else 0.4f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayName = if (config.name.isBlank()) {
                    stringResource(R.string.api_unnamed)
                } else {
                    config.name
                }
                Text(
                    text = displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = config.model.ifBlank { config.baseUrl },
                    fontSize = 12.5.sp,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Switch(
            checked = config.enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                checkedBorderColor = colors.primary
            )
        )
    }
}
