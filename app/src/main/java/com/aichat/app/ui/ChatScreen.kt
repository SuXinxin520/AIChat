package com.aichat.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.ApiConfigStore
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.ChatApi
import com.aichat.app.data.ChatMessage
import com.aichat.app.data.DraftStore
import com.aichat.app.data.MessageStore
import com.aichat.app.data.ProfileStore
import com.aichat.app.data.Role
import com.aichat.app.util.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    roleId: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    val loadedRole = remember(roleId) {
        ProfileStore.loadRoles(context).firstOrNull { it.id == roleId }
    }

    if (loadedRole == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val role = loadedRole

    var messages by remember(roleId) { mutableStateOf(MessageStore.load(context, roleId)) }
    var input by rememberSaveable {
        mutableStateOf(DraftStore.load(context, roleId)?.text.orEmpty())
    }
    var showAttachSheet by remember { mutableStateOf(false) }

    // 输入停顿后存草稿，离开页面时再兜一次
    LaunchedEffect(input) {
        delay(400)
        DraftStore.save(context, roleId, input)
    }
    DisposableEffect(Unit) {
        onDispose { DraftStore.save(context, roleId, input) }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val imagePreview = stringResource(R.string.chat_preview_image)
    val filePreview = stringResource(R.string.chat_preview_file)
    val filePickFailed = stringResource(R.string.chat_file_pick_failed)
    val noApiMessage = stringResource(R.string.chat_no_api)

    var sending by remember { mutableStateOf(false) }
    // streamingText 是已收到的全文，shownChars 是已经"打"出来的字数
    var streamingText by remember { mutableStateOf("") }
    var shownChars by remember { mutableIntStateOf(0) }

    // 不管服务端一次吐多少字，这里按固定节奏一个个显示
    LaunchedEffect(sending) {
        while (sending) {
            if (shownChars < streamingText.length) {
                delay(22)
                shownChars += 1
            } else {
                delay(16)
            }
        }
    }

    // 用户资料与角色设定拼成系统提示
    val profile = remember { ProfileStore.loadUser(context) }
    val userNameLine = if (profile.name.isNotBlank()) {
        stringResource(R.string.sys_user_name, profile.name)
    } else {
        ""
    }
    val userAboutLine = if (profile.prompt.isNotBlank()) {
        stringResource(R.string.sys_about_user, profile.prompt)
    } else {
        ""
    }
    val roleLine = if (role.prompt.isNotBlank()) {
        stringResource(R.string.sys_role, role.prompt)
    } else {
        ""
    }
    val systemPrompt = listOf(userNameLine, userAboutLine, roleLine)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    fun append(message: ChatMessage, preview: String) {
        MessageStore.add(context, message)
        messages = MessageStore.load(context, roleId)
        ProfileStore.upsertRole(
            context,
            role.copy(lastMessage = preview, lastMessageAt = message.at)
        )
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    ImageStore.copyToAppStorage(context, uri, folder = "attachments")
                }
                if (path != null) {
                    val now = System.currentTimeMillis()
                    append(
                        ChatMessage(roleId = roleId, fromUser = true, imagePath = path, at = now),
                        imagePreview
                    )
                } else {
                    snackbarHostState.showSnackbar(filePickFailed)
                }
            }
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val name = ImageStore.displayName(context, uri)
                val path = withContext(Dispatchers.IO) {
                    ImageStore.copyToAppStorage(context, uri, folder = "attachments")
                }
                if (path != null) {
                    val now = System.currentTimeMillis()
                    append(
                        ChatMessage(
                            roleId = roleId,
                            fromUser = true,
                            fileName = name,
                            filePath = path,
                            at = now
                        ),
                        filePreview
                    )
                } else {
                    snackbarHostState.showSnackbar(filePickFailed)
                }
            }
        }
    }

    // 列表是 reverseLayout（底部锚定），键盘上推、逐字变长都会自动贴底；
    // 只有新消息落库时平滑滚一下
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(role = role, onBack = onBack, onOpenSettings = onOpenSettings)
        },
        bottomBar = {
            ChatInputBar(
                input = input,
                onInputChange = { input = it },
                onAttach = { showAttachSheet = true },
                onSend = {
                    val text = input.trim()
                    if (text.isNotEmpty() && !sending) {
                        val config = ApiConfigStore.loadAll(context).firstOrNull { it.enabled }
                        if (config == null || config.apiKey.isBlank() || config.model.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar(noApiMessage) }
                        } else {
                            val now = System.currentTimeMillis()
                            append(
                                ChatMessage(roleId = roleId, fromUser = true, text = text, at = now),
                                text
                            )
                            input = ""
                            DraftStore.clear(context, roleId)
                            sending = true
                            streamingText = ""
                            shownChars = 0
                            scope.launch {
                                val history = MessageStore.load(context, roleId)
                                try {
                                    if (AppPrefs.streamEnabled(context)) {
                                        ChatApi.streamReply(config, systemPrompt, history)
                                            .collect { delta -> streamingText += delta }
                                    } else {
                                        streamingText = ChatApi.reply(config, systemPrompt, history)
                                    }

                                    val reply = streamingText.trim()
                                    if (reply.isEmpty()) {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.chat_send_failed, "")
                                        )
                                    } else {
                                        // 等打字追上再落库，避免末尾被截断
                                        while (shownChars < streamingText.length) {
                                            delay(12)
                                        }
                                        append(
                                            ChatMessage(
                                                roleId = roleId,
                                                fromUser = false,
                                                text = reply,
                                                at = System.currentTimeMillis()
                                            ),
                                            reply
                                        )
                                    }
                                } catch (error: Exception) {
                                    snackbarHostState.showSnackbar(
                                        context.getString(
                                            R.string.chat_send_failed,
                                            error.message.orEmpty()
                                        )
                                    )
                                } finally {
                                    sending = false
                                    streamingText = ""
                                    shownChars = 0
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(interactionSource = null, indication = null) {
                    // 点空白处收起键盘
                    focusManager.clearFocus()
                },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 反向布局：索引 0 在最底部，所以"输入中"气泡排在最前
            if (sending) {
                item("typing") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 13.dp, vertical = 9.dp)
                        ) {
                            val visibleText = streamingText.take(shownChars)
                            if (visibleText.isEmpty()) {
                                TypingIndicator(color = colors.onSurfaceVariant)
                            } else {
                                PoppingText(text = visibleText, color = colors.onSurface)
                            }
                        }
                    }
                }
            }

            items(messages.asReversed(), key = { it.id }) { message ->
                MessageBubble(message)
            }
        }
    }

    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            containerColor = colors.background,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                AttachRow(
                    icon = Icons.Outlined.Image,
                    label = stringResource(R.string.chat_attach_image),
                    onClick = {
                        showAttachSheet = false
                        imagePicker.launch("image/*")
                    }
                )
                AttachRow(
                    icon = Icons.Outlined.AttachFile,
                    label = stringResource(R.string.chat_attach_file),
                    onClick = {
                        showAttachSheet = false
                        filePicker.launch("*/*")
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatTopBar(role: Role, onBack: () -> Unit, onOpenSettings: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column {
    // 与主界面头部保持同样的内边距，右侧图标位置才对得上
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 6.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = colors.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        Avatar(path = role.avatarPath, size = 32.dp)

        Spacer(Modifier.width(10.dp))

        Text(
            text = role.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // 三个点进角色设置，和主界面的齿轮区分开
        IconButton(onClick = onOpenSettings, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.chat_role_settings),
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    HorizontalDivider(thickness = 0.8.dp, color = colors.outline)
    }
}

/** "输入中"后面的点：1 → 2 → 3 → 1 循环。 */
@Composable
private fun TypingIndicator(color: Color) {
    var dots by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(420)
            dots = if (dots >= 3) 1 else dots + 1
        }
    }
    Text(
        text = stringResource(R.string.chat_typing) + ".".repeat(dots),
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = color
    )
}

@Composable
private fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canSend = input.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 左边：加号，发图片或文件
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant)
                .clickable(onClick = onAttach),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.chat_attach),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        BasicTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            maxLines = 4,
            decorationBox = { innerTextField ->
                Box {
                    if (input.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_input_hint),
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                color = colors.onSurfaceVariant
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(Modifier.width(8.dp))

        // 右边：发送
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (canSend) colors.primary else colors.surfaceVariant)
                .clickable(enabled = canSend, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.chat_send),
                tint = if (canSend) colors.onPrimary else colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.fromUser
    val bubbleColor = if (isUser) colors.primary else colors.surfaceVariant
    val contentColor = if (isUser) colors.onPrimary else colors.onSurface
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleColor)
        ) {
            when {
                message.imagePath.isNotBlank() -> {
                    val image = remember(message.imagePath) {
                        ImageStore.loadBitmap(message.imagePath, 800)
                    }
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .widthIn(max = 220.dp)
                                .heightIn(max = 300.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.chat_preview_image),
                            fontSize = 14.sp,
                            color = contentColor,
                            modifier = Modifier.padding(13.dp)
                        )
                    }
                }

                message.fileName.isNotBlank() -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = message.fileName,
                            fontSize = 14.sp,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                else -> {
                    Box(modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)) {
                        if (isUser) {
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                color = contentColor
                            )
                        } else {
                            // 和打字过程用同一套排版，落库后不再变形
                            PoppingText(
                                text = message.text,
                                color = contentColor,
                                animate = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 逐字排版。打字时 animate = true（每个新字弹一下），
 * 消息落库后仍用同一套排版但 animate = false，保证前后渲染完全一致、不会跳。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoppingText(text: String, color: Color, animate: Boolean = true) {
    Column {
        text.split('\n').forEach { line ->
            if (line.isEmpty()) {
                Spacer(Modifier.height(20.dp))
            } else {
                FlowRow {
                    line.forEachIndexed { index, ch ->
                        key(index) {
                            PoppingChar(ch.toString(), color, animate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PoppingChar(ch: String, color: Color, animate: Boolean) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (animate) {
            scale.snapTo(1.5f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.42f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
    Text(
        text = ch,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = color,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    )
}

@Composable
private fun AttachRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(text = label, fontSize = 15.sp, color = colors.onSurface)
    }
}
