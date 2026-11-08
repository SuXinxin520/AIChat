package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.PromptLibraryStore

@Composable
fun PromptLibraryScreen(
    onBack: () -> Unit,
    onOpenFolder: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var library by remember { mutableStateOf(PromptLibraryStore.ensureReady(context)) }
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.prompt_library_title),
                onBack = onBack,
                action = {
                    TextButton(onClick = {
                        newName = ""
                        showAdd = true
                    }) {
                        Text(
                            text = stringResource(R.string.prompt_folder_add),
                            fontSize = 14.sp,
                            color = colors.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (library.folders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.prompt_library_empty_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prompt_library_empty_hint),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        newName = ""
                        showAdd = true
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(stringResource(R.string.prompt_folder_add))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(library.folders, key = { it.id }) { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFolder(folder.id) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
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
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = promptFolderName(folder),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.prompt_folder_count,
                                    library.promptsIn(folder.id).size
                                ),
                                fontSize = 12.5.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                        thickness = 0.6.dp,
                        color = colors.outlineVariant
                    )
                }
            }
        }
    }

    if (showAdd) {
        AppDialog(
            onDismissRequest = { showAdd = false },
            title = stringResource(R.string.prompt_folder_add),
            text = {
                AppTextField(
                    label = stringResource(R.string.prompt_folder_name),
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = stringResource(R.string.prompt_folder_name_hint)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newName.isNotBlank(),
                    onClick = {
                        library = PromptLibraryStore.addFolder(context, newName).first
                        showAdd = false
                    }
                ) {
                    Text(stringResource(R.string.save), color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) {
                    Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
                }
            }
        )
    }
}
