package com.aichat.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
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
import com.aichat.app.data.Storage
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
    val copiedMessage = stringResource(R.string.chat_copied)
    val clipboard = LocalClipboardManager.current
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var deletingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    // 长按进入多选：selectedIds 为当前选中的消息
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 拖选时用消息在窗口里的实际位置做命中，避免 LazyColumn 内边距/反向布局带来的坐标偏差
    val itemBounds = remember { mutableMapOf<String, Rect>() }
    var listRootOffset by remember { mutableStateOf(Offset.Zero) }
    // 点屏幕其它位置时重建 SelectionContainer，从而取消已选中的文字
    var selectionReset by remember { mutableIntStateOf(0) }
    // 当前是否有长按选中的文字（用于把返回键先用于取消选择）
    var textSelectionActive by remember { mutableStateOf(false) }

    var sending by remember { mutableStateOf(false) }
    // streamingText 是已收到的全文，shownChars 是已经"打"出来的字数
    var streamingText by remember { mutableStateOf("") }
    var shownChars by remember { mutableIntStateOf(0) }

    val typewriterEnabled = AppPrefs.typewriterEnabled(context)

    // 不开打字机就直接整段显示；开了就按固定节奏一个个吐
    LaunchedEffect(sending, typewriterEnabled) {
        while (sending) {
            when {
                !typewriterEnabled -> {
                    shownChars = streamingText.length
                    delay(16)
                }
                shownChars < streamingText.length -> {
                    delay(22)
                    shownChars += 1
                }
                else -> delay(16)
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

    fun startReply() {
        val config = ApiConfigStore.loadAll(context).firstOrNull { it.enabled }
        if (config == null || config.apiKey.isBlank() || config.model.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar(noApiMessage) }
            return
        }

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
                    snackbarHostState.showSnackbar(context.getString(R.string.chat_send_failed, ""))
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
                    context.getString(R.string.chat_send_failed, error.message.orEmpty())
                )
            } finally {
                sending = false
                streamingText = ""
                shownChars = 0
            }
        }
    }

    fun regenerate(target: ChatMessage) {
        if (sending) return
        MessageStore.delete(context, target.id)
        messages = MessageStore.load(context, roleId)
        val currentRole = ProfileStore.loadRoles(context).firstOrNull { it.id == roleId }
        if (currentRole != null) {
            val last = messages.lastOrNull()
            ProfileStore.upsertRole(
                context,
                currentRole.copy(
                    lastMessage = last?.text ?: "",
                    lastMessageAt = last?.at ?: 0L
                )
            )
        }
        startReply()
    }

    fun applyEdit(target: ChatMessage, newText: String) {
        if (sending) return
        MessageStore.updateText(context, target.id, newText)
        MessageStore.deleteAfter(context, roleId, target.id)
        messages = MessageStore.load(context, roleId)
        val currentRole = ProfileStore.loadRoles(context).firstOrNull { it.id == roleId }
        if (currentRole != null) {
            ProfileStore.upsertRole(
                context,
                currentRole.copy(lastMessage = newText, lastMessageAt = target.at)
            )
        }
        startReply()
    }

    // 删除后刷新角色列表里的"最后一条消息"预览
    fun refreshRolePreview() {
        val currentRole = ProfileStore.loadRoles(context).firstOrNull { it.id == roleId } ?: return
        val last = messages.lastOrNull()
        val preview = when {
            last == null -> ""
            last.text.isNotBlank() -> last.text
            last.imagePath.isNotBlank() -> imagePreview
            last.fileName.isNotBlank() -> filePreview
            else -> ""
        }
        ProfileStore.upsertRole(
            context,
            currentRole.copy(lastMessage = preview, lastMessageAt = last?.at ?: 0L)
        )
    }

    fun deleteMessages(ids: Set<String>) {
        if (ids.isEmpty()) return
        MessageStore.deleteMany(context, ids)
        messages = MessageStore.load(context, roleId)
        refreshRolePreview()
    }

    fun exitSelection() {
        selecting = false
        selectedIds = emptySet()
    }

    BackHandler(enabled = selecting || textSelectionActive) {
        if (selecting) {
            exitSelection()
        } else {
            // 先取消文字选择，再按一次才退出聊天
            textSelectionActive = false
            selectionReset++
        }
    }

    // 多选模式下长按并拖动时，把手指经过的消息快速加进选中集合
    fun selectAtPosition(positionInList: Offset) {
        val root = positionInList + listRootOffset
        val id = itemBounds.entries.firstOrNull { it.value.contains(root) }?.key ?: return
        if (messages.none { it.id == id }) return
        if (id !in selectedIds) selectedIds = selectedIds + id
    }

    val sendAction: () -> Unit = {
        val text = input.trim()
        if (text.isNotEmpty() && !sending) {
            val config = ApiConfigStore.loadAll(context).firstOrNull { it.enabled }
            if (config == null || config.apiKey.isBlank() || config.model.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar(noApiMessage) }
            } else {
                append(
                    ChatMessage(
                        roleId = roleId,
                        fromUser = true,
                        text = text,
                        at = System.currentTimeMillis()
                    ),
                    text
                )
                input = ""
                DraftStore.clear(context, roleId)
                startReply()
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    Storage.saveMedia(context, uri, Storage.DIR_IMAGES)
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
                    Storage.saveMedia(context, uri, Storage.DIR_ATTACHMENTS)
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
        modifier = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                // 只观察、不消费：任何非长按、非拖动的点击都用来取消文字选择
                val down = awaitFirstDown(requireUnconsumed = false)
                var moved = false
                var upChange: PointerInputChange? = null
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (change.positionChange().getDistance() > viewConfiguration.touchSlop) {
                        moved = true
                    }
                    if (!change.pressed) {
                        upChange = change
                        break
                    }
                }
                val up = upChange
                if (up != null && !moved &&
                    up.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis
                ) {
                    selectionReset++
                    textSelectionActive = false
                }
            }
        },
        containerColor = colors.background,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                role = role,
                onBack = { if (selecting) exitSelection() else onBack() },
                onOpenSettings = onOpenSettings
            )
        },
        bottomBar = {
            if (selecting) {
                SelectionBar(
                    count = selectedIds.size,
                    onCancel = { exitSelection() },
                    onDelete = {
                        deleteMessages(selectedIds)
                        exitSelection()
                    }
                )
            } else {
                ChatInputBar(
                    input = input,
                    onInputChange = { input = it },
                    onAttach = { showAttachSheet = true },
                    onSend = sendAction
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onGloballyPositioned { listRootOffset = it.positionInRoot() }
                .pointerInput(selecting) {
                    if (selecting) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { position -> selectAtPosition(position) },
                            onDrag = { change, _ -> selectAtPosition(change.position) }
                        )
                    }
                }
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
                            } else if (typewriterEnabled) {
                                PoppingText(
                                    text = visibleText,
                                    color = colors.onSurface,
                                    backgroundColor = colors.surfaceVariant
                                )
                            } else {
                                Text(
                                    text = visibleText,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }
                }
            }

            itemsIndexed(
                items = messages.asReversed(),
                key = { _, message -> message.id }
            ) { index, message ->
                DisposableEffect(message.id) {
                    onDispose { itemBounds.remove(message.id) }
                }
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        itemBounds[message.id] = Rect(
                            coordinates.positionInRoot(),
                            Size(
                                coordinates.size.width.toFloat(),
                                coordinates.size.height.toFloat()
                            )
                        )
                    }
                ) {
                    MessageBubble(
                        message = message,
                        showRegenerate = index == 0 && !message.fromUser && !sending,
                        selecting = selecting,
                        selected = message.id in selectedIds,
                        selectionReset = selectionReset,
                        onClick = {
                            if (selecting) {
                                selectedIds = if (message.id in selectedIds) {
                                    selectedIds - message.id
                                } else {
                                    selectedIds + message.id
                                }
                            }
                        },
                        onTextLongPress = { textSelectionActive = true },
                        onLongPress = {
                            if (!selecting) {
                                focusManager.clearFocus()
                                textSelectionActive = false
                                selecting = true
                                selectedIds = setOf(message.id)
                            } else {
                                selectedIds = if (message.id in selectedIds) {
                                    selectedIds - message.id
                                } else {
                                    selectedIds + message.id
                                }
                            }
                        },
                        onDelete = { deletingMessage = message },
                        onCopy = {
                            clipboard.setText(AnnotatedString(message.text))
                            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                        },
                        onEdit = { editingMessage = message },
                        onRegenerate = { regenerate(message) }
                    )
                }
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

    val dialogMessage = editingMessage
    if (dialogMessage != null) {
        EditMessageDialog(
            initialText = dialogMessage.text,
            onDismiss = { editingMessage = null },
            onConfirm = { newText ->
                editingMessage = null
                applyEdit(dialogMessage, newText)
            }
        )
    }

    val pendingDelete = deletingMessage
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { deletingMessage = null },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.chat_delete_title),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.chat_delete_message),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deletingMessage = null
                    deleteMessages(setOf(pendingDelete.id))
                }) {
                    Text(stringResource(R.string.delete), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMessage = null }) {
                    Text(stringResource(R.string.cancel), color = colors.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun EditMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val fadeEnabled = AppPrefs.cursorFadeEnabled(LocalContext.current)
    val measurer = rememberTextMeasurer()
    val measureStyle = TextStyle(fontSize = 14.sp)
    val textPad = with(density) { 16.dp.toPx() }
    // 竖线宽 2dp，水滴尖对准其中心
    val caretHalf = with(density) { 1.dp.toPx() }
    var tfv by remember { mutableStateOf(TextFieldValue(initialText)) }
    var focused by remember { mutableStateOf(false) }
    val text = tfv.text

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = stringResource(R.string.chat_edit_title),
                fontSize = 17.sp,
                color = colors.onSurface
            )
        },
        text = {
            Column {
                val dialogBaseColors = LocalTextSelectionColors.current
                FallingCursorDrop(
                    modifier = Modifier.fillMaxWidth(),
                    dropOffset = { tap, boxSize ->
                        val maxWidth = (boxSize.width - textPad * 2)
                            .toInt()
                            .coerceAtLeast(1)
                        val layout = measurer.measure(
                            text = AnnotatedString(tfv.text),
                            style = measureStyle,
                            softWrap = true,
                            maxLines = Int.MAX_VALUE,
                            constraints = Constraints(maxWidth = maxWidth)
                        )
                        val offset = layout.getOffsetForPosition(
                            Offset(
                                (tap.x - textPad).coerceAtLeast(0f),
                                (tap.y - textPad).coerceAtLeast(0f)
                            )
                        )
                        val caretRect = layout.getCursorRect(offset)
                        Offset(
                            textPad + caretRect.left + caretHalf,
                            textPad + caretRect.bottom
                        )
                    },
                    caretProvider = caret@{ boxSize ->
                        if (!fadeEnabled || !focused || !tfv.selection.collapsed) {
                            return@caret null
                        }
                        val maxWidth = (boxSize.width - textPad * 2)
                            .toInt()
                            .coerceAtLeast(1)
                        val layout = measurer.measure(
                            text = AnnotatedString(tfv.text),
                            style = measureStyle,
                            softWrap = true,
                            maxLines = Int.MAX_VALUE,
                            constraints = Constraints(maxWidth = maxWidth)
                        )
                        val caretRect = layout.getCursorRect(tfv.selection.start)
                        val top = textPad + caretRect.top
                        val bottom = textPad + caretRect.bottom
                        if (bottom < 0f || top > boxSize.height) {
                            null
                        } else {
                            CaretInfo(
                                x = textPad + caretRect.left,
                                top = top,
                                bottom = bottom
                            )
                        }
                    }
                ) { handleColor ->
                    OutlinedTextField(
                        value = tfv,
                        onValueChange = { tfv = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focused = it.isFocused },
                        minLines = 3,
                        textStyle = TextStyle(fontSize = 14.sp, color = colors.onSurface),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface,
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            cursorColor = if (fadeEnabled) Color.Transparent else colors.primary,
                            selectionColors = TextSelectionColors(
                                handleColor = handleColor,
                                backgroundColor = dialogBaseColors.backgroundColor
                            )
                        )
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.chat_edit_warning),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = colors.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text(text = stringResource(R.string.save), color = colors.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel), color = colors.onSurfaceVariant)
            }
        }
    )
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

