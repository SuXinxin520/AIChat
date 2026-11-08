package com.aichat.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.PromptLibraryStore
import kotlinx.coroutines.launch

@Composable
fun PromptEditScreen(
    promptId: String?,
    folderId: String,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val library = remember { PromptLibraryStore.ensureReady(context) }
    val existing = remember(promptId) { promptId?.let { library.prompt(it) } }

    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var content by rememberSaveable { mutableStateOf(existing?.content.orEmpty()) }
    var showDelete by remember { mutableStateOf(false) }
    var showUnsaved by remember { mutableStateOf(false) }

    val nameRequired = stringResource(R.string.prompt_name_required)
    val contentRequired = stringResource(R.string.prompt_content_required)

    val dirty = name.trim() != existing?.name?.trim().orEmpty() ||
        content.trim() != existing?.content?.trim().orEmpty()

    fun save(): Boolean {
        if (name.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar(nameRequired) }
            return false
        }
        if (content.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar(contentRequired) }
            return false
        }
        if (existing == null) {
            PromptLibraryStore.addPrompt(context, folderId, name, content)
        } else {
            PromptLibraryStore.updatePrompt(
                context,
                existing.copy(name = name.trim(), content = content.trim())
            )
        }
        return true
    }

    val requestBack: () -> Unit = {
        if (dirty && AppPrefs.confirmUnsaved(context)) {
            showUnsaved = true
        } else {
            onBack()
        }
    }

    BackHandler { requestBack() }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            SubPageTopBar(
                title = stringResource(
                    if (existing == null) R.string.prompt_edit_add else R.string.prompt_edit_edit
                ),
                onBack = requestBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            AppTextField(
                label = stringResource(R.string.prompt_name),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.prompt_name_hint)
            )

            Spacer(Modifier.height(14.dp))

            PromptField(
                label = stringResource(R.string.prompt_content),
                value = content,
                onValueChange = { content = it },
                placeholder = stringResource(R.string.prompt_content_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (save()) onBack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(stringResource(R.string.save), fontSize = 15.sp, color = colors.onPrimary)
            }

            if (existing != null) {
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.prompt_delete),
                        color = colors.error,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.prompt_delete_title),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.prompt_delete_message),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    PromptLibraryStore.deletePrompt(context, existing.id)
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

    if (showUnsaved) {
        UnsavedChangesDialog(
            onDismiss = { showUnsaved = false },
            onDiscard = {
                showUnsaved = false
                onBack()
            },
            onSave = {
                showUnsaved = false
                if (save()) onBack()
            }
        )
    }
}
