package com.aichat.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.AppPrefs

@Composable
fun GeneralScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var stream by remember { mutableStateOf(AppPrefs.streamEnabled(context)) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.general_title),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.general_stream),
                        fontSize = 15.sp,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.general_stream_desc),
                        fontSize = 12.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }

                Switch(
                    checked = stream,
                    onCheckedChange = {
                        AppPrefs.setStreamEnabled(context, it)
                        stream = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimary,
                        checkedTrackColor = colors.primary,
                        checkedBorderColor = colors.primary
                    )
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.6.dp,
                color = colors.outlineVariant
            )
        }
    }
}
