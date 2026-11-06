package com.aichat.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.Storage
import com.aichat.app.data.StorageUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val AvatarColor = Color(0xFF7A5AD6)
private val AttachmentColor = Color(0xFF2A9AA8)

@Composable
fun StorageScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var folder by remember { mutableStateOf(Storage.folderName(context)) }
    var usage by remember { mutableStateOf(Storage.usage(context)) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { Storage.setFolder(context, uri) }
                if (ok) {
                    folder = Storage.folderName(context)
                    usage = Storage.usage(context)
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.storage_title),
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.storage_folder),
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = folder.ifBlank { "—" },
                fontSize = 15.sp,
                color = colors.onSurface
            )

            Spacer(Modifier.height(24.dp))

            UsageBar(usage)

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.storage_total, formatBytes(usage.total)),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Spacer(Modifier.height(12.dp))

            LegendRow(colors.primary, stringResource(R.string.storage_data), usage.dataBytes)
            LegendRow(AvatarColor, stringResource(R.string.storage_avatars), usage.avatarBytes)
            LegendRow(AttachmentColor, stringResource(R.string.storage_attachments), usage.attachmentBytes)

            Spacer(Modifier.height(26.dp))

            OutlinedButton(
                onClick = { picker.launch(null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(text = stringResource(R.string.storage_change), fontSize = 15.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun UsageBar(usage: StorageUsage) {
    val colors = MaterialTheme.colorScheme
    val total = usage.total

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceVariant)
    ) {
        if (total > 0) {
            if (usage.dataBytes > 0) {
                Box(
                    Modifier
                        .weight(usage.dataBytes.toFloat())
                        .fillMaxHeight()
                        .background(colors.primary)
                )
            }
            if (usage.avatarBytes > 0) {
                Box(
                    Modifier
                        .weight(usage.avatarBytes.toFloat())
                        .fillMaxHeight()
                        .background(AvatarColor)
                )
            }
            if (usage.attachmentBytes > 0) {
                Box(
                    Modifier
                        .weight(usage.attachmentBytes.toFloat())
                        .fillMaxHeight()
                        .background(AttachmentColor)
                )
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, bytes: Long) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = label,
            fontSize = 13.5.sp,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatBytes(bytes),
            fontSize = 13.5.sp,
            color = colors.onSurfaceVariant
        )
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
    else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
}
