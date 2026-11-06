package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aichat.app.R
import com.aichat.app.data.AppPrefs

/**
 * 离开编辑页且内容有修改时的提醒。
 * 左下方可勾选「不再显示」，右侧是「直接退出」（红色）和「保存并退出」。
 */
@Composable
fun UnsavedChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var neverShow by remember { mutableStateOf(false) }

    // 勾了「不再显示」时记下来，之后直接退出不再询问
    val rememberChoice: () -> Unit = {
        if (neverShow) AppPrefs.setConfirmUnsaved(context, false)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    Text(
                        text = stringResource(R.string.unsaved_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.unsaved_message),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = colors.onSurfaceVariant
                    )

                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { neverShow = !neverShow }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MiniCheck(neverShow)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.unsaved_never),
                                    fontSize = 12.5.sp,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                rememberChoice()
                                onDiscard()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.unsaved_discard),
                                fontSize = 14.sp,
                                color = colors.error,
                                maxLines = 1
                            )
                        }

                        TextButton(
                            onClick = {
                                rememberChoice()
                                onSave()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.unsaved_save),
                                fontSize = 14.sp,
                                color = colors.primary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCheck(checked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(17.dp)
            .clip(shape)
            .background(if (checked) colors.primary else Color.Transparent)
            .border(1.4.dp, if (checked) colors.primary else colors.outline, shape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
