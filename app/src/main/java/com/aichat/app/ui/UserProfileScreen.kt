package com.aichat.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.ProfileStore
import com.aichat.app.data.Storage
import com.aichat.app.data.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun UserProfileScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val saved = remember { ProfileStore.loadUser(context) }

    var name by rememberSaveable { mutableStateOf(saved.name) }
    var prompt by rememberSaveable { mutableStateOf(saved.prompt) }
    var avatarPath by rememberSaveable { mutableStateOf(saved.avatarPath) }
    var showUnsaved by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 键盘弹出时把表单滚到底，避免最下面的输入框被挡住（现在表单短，滚动量很小）
    val scrollState = rememberScrollState()
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            repeat(4) {
                delay(120)
                scrollState.scrollTo(scrollState.maxValue)
            }
        }
    }
    val pickFailed = stringResource(R.string.avatar_pick_failed)

    // 和进来时的内容对比，判断有没有改动
    val dirty = name.trim() != saved.name ||
        prompt.trim() != saved.prompt ||
        avatarPath != saved.avatarPath

    val save: () -> Unit = {
        ProfileStore.saveUser(
            context,
            UserProfile(
                name = name.trim(),
                prompt = prompt.trim(),
                avatarPath = avatarPath
            )
        )
    }

    val requestBack: () -> Unit = {
        if (dirty && AppPrefs.confirmUnsaved(context)) {
            showUnsaved = true
        } else {
            onBack()
        }
    }

    BackHandler { requestBack() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val copied = withContext(Dispatchers.IO) {
                    Storage.saveMedia(context, uri, Storage.DIR_AVATARS)
                }
                if (copied != null) {
                    avatarPath = copied
                } else {
                    snackbarHostState.showSnackbar(pickFailed)
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SubPageTopBar(title = stringResource(R.string.profile_title), onBack = requestBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(interactionSource = null, indication = null) {
                    focusManager.clearFocus()
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Avatar(
                    path = avatarPath,
                    size = 96.dp,
                    modifier = Modifier.clickable { picker.launch("image/*") }
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.profile_avatar_hint),
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            AppTextField(
                label = stringResource(R.string.profile_name),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.profile_name_hint)
            )

            Spacer(Modifier.height(14.dp))

            PromptField(
                label = stringResource(R.string.profile_prompt),
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = stringResource(R.string.profile_prompt_hint),
                // 固定高度 + 框内滚动 + 右侧滚动条
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    save()
                    onBack()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(text = stringResource(R.string.save), fontSize = 15.sp)
            }

            Spacer(Modifier.height(32.dp))
        }
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
                save()
                onBack()
            }
        )
    }
}
