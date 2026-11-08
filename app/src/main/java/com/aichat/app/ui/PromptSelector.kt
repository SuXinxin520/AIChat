package com.aichat.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.PromptLibraryStore

/**
 * 角色/用户资料的提示词选择器：左侧选分类，右侧多选提示词；
 * 分类标题旁的三态框可以整组选择；「添加提示词」会往库里写一条并自动选中。
 */
@Composable
fun PromptSelector(
    label: String,
    selectedIds: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var library by remember { mutableStateOf(PromptLibraryStore.ensureReady(context)) }
    var currentFolderId by rememberSaveable {
        mutableStateOf(library.folders.firstOrNull()?.id.orEmpty())
    }
    var showCustom by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var showAddFolder by remember { mutableStateOf(false) }

    val validIds = remember(selectedIds, library) {
        selectedIds.distinct().filter { library.prompt(it) != null }
    }

    LaunchedEffect(library.folders) {
        if (library.folder(currentFolderId) == null) {
            currentFolderId = library.folders.firstOrNull()?.id.orEmpty()
        }
    }

    val currentFolder = library.folder(currentFolderId)
    val folderPrompts = library.promptsIn(currentFolderId)
    val selectedInFolder = folderPrompts.count { it.id in validIds }
    val triState = when {
        folderPrompts.isNotEmpty() && selectedInFolder == folderPrompts.size -> ToggleableState.On
        selectedInFolder > 0 -> ToggleableState.Indeterminate
        else -> ToggleableState.Off
    }

    fun toggle(id: String) {
        val next = if (id in validIds) validIds - id else validIds + id
        onSelectionChange(next)
    }

    fun toggleFolder() {
        if (folderPrompts.isEmpty()) return
        val ids = folderPrompts.map { it.id }
        val allSelected = ids.all { it in validIds }
        val next = if (allSelected) {
            validIds.filterNot { it in ids }
        } else {
            (validIds + ids).distinct()
        }
        onSelectionChange(next)
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.prompt_selected_count, validIds.size),
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.width(104.dp).fillMaxHeight()) {
                    items(library.folders, key = { it.id }) { folder ->
                        val active = folder.id == currentFolderId
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (active) colors.primaryContainer else Color.Transparent
                                )
                                .clickable { currentFolderId = folder.id }
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = promptFolderName(folder),
                                fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                                color = if (active) {
                                    colors.onPrimaryContainer
                                } else {
                                    colors.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(
                                    R.string.prompt_folder_count_short,
                                    library.promptsIn(folder.id).size
                                ),
                                fontSize = 11.sp,
                                color = if (active) {
                                    colors.onPrimaryContainer.copy(alpha = 0.7f)
                                } else {
                                    colors.onSurfaceVariant
                                },
                                maxLines = 1
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddFolder = true }
                                .padding(horizontal = 10.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.width(16.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = stringResource(R.string.prompt_folder_add),
                                fontSize = 12.5.sp,
                                color = colors.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .width(0.6.dp)
                        .fillMaxHeight()
                        .background(colors.outlineVariant)
                )

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 4.dp, top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFolder?.let { promptFolderName(it) }
                                ?: stringResource(R.string.prompt_folder_untitled),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TriStateCheckbox(
                            state = triState,
                            onClick = { toggleFolder() },
                            enabled = folderPrompts.isNotEmpty()
                        )
                    }

                    if (folderPrompts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.prompt_selector_empty),
                                fontSize = 12.5.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(folderPrompts, key = { it.id }) { prompt ->
                                val checked = prompt.id in validIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggle(prompt.id) }
                                        .padding(start = 4.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = checked, onCheckedChange = null)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prompt.name.ifBlank { prompt.content.take(12) },
                                            fontSize = 13.5.sp,
                                            color = colors.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = prompt.content.replace('\n', ' '),
                                            fontSize = 11.5.sp,
                                            color = colors.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showCustom = true },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.prompt_add),
                            fontSize = 12.5.sp,
                            color = colors.primary
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showPreview = true }) {
                Text(
                    text = stringResource(R.string.prompt_preview),
                    fontSize = 12.5.sp,
                    color = colors.primary
                )
            }
            if (validIds.isNotEmpty()) {
                TextButton(onClick = { onSelectionChange(emptyList()) }) {
                    Text(
                        text = stringResource(R.string.clear),
                        fontSize = 12.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showCustom) {
        CustomPromptDialog(
            library = library,
            folders = library.folders,
            initialFolderId = currentFolderId,
            onDismiss = { showCustom = false },
            onCreateFolder = { name ->
                val (updated, folder) = PromptLibraryStore.addFolder(context, name)
                library = updated
                folder
            },
            onConfirm = { folderId, name, content ->
                val (updated, item) = PromptLibraryStore.addPrompt(
                    context = context,
                    folderId = folderId,
                    name = name,
                    content = content
                )
                library = updated
                onSelectionChange((validIds + item.id).distinct())
                currentFolderId = item.folderId
                showCustom = false
            }
        )
    }

    if (showAddFolder) {
        FolderNameDialog(
            onDismiss = { showAddFolder = false },
            onConfirm = { name ->
                val (updated, folder) = PromptLibraryStore.addFolder(context, name)
                library = updated
                currentFolderId = folder.id
                showAddFolder = false
            }
        )
    }

    if (showPreview) {
        AlertDialog(
            onDismissRequest = { showPreview = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.prompt_preview_title),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val text = library.composePrompt(validIds)
                    Text(
                        text = text.ifBlank { stringResource(R.string.prompt_preview_empty) },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreview = false }) {
                    Text(stringResource(R.string.prompt_close), color = colors.primary)
                }
            }
        )
    }
}