/** 多选模式下替代输入栏的底部操作条。 */
@Composable
private fun SelectionBar(
    count: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        HorizontalDivider(color = colors.outlineVariant, thickness = 0.8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.chat_selected_count, count),
                fontSize = 14.sp,
                color = colors.onSurface
            )
            Spacer(Modifier.weight(1f))
            val enabled = count > 0
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled, onClick = onDelete)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tint = if (enabled) {
                    colors.error
                } else {
                    colors.onSurfaceVariant.copy(alpha = 0.4f)
                }
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.delete),
                    fontSize = 14.sp,
                    color = tint
                )
            }
        }
    }
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

        var chatTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
        val chatPadH = with(LocalDensity.current) { 14.dp.toPx() }
        val chatPadV = with(LocalDensity.current) { 10.dp.toPx() }
        // 竖线宽 2dp，水滴尖对准其中心
        val chatCaretHalf = with(LocalDensity.current) { 1.dp.toPx() }
        val chatFade = AppPrefs.cursorFadeEnabled(LocalContext.current)
        var chatFocused by remember { mutableStateOf(false) }
        var chatValue by remember { mutableStateOf(TextFieldValue(input)) }
        if (chatValue.text != input) {
            chatValue = chatValue.copy(
                text = input,
                selection = TextRange(chatValue.selection.end.coerceIn(0, input.length))
            )
        }
        FallingCursorDrop(
            modifier = Modifier.weight(1f),
            caretProvider = caret@{ _ ->
                if (!chatFade || !chatFocused || !chatValue.selection.collapsed) {
                    return@caret null
                }
                val layout = chatTextLayout ?: return@caret null
                // 输入瞬间 layout 可能还是上一帧的，先夹取到合法范围
                val offset = chatValue.selection.start
                    .coerceIn(0, layout.layoutInput.text.length)
                val caretRect = layout.getCursorRect(offset)
                CaretInfo(
                    x = chatPadH + caretRect.left,
                    top = chatPadV + caretRect.top,
                    bottom = chatPadV + caretRect.bottom
                )
            },
            dropOffset = { tap, _ ->
                val layout = chatTextLayout
                if (layout == null) {
                    tap
                } else {
                    val offset = layout.getOffsetForPosition(
                        Offset(tap.x - chatPadH, tap.y - chatPadV)
                    )
                    val caret = layout.getCursorRect(offset)
                    Offset(chatPadH + caret.left + chatCaretHalf, chatPadV + caret.bottom)
                }
            }
        ) { _ ->
            BasicTextField(
                value = chatValue,
                onValueChange = { updated ->
                    chatValue = updated
                    if (updated.text != input) onInputChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .onFocusChanged { chatFocused = it.isFocused },
                textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = colors.onSurface),
                cursorBrush = SolidColor(
                    if (chatFade) Color.Transparent else colors.primary
                ),
                onTextLayout = { chatTextLayout = it },
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
        }

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    showRegenerate: Boolean,
    selecting: Boolean,
    selected: Boolean,
    selectionReset: Int,
    onTextLongPress: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isUser = message.fromUser
    val bubbleColor = if (isUser) colors.primary else colors.surfaceVariant
    val contentColor = if (isUser) colors.onPrimary else colors.onSurface
    val hasText = message.text.isNotBlank()
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (selecting) {
                    // 多选时点击切换；长按留给列表上的拖选手势
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier.combinedClickable(
                        interactionSource = null,
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongPress
                    )
                }
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(bubbleColor)
                    .then(
                        if (selecting) {
                            Modifier
                        } else {
                            // 气泡内部不算空白：吞掉长按，避免误进入多选
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onLongPress = {})
                            }
                        }
                    )
            ) {
                when {
                    message.imagePath.isNotBlank() -> {
                        val context = LocalContext.current
                        val image = remember(message.imagePath) {
                            ImageStore.loadBitmap(context, message.imagePath, 800)
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
                            // 与打字动画用同一套排版参数
                            if (selecting) {
                                // 多选时直接点文字也能切换勾选
                                Text(
                                    text = message.text,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    color = contentColor
                                )
                            } else {
                                // 正常状态长按文字仍是选词复制；
                                // 点击屏幕其它位置后 key 变化，SelectionContainer 重建、选择被清空
                                key(selectionReset) {
                                    Box(
                                        modifier = Modifier.pointerInput(Unit) {
                                            awaitEachGesture {
                                                // 只观察长按，用来知道“现在有选中的文字”，供返回键使用
                                                val down = awaitFirstDown(requireUnconsumed = false)
                                                if (awaitLongPressOrCancellation(down.id) != null) {
                                                    onTextLongPress()
                                                }
                                            }
                                        }
                                    ) {
                                        SelectionContainer {
                                            Text(
                                                text = message.text,
                                                fontSize = 15.sp,
                                                lineHeight = 21.sp,
                                                color = contentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!selecting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasText) {
                        MessageAction(
                            icon = Icons.Outlined.ContentCopy,
                            label = stringResource(R.string.chat_copy),
                            onClick = onCopy
                        )
                        if (isUser) {
                            MessageAction(
                                icon = Icons.Outlined.Edit,
                                label = stringResource(R.string.chat_edit),
                                onClick = onEdit
                            )
                        } else if (showRegenerate) {
                            MessageAction(
                                icon = Icons.Outlined.Refresh,
                                label = stringResource(R.string.chat_regenerate),
                                onClick = onRegenerate
                            )
                        }
                    }
                    MessageAction(
                        icon = Icons.Outlined.Delete,
                        label = stringResource(R.string.delete),
                        onClick = onDelete
                    )
                }
            }
        }

        if (selecting) {
            SelectionMark(
                selected = selected,
                modifier = Modifier
                    .align(if (isUser) Alignment.TopStart else Alignment.TopEnd)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MessageAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.onSurfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/** 多选模式下消息旁的勾选圆点。 */
@Composable
private fun SelectionMark(selected: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Canvas(modifier = modifier.size(20.dp)) {
        val radius = size.minDimension / 2f
        if (selected) {
            drawCircle(color = colors.primary, radius = radius)
            val w = size.width
            drawLine(
                color = Color.White,
                start = Offset(w * 0.27f, w * 0.52f),
                end = Offset(w * 0.44f, w * 0.69f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.44f, w * 0.69f),
                end = Offset(w * 0.73f, w * 0.35f),
                strokeWidth = w * 0.12f,
                cap = StrokeCap.Round
            )
        } else {
            drawCircle(
                color = colors.outline,
                radius = radius - 0.75.dp.toPx(),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

/**
 * 打字动画：用文本排版引擎算出整段的位置，整段照常绘制，
 * 只把刚出现的最后一个字放大后叠画上去。除弹动瞬间外，渲染与整段 Text 完全一致，
 * 所以落库后不会重新排版。
 */
@Composable
private fun PoppingText(
    text: String,
    color: Color,
    backgroundColor: Color,
    animate: Boolean = true
) {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val style = LocalTextStyle.current.copy(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = color
    )
    // 气泡最大宽度 280dp 减去左右各 13dp 内边距
    val maxWidth = with(density) { 254.dp.roundToPx() }
    val constraints = remember(maxWidth) { Constraints(maxWidth = maxWidth) }

    val layout = remember(text, style, constraints) {
        measurer.measure(AnnotatedString(text), style, constraints = constraints)
    }
    val lastChar = remember(text, style) {
        if (text.isEmpty()) null else measurer.measure(AnnotatedString(text.takeLast(1)), style)
    }

    val scale = remember { Animatable(1f) }
    LaunchedEffect(text.length) {
        if (animate && text.isNotEmpty()) {
            scale.snapTo(1.5f)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.4f,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Canvas(
        modifier = Modifier.size(
            width = with(density) { layout.size.width.toDp() },
            height = with(density) { layout.size.height.toDp() }
        )
    ) {
        drawText(layout)

        val charLayout = lastChar
        if (animate && charLayout != null && scale.value != 1f) {
            val box = layout.getBoundingBox(text.length - 1)
            if (box.width > 0f) {
                // 先抹掉原位上的字，缩小时才不会露出底下的字形
                drawRect(
                    color = backgroundColor,
                    topLeft = box.topLeft,
                    size = box.size
                )
                val center = box.center
                scale(scale.value, scale.value, pivot = center) {
                    drawText(
                        textLayoutResult = charLayout,
                        topLeft = Offset(
                            center.x - charLayout.size.width / 2f,
                            center.y - charLayout.size.height / 2f
                        )
                    )
                }
            }
        }
    }
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
