package com.aichat.app.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.PromptLibraryStore

@Composable
fun PromptFolderScreen(
    folderId: String,
    onBack: () -> Unit,
    onAddPrompt: () -> Unit,
    onOpenPrompt: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var library by remember { mutableStateOf(PromptLibraryStore.ensureReady(context)) }
    val folder = library.folder(folderId)
    if (folder == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val folderTitle = promptFolderName(folder)
    val promptCount = library.promptsIn(folderId).size

    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = folderTitle,
                onBack = onBack,
                action = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onAddPrompt) {
                            Text(
                                text = stringResource(R.string.prompt_add),
                                fontSize = 14.sp,
                                color = colors.primary
                            )
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.prompt_more),
                                    tint = colors.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.prompt_folder_rename)) },
                                    onClick = {
                                        menuOpen = false
                                        renameText = folder.name.ifBlank { folderTitle }
                                        showRename = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.prompt_folder_delete),
                                            color = colors.error
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        showDelete = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val prompts = library.promptsIn(folderId)
        if (prompts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.prompt_folder_empty),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prompt_folder_empty_hint),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onAddPrompt,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(stringResource(R.string.prompt_add))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(prompts, key = { it.id }) { prompt ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPrompt(prompt.id) }
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = prompt.name.ifBlank { stringResource(R.string.prompt_folder_untitled) },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = prompt.content.replace('\n', ' '),
                            fontSize = 12.5.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp),
                        thickness = 0.6.dp,
                        color = colors.outlineVariant
                    )
                }
            }
        }
    }

    if (showRename) {
        AlertDialog(
            onDismissRequest = { showRename = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.prompt_folder_rename),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                AppTextField(
                    label = stringResource(R.string.prompt_folder_name),
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = stringResource(R.string.prompt_folder_name_hint)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = renameText.isNotBlank(),
                    onClick = {
                        library = PromptLibraryStore.renameFolder(context, folderId, renameText)
                        showRename = false
                    }
                ) {
                    Text(stringResource(R.string.save), color = colors.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRename = false }) {
                    Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
                }
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.prompt_folder_delete_title),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.prompt_folder_delete_message, promptCount),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    PromptLibraryStore.deleteFolder(context, folderId)
                    onBack()
                }) {
                    Text(stringResource(R.string.delete), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
                }
            }
        )
    }
}