@Composable
private fun CustomPromptDialog(
    library: com.aichat.app.data.PromptLibrary,
    folders: List<com.aichat.app.data.PromptFolder>,
    initialFolderId: String,
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> com.aichat.app.data.PromptFolder,
    onConfirm: (folderId: String, name: String, content: String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var folderId by remember { mutableStateOf(initialFolderId) }
    var folderMenu by remember { mutableStateOf(false) }
    var showNewFolder by remember { mutableStateOf(false) }

    val selectedFolder = library.folder(folderId)
    val folderLabel = if (selectedFolder != null) {
        promptFolderName(selectedFolder)
    } else {
        stringResource(R.string.prompt_folder_custom)
    }

    AppDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.prompt_edit_add),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                AppTextField(
                    label = stringResource(R.string.prompt_name),
                    value = name,
                    onValueChange = { name = it },
                    placeholder = stringResource(R.string.prompt_name_hint)
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { folderMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.prompt_custom_folder_value, folderLabel),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = folderMenu,
                            onDismissRequest = { folderMenu = false }
                        ) {
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(promptFolderName(folder)) },
                                    onClick = {
                                        folderId = folder.id
                                        folderMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showNewFolder = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.prompt_folder_add),
                            tint = colors.primary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                PromptField(
                    label = stringResource(R.string.prompt_content),
                    value = content,
                    onValueChange = { content = it },
                    placeholder = stringResource(R.string.prompt_content_hint),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && content.isNotBlank(),
                onClick = { onConfirm(folderId, name, content) }
            ) {
                Text(stringResource(R.string.save), color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
            }
        }
    )

    if (showNewFolder) {
        FolderNameDialog(
            onDismiss = { showNewFolder = false },
            onConfirm = { folderName ->
                folderId = onCreateFolder(folderName).id
                showNewFolder = false
            }
        )
    }
}

/** 新建分组：只输入名称，确认后由调用方写入提示词库。 */
@Composable
private fun FolderNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var name by remember { mutableStateOf("") }

    AppDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.prompt_folder_add),
        text = {
            AppTextField(
                label = stringResource(R.string.prompt_folder_name),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.prompt_folder_name_hint)
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) {
                Text(stringResource(R.string.save), color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
            }
        }
    )
}
