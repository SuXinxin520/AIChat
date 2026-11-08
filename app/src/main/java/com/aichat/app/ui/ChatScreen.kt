package com.aichat.app.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Language
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.ApiConfig
import com.aichat.app.data.ApiConfigStore
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.ChatApi
import com.aichat.app.data.ChatAvatar
import com.aichat.app.data.ChatAvatarContext
import com.aichat.app.data.ChatMessage
import com.aichat.app.data.ChatStreamEvent
import com.aichat.app.data.ChatSummary
import com.aichat.app.data.DraftStore
import com.aichat.app.data.MessageStore
import com.aichat.app.data.ModelTokenizer
import com.aichat.app.data.ProfileStore
import com.aichat.app.data.PromptLibraryStore
import com.aichat.app.data.Role
import com.aichat.app.data.RoleChatSettings
import com.aichat.app.data.RoleChatSettingsStore
import com.aichat.app.data.Storage
import com.aichat.app.data.SummaryStore
import com.aichat.app.data.UsageStore
import com.aichat.app.data.ApiUsage
import com.aichat.app.util.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val SUMMARY_RESERVE = 2_500
private const val TRIM_RESERVE = 500
private const val SUMMARY_SYSTEM_PROMPT =
    "You are a context compressor for a long role-play chat. Merge the previous summary " +
        "(if any) and the new old messages into one concise summary. Keep character names, " +
        "user persona details, important events, relationship changes, unresolved threads, " +
        "and tone/format rules. Do not invent facts. Write the summary in the same language " +
        "as the conversation. Aim for 200-400 words."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    roleId: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val rootView = LocalView.current
    val keepAwakeMode = AppPrefs.chatKeepAwake(context)
    var keepAwakeTick by remember { mutableIntStateOf(0) }

    // 聊天页常亮：always 一直保持；数字模式在无操作 N 分钟后允许系统息屏
    LaunchedEffect(keepAwakeMode, keepAwakeTick) {
        if (keepAwakeMode == AppPrefs.KEEP_AWAKE_SYSTEM) {
            rootView.keepScreenOn = false
            return@LaunchedEffect
        }
        rootView.keepScreenOn = true
        if (keepAwakeMode != AppPrefs.KEEP_AWAKE_ALWAYS) {
            val minutes = keepAwakeMode.toIntOrNull() ?: 5
            delay(minutes.coerceAtLeast(1) * 60_000L)
            rootView.keepScreenOn = false
        }
    }
    DisposableEffect(Unit) {
        onDispose { rootView.keepScreenOn = false }
    }

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
    var summary by remember(roleId) { mutableStateOf(SummaryStore.load(context, roleId)) }
    var lastUsage by remember(roleId) { mutableStateOf(UsageStore.load(context, roleId)) }
    var roleChatSettings by remember(roleId) {
        mutableStateOf(RoleChatSettingsStore.load(context, roleId))
    }
    var summarizing by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    var summaryExpanded by remember { mutableStateOf(false) }
    var showAttachSheet by remember { mutableStateOf(false) }
    // 长按思考图标时的强度浮层：放在页面根部，避免 Popup 打断拖动手势
    val thinkingPicker = remember { ThinkingPickerState() }
    // 用量气泡也放在页面根部，方便精确按圆心定位
    val usagePopup = remember { UsagePopupState() }
    var manualCompressing by remember { mutableStateOf(false) }

    LaunchedEffect(usagePopup.visible) {
        if (usagePopup.visible) {
            delay(3000)
            usagePopup.visible = false
        }
    }
    BackHandler(enabled = usagePopup.visible) { usagePopup.visible = false }

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
    // 输入栏在窗口里的位置：点它以外的地方都应该收起键盘
    var inputBarBounds by remember { mutableStateOf(Rect.Zero) }
    var scaffoldRootOffset by remember { mutableStateOf(Offset.Zero) }

    var sending by remember { mutableStateOf(false) }
    // streamingText 是已收到的全文，shownChars 是已经"打"出来的字数
    var streamingText by remember { mutableStateOf("") }
    var streamingReasoning by remember { mutableStateOf("") }
    var reasoningStartedAt by remember { mutableStateOf(0L) }
    var reasoningEndedAt by remember { mutableStateOf(0L) }
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
    val promptLibrary = remember { PromptLibraryStore.load(context) }
    val userPrompt = if (profile.promptIds.isEmpty()) {
        profile.prompt.trim()
    } else {
        promptLibrary.composePrompt(profile.promptIds)
    }
    val rolePrompt = if (role.promptIds.isEmpty()) {
        role.prompt.trim()
    } else {
        promptLibrary.composePrompt(role.promptIds)
    }
    val userNameLine = if (profile.name.isNotBlank()) {
        stringResource(R.string.sys_user_name, profile.name)
    } else {
        ""
    }
    val userAboutLine = if (userPrompt.isNotBlank()) {
        stringResource(R.string.sys_about_user, userPrompt)
    } else {
        ""
    }
    val roleNameLine = if (role.name.isNotBlank()) {
        stringResource(R.string.sys_role_name, role.name)
    } else {
        ""
    }
    val roleLine = if (rolePrompt.isNotBlank()) {
        stringResource(R.string.sys_role, rolePrompt)
    } else {
        ""
    }
    val systemPrompt = listOf(userNameLine, userAboutLine, roleNameLine, roleLine)
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

    var activeConfig by remember {
        mutableStateOf(ApiConfigStore.loadAll(context).firstOrNull { it.enabled })
    }
    val isDeepSeek = activeConfig?.let { config ->
        config.baseUrl.contains("deepseek", ignoreCase = true) ||
            config.model.startsWith("deepseek", ignoreCase = true)
    } == true
    val canWebSearch = activeConfig?.baseUrl
        ?.contains("api.deepseek.com", ignoreCase = true) == true
    val contextLimit = activeConfig?.contextLimit ?: ApiConfig.DEFAULT_CONTEXT_LIMIT
    val tokenModel = activeConfig?.model.orEmpty()
    // 角色/用户头像作为隐形的 user 消息随请求发送，只给支持视觉的模型
    val avatarContext = remember(
        role.id,
        role.name,
        role.avatarPath,
        profile.name,
        profile.avatarPath,
        tokenModel
    ) {
        if (tokenModel.isBlank() || !ChatApi.supportsVision(tokenModel)) {
            null
        } else {
            val images = buildList {
                role.avatarPath?.takeIf { it.isNotBlank() }?.let { path ->
                    add(
                        ChatAvatar(
                            context.getString(R.string.sys_avatar_role, role.name),
                            path
                        )
                    )
                }
                profile.avatarPath?.takeIf { it.isNotBlank() }?.let { path ->
                    add(
                        ChatAvatar(
                            context.getString(R.string.sys_avatar_user, profile.name),
                            path
                        )
                    )
                }
            }
            if (images.isEmpty()) {
                null
            } else {
                ChatAvatarContext(
                    intro = context.getString(R.string.sys_avatar_intro),
                    images = images
                )
            }
        }
    }
    val avatarTokens = remember(avatarContext, tokenModel) {
        if (tokenModel.isBlank() || avatarContext == null) {
            0
        } else {
            val labels = avatarContext.intro +
                avatarContext.images.joinToString("") { it.label }
            avatarContext.images.size * ModelTokenizer.imageTokens(tokenModel, 1) +
                ModelTokenizer.count(labels, tokenModel)
        }
    }
    val coveredIds = summary?.coveredIds?.toSet().orEmpty()
    val pendingMessages = messages.filter { it.id !in coveredIds }
    val systemTokens = remember(systemPrompt, tokenModel) {
        ModelTokenizer.count(systemPrompt, tokenModel)
    }
    val summaryTokens = remember(summary, tokenModel) {
        ModelTokenizer.count(summary?.text.orEmpty(), tokenModel)
    }
    val historyTokens = remember(pendingMessages, tokenModel) {
        pendingMessages.sumOf { message ->
            var tokens = if (message.text.isBlank()) {
                0
            } else {
                ModelTokenizer.countMessage(message.text, tokenModel)
            }
            if (message.imagePath.isNotBlank()) {
                tokens += ModelTokenizer.imageTokens(tokenModel, 1)
            }
            if (message.filePath.isNotBlank()) {
                tokens += fileTokenEstimate(message.fileName, tokenModel)
            }
            tokens
        }
    }
    val estimatedTokens = systemTokens + summaryTokens + historyTokens + avatarTokens +
        ModelTokenizer.count(input, tokenModel)
    // 有接口返回的真实用量时取两者较大值，圆环不会低估
    val usedTokens = maxOf(estimatedTokens, lastUsage?.total ?: 0)

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
        val chatConfig = config.copy(
            thinkingEnabled = roleChatSettings.thinkingEnabled,
            thinkingEffort = roleChatSettings.thinkingEffort
        )

        sending = true
        summarizing = false
        streamingText = ""
        streamingReasoning = ""
        reasoningStartedAt = 0L
        reasoningEndedAt = 0L
        shownChars = 0

        scope.launch {
            try {
                val limit = config.contextLimit
                    .coerceIn(ApiConfig.MIN_CONTEXT_LIMIT, ApiConfig.MAX_CONTEXT_LIMIT)

                var currentSummary = SummaryStore.load(context, roleId)
                var effectiveSystem = buildSummarySystem(
                    systemPrompt,
                    currentSummary?.text.orEmpty()
                )
                var pending = MessageStore.load(context, roleId).filter { message ->
                    currentSummary?.coveredIds?.contains(message.id) != true
                }
                var estimate = estimateContextTokens(
                    effectiveSystem,
                    pending,
                    config.model,
                    avatarTokens
                )

                // 达到 API 配置的阈值时，把最旧的一段滚动摘要掉，保留最近一段原文
                val thresholdPercent = config.compressThreshold.coerceIn(
                    ApiConfig.MIN_COMPRESS_THRESHOLD,
                    ApiConfig.MAX_COMPRESS_THRESHOLD
                )
                val triggerTokens = (limit.toLong() * thresholdPercent / 100L)
                    .toInt()
                    .coerceAtLeast(1)
                if (config.autoCompress && estimate > triggerTokens) {
                    val systemTokens = ModelTokenizer.count(effectiveSystem, config.model)
                    val keepBudget =
                        (triggerTokens - systemTokens - avatarTokens - SUMMARY_RESERVE)
                            .coerceAtLeast((triggerTokens * 0.20f).toInt())
                            .coerceAtMost((triggerTokens * 0.65f).toInt())
                    val keep = takeRecentMessages(pending, keepBudget, config.model)
                    val keepIds = keep.mapTo(HashSet()) { it.id }
                    val older = pending.filter { it.id !in keepIds && it.text.isNotBlank() }

                    if (older.isNotEmpty()) {
                        summarizing = true
                        val transcript = buildSummaryTranscript(
                            roleName = role.name,
                            previous = currentSummary?.text.orEmpty(),
                            older = older
                        )
                        val newText = runCatching {
                            ChatApi.reply(
                                context = context,
                                config = chatConfig,
                                systemPrompt = SUMMARY_SYSTEM_PROMPT,
                                history = listOf(ChatMessage(fromUser = true, text = transcript)),
                                useThinking = false
                            )
                        }.getOrNull()?.trim().orEmpty().take(4000)
                        summarizing = false

                        if (newText.isNotEmpty()) {
                            val covered = (
                                currentSummary?.coveredIds.orEmpty() + older.map { it.id }
                                ).distinct()
                            val saved = ChatSummary(
                                roleId = roleId,
                                text = newText,
                                coveredIds = covered,
                                updatedAt = System.currentTimeMillis()
                            )
                            SummaryStore.save(context, saved)
                            summary = saved
                            currentSummary = saved
                            effectiveSystem = buildSummarySystem(systemPrompt, newText)
                            pending = MessageStore.load(context, roleId).filter { message ->
                                message.id !in covered
                            }
                            estimate = estimateContextTokens(
                                effectiveSystem,
                                pending,
                                config.model,
                                avatarTokens
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.context_summarize_failed)
                                )
                            }
                        }
                    }
                }

                // 联网搜索：把搜索结果作为临时的 system 上下文，不写进历史
                if (canWebSearch && roleChatSettings.webSearchEnabled) {
                    val query = pending.lastOrNull { it.fromUser && it.text.isNotBlank() }?.text
                    if (!query.isNullOrBlank()) {
                        searching = true
                        val webContext = runCatching {
                            ChatApi.webSearch(chatConfig, query)
                        }.getOrNull()
                        searching = false
                        if (webContext.isNullOrBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.web_search_failed)
                                )
                            }
                        } else {
                            effectiveSystem = effectiveSystem + "\n\n" + webContext
                            estimate = estimateContextTokens(
                                effectiveSystem,
                                pending,
                                config.model,
                                avatarTokens
                            )
                        }
                    }
                }

                // 摘要失败或仍然超限时，本次请求只带最近一段，避免直接报上下文超限
                var history = pending
                if (estimate > limit) {
                    val systemTokens = ModelTokenizer.count(effectiveSystem, config.model)
                    val trimBudget = (limit - systemTokens - avatarTokens - TRIM_RESERVE)
                        .coerceAtLeast((limit * 0.15f).toInt())
                    history = takeRecentMessages(pending, trimBudget, config.model)
                    if (history.size < pending.size) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.context_trimmed)
                            )
                        }
                    }
                }

                val onUsage: (Int, Int) -> Unit = { prompt, completion ->
                    val usage = ApiUsage(
                        promptTokens = prompt,
                        completionTokens = completion,
                        updatedAt = System.currentTimeMillis()
                    )
                    UsageStore.save(context, roleId, usage)
                    scope.launch { lastUsage = usage }
                }

                if (AppPrefs.streamEnabled(context)) {
                    ChatApi.streamReply(
                        context = context,
                        config = chatConfig,
                        systemPrompt = effectiveSystem,
                        history = history,
                        avatars = avatarContext,
                        onUsage = onUsage
                    ).collect { event ->
                        when (event) {
                            is ChatStreamEvent.Content -> {
                                if (reasoningStartedAt > 0L && reasoningEndedAt == 0L) {
                                    reasoningEndedAt = System.currentTimeMillis()
                                }
                                streamingText += event.text
                            }

                            is ChatStreamEvent.Reasoning -> {
                                if (reasoningStartedAt == 0L) {
                                    reasoningStartedAt = System.currentTimeMillis()
                                }
                                streamingReasoning += event.text
                            }
                        }
                    }
                } else {
                    val startedAt = System.currentTimeMillis()
                    val result = ChatApi.replyDetailed(
                        context = context,
                        config = chatConfig,
                        systemPrompt = effectiveSystem,
                        history = history,
                        avatars = avatarContext,
                        onUsage = onUsage
                    )
                    streamingText = result.content
                    streamingReasoning = result.reasoning
                    if (result.reasoning.isNotBlank()) {
                        reasoningStartedAt = startedAt
                        reasoningEndedAt = System.currentTimeMillis()
                    }
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
                            reasoning = streamingReasoning.trim(),
                            reasoningMs = if (reasoningStartedAt > 0L) {
                                (reasoningEndedAt.takeIf { it > 0L }
                                    ?: System.currentTimeMillis()) - reasoningStartedAt
                            } else {
                                0L
                            },
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
                summarizing = false
                searching = false
                streamingText = ""
                streamingReasoning = ""
                reasoningStartedAt = 0L
                reasoningEndedAt = 0L
                shownChars = 0
            }
        }
    }

    /** 长按用量圆环：不等自动阈值，立即把较早的一半消息压缩成摘要。 */
    fun manualCompress() {
        if (sending || summarizing || searching || manualCompressing) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.context_compress_busy))
            }
            return
        }
        val config = ApiConfigStore.loadAll(context).firstOrNull { it.enabled }
        if (config == null || config.apiKey.isBlank() || config.model.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar(noApiMessage) }
            return
        }

        val currentSummary = SummaryStore.load(context, roleId)
        val covered = currentSummary?.coveredIds.orEmpty().toSet()
        val pending = MessageStore.load(context, roleId).filter { it.id !in covered }
        val keepCount = (pending.size / 2).coerceIn(2, 10)
        if (pending.size <= keepCount) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.context_compress_empty))
            }
            return
        }
        val older = pending.dropLast(keepCount).filter { it.text.isNotBlank() }
        if (older.isEmpty()) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.context_compress_empty))
            }
            return
        }

        val transcript = buildSummaryTranscript(
            roleName = role.name,
            previous = currentSummary?.text.orEmpty(),
            older = older
        )
        manualCompressing = true
        scope.launch {
            snackbarHostState.showSnackbar(context.getString(R.string.context_summarizing))
        }
        scope.launch {
            try {
                val newText = runCatching {
                    ChatApi.reply(
                        context = context,
                        config = config.copy(thinkingEnabled = false),
                        systemPrompt = SUMMARY_SYSTEM_PROMPT,
                        history = listOf(ChatMessage(fromUser = true, text = transcript)),
                        useThinking = false
                    )
                }.getOrNull()?.trim().orEmpty().take(4000)

                if (newText.isNotEmpty()) {
                    val newCovered = (covered + older.map { it.id }).distinct()
                    val saved = ChatSummary(
                        roleId = roleId,
                        text = newText,
                        coveredIds = newCovered,
                        updatedAt = System.currentTimeMillis()
                    )
                    SummaryStore.save(context, saved)
                    summary = saved
                    // 手动压缩后旧的接口 usage 已经过时，让圆环回到当前估算
                    UsageStore.clear(context, roleId)
                    lastUsage = null
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.context_compress_done)
                    )
                } else {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.context_compress_failed)
                    )
                }
            } finally {
                manualCompressing = false
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

    val pendingAttachment = messages.lastOrNull()?.let { message ->
        message.fromUser && (message.imagePath.isNotBlank() || message.filePath.isNotBlank())
    } == true && !sending

    val sendAction: () -> Unit = {
        val text = input.trim()
        if (!sending && (text.isNotEmpty() || pendingAttachment)) {
            val config = ApiConfigStore.loadAll(context).firstOrNull { it.enabled }
            if (config == null || config.apiKey.isBlank() || config.model.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar(noApiMessage) }
            } else {
                if (text.isNotEmpty()) {
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
                }
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

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { scaffoldRootOffset = it.positionInRoot() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // 只观察、不消费：短按用来取消文字选择，点输入栏以外还收起键盘
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // 任何触摸都算一次操作，重置常亮计时
                    keepAwakeTick++
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
                        val inInput = inputBarBounds.contains(up.position + scaffoldRootOffset)
                        if (!inInput) {
                            focusManager.clearFocus()
                        }
                    }
                }
            },
        containerColor = colors.background,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        topBar = {
            ChatTopBar(
                role = role,
                contextUsed = usedTokens,
                contextLimit = contextLimit,
                thinkingVisible = isDeepSeek,
                thinkingEnabled = roleChatSettings.thinkingEnabled,
                thinkingEffort = roleChatSettings.thinkingEffort,
                webSearchVisible = canWebSearch,
                webSearchEnabled = roleChatSettings.webSearchEnabled,
                onWebSearchToggle = {
                    val updated = roleChatSettings.copy(
                        webSearchEnabled = !roleChatSettings.webSearchEnabled
                    )
                    roleChatSettings = updated
                    RoleChatSettingsStore.save(context, roleId, updated)
                },
                onThinkingToggle = {
                    val updated = roleChatSettings.copy(
                        thinkingEnabled = !roleChatSettings.thinkingEnabled
                    )
                    roleChatSettings = updated
                    RoleChatSettingsStore.save(context, roleId, updated)
                },
                onThinkingEffortChange = { effort ->
                    val updated = roleChatSettings.copy(thinkingEffort = effort)
                    roleChatSettings = updated
                    RoleChatSettingsStore.save(context, roleId, updated)
                },
                onBack = { if (selecting) exitSelection() else onBack() },
                onOpenSettings = onOpenSettings,
                onManualCompress = { manualCompress() },
                thinkingPicker = thinkingPicker,
                usagePopup = usagePopup
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
                Box(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        inputBarBounds = Rect(
                            coordinates.positionInRoot(),
                            Size(
                                coordinates.size.width.toFloat(),
                                coordinates.size.height.toFloat()
                            )
                        )
                    }
                ) {
                    ChatInputBar(
                        input = input,
                        onInputChange = {
                            input = it
                            keepAwakeTick++
                        },
                        onAttach = { showAttachSheet = true },
                        canSendEmpty = pendingAttachment,
                        onSend = sendAction
                    )
                }
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
                },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 反向布局：索引 0 在最底部，所以"输入中"气泡排在最前
            if (sending) {
                item("typing") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val hasReasoning = streamingReasoning.isNotBlank()
                        if (hasReasoning && !summarizing && !searching) {
                            ReasoningBlock(
                                text = streamingReasoning,
                                durationMs = if (reasoningStartedAt > 0L && reasoningEndedAt > 0L) {
                                    reasoningEndedAt - reasoningStartedAt
                                } else {
                                    0L
                                },
                                thinking = streamingText.isEmpty()
                            )
                            Spacer(Modifier.height(5.dp))
                        }
                        if (streamingText.isNotEmpty() || !hasReasoning) {
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
                                        when {
                                            summarizing -> {
                                                Text(
                                                    text = stringResource(
                                                        R.string.context_summarizing
                                                    ),
                                                    fontSize = 14.sp,
                                                    color = colors.onSurfaceVariant
                                                )
                                            }

                                            searching -> {
                                                Text(
                                                    text = stringResource(
                                                        R.string.web_searching
                                                    ),
                                                    fontSize = 14.sp,
                                                    color = colors.onSurfaceVariant
                                                )
                                            }

                                            else -> TypingIndicator(
                                                color = colors.onSurfaceVariant
                                            )
                                        }
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

            // 反向下标列表的末尾就是最上方：显示更早对话的摘要
            val currentSummary = summary
            if (currentSummary != null) {
                item("context_summary") {
                    ContextSummaryCard(
                        summary = currentSummary,
                        coveredCount = currentSummary.coveredIds.count { id ->
                            messages.any { it.id == id }
                        },
                        expanded = summaryExpanded,
                        onToggle = { summaryExpanded = !summaryExpanded }
                    )
                }
            }
        }
    }

    if (thinkingPicker.visible) {
        ThinkingEffortOverlay(
            anchor = thinkingPicker.anchor,
            hovered = thinkingPicker.hovered,
            currentEffort = roleChatSettings.thinkingEffort
        )
    }
    if (usagePopup.visible) {
        UsageTooltipOverlay(
            anchor = usagePopup.anchor,
            used = usedTokens,
            limit = contextLimit,
            onDismiss = { usagePopup.visible = false }
        )
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

private fun buildSummarySystem(base: String, summaryText: String): String =
    if (summaryText.isBlank()) {
        base
    } else {
        base + "\n\nEarlier conversation summary:\n" + summaryText
    }

private fun fileTokenEstimate(fileName: String, model: String): Int {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when {
        ext in setOf("jpg", "jpeg", "png", "gif", "webp") ->
            ModelTokenizer.imageTokens(model, 1)
        ext == "pdf" -> ModelTokenizer.imageTokens(model, 5)
        else -> 8_000
    }
}

private fun estimateContextTokens(
    system: String,
    messages: List<ChatMessage>,
    model: String,
    extraTokens: Int = 0
): Int = extraTokens + ModelTokenizer.count(system, model) + messages.sumOf { message ->
    var tokens = if (message.text.isBlank()) {
        0
    } else {
        ModelTokenizer.countMessage(message.text, model)
    }
    if (message.imagePath.isNotBlank()) {
        tokens += ModelTokenizer.imageTokens(model, 1)
    }
    if (message.filePath.isNotBlank()) {
        tokens += fileTokenEstimate(message.fileName, model)
    }
    tokens
}

/** 从最新消息往前收，尽量装满预算；至少保留一条，避免把刚发的消息裁掉。 */
private fun takeRecentMessages(
    messages: List<ChatMessage>,
    budget: Int,
    model: String
): List<ChatMessage> {
    if (messages.isEmpty()) return emptyList()
    val selected = ArrayDeque<ChatMessage>()
    var total = 0
    for (message in messages.asReversed()) {
        var tokens = if (message.text.isBlank()) {
            0
        } else {
            ModelTokenizer.countMessage(message.text, model)
        }
        if (message.imagePath.isNotBlank()) {
            tokens += ModelTokenizer.imageTokens(model, 1)
        }
        if (message.filePath.isNotBlank()) {
            tokens += fileTokenEstimate(message.fileName, model)
        }
        if (selected.isNotEmpty() && total + tokens > budget) break
        total += tokens
        selected.addFirst(message)
    }
    return selected
}

private fun buildSummaryTranscript(
    roleName: String,
    previous: String,
    older: List<ChatMessage>
): String {
    val builder = StringBuilder()
    if (previous.isNotBlank()) {
        builder.append("Previous summary:\n")
            .append(previous.trim())
            .append("\n\n")
    }
    builder.append("New old messages:\n")
    older.forEach { message ->
        if (message.text.isNotBlank()) {
            builder.append(if (message.fromUser) "User: " else "${roleName.ifBlank { "Character" }}: ")
                .append(message.text.trim())
                .append("\n\n")
        }
    }
    return builder.toString()
}

@Composable
private fun ChatTopBar(
    role: Role,
    contextUsed: Int,
    contextLimit: Int,
    thinkingVisible: Boolean,
    thinkingEnabled: Boolean,
    thinkingEffort: String,
    webSearchVisible: Boolean,
    webSearchEnabled: Boolean,
    onWebSearchToggle: () -> Unit,
    onThinkingToggle: () -> Unit,
    onThinkingEffortChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onManualCompress: () -> Unit,
    thinkingPicker: ThinkingPickerState,
    usagePopup: UsagePopupState
) {
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

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = role.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Box {
                ContextUsageRing(
                    used = contextUsed,
                    limit = contextLimit,
                    onClick = { usagePopup.visible = true },
                    onLongClick = onManualCompress,
                    onBounds = { bounds -> usagePopup.anchor = bounds }
                )
            }
            if (thinkingVisible) {
                Spacer(Modifier.width(2.dp))
                ThinkingControl(
                    enabled = thinkingEnabled,
                    effort = thinkingEffort,
                    onToggle = onThinkingToggle,
                    onEffortChange = onThinkingEffortChange,
                    picker = thinkingPicker
                )
            }
            if (webSearchVisible) {
                IconButton(
                    onClick = onWebSearchToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Language,
                        contentDescription = stringResource(R.string.web_search_title),
                        tint = if (webSearchEnabled) {
                            colors.primary
                        } else {
                            colors.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

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
    }
}

/** 长按思考图标时，页面根部浮层的状态。 */
private class ThinkingPickerState {
    var visible by mutableStateOf(false)
    var hovered by mutableIntStateOf(0)
    var anchor by mutableStateOf(Rect.Zero)
}

/** 用量气泡的显示状态和圆环位置。 */
private class UsagePopupState {
    var visible by mutableStateOf(false)
    var anchor by mutableStateOf(Rect.Zero)
}

/** 圆角矩形 + 顶部居中小三角，作为气泡的整体背景形状。 */
private class TooltipShape(
    private val arrowWidthPx: Float,
    private val arrowHeightPx: Float,
    private val cornerRadiusPx: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cardTop = arrowHeightPx
        val radius = cornerRadiusPx.coerceAtMost(
            minOf(size.width / 2f, (size.height - cardTop) / 2f)
        )
        val card = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = cardTop,
                    right = size.width,
                    bottom = size.height,
                    topLeftCornerRadius = CornerRadius(radius, radius),
                    topRightCornerRadius = CornerRadius(radius, radius),
                    bottomLeftCornerRadius = CornerRadius(radius, radius),
                    bottomRightCornerRadius = CornerRadius(radius, radius)
                )
            )
        }
        val arrow = Path().apply {
            moveTo(size.width / 2f - arrowWidthPx / 2f, cardTop)
            lineTo(size.width / 2f, 0f)
            lineTo(size.width / 2f + arrowWidthPx / 2f, cardTop)
            close()
        }
        val combined = Path().apply {
            op(card, arrow, PathOperation.Union)
        }
        return Outline.Generic(combined)
    }
}

/** 角色名旁的 DeepSeek 思考开关和强度选择：点击开关，长按下滑选强度。 */
@Composable
private fun ThinkingControl(
    enabled: Boolean,
    effort: String,
    onToggle: () -> Unit,
    onEffortChange: (String) -> Unit,
    picker: ThinkingPickerState
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val options = listOf(
        "low" to R.string.thinking_effort_low,
        "medium" to R.string.thinking_effort_medium,
        "high" to R.string.thinking_effort_high
    )
    var iconBounds by remember { mutableStateOf(Rect.Zero) }
    val rowHeightPx = with(density) { 40.dp.toPx() }
    // 浮层顶部：8dp 间距 + 8dp 箭头，之后三行直接贴住上下边
    val firstRowOffsetPx = with(density) { 16.dp.toPx() }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .onGloballyPositioned { coordinates ->
                iconBounds = Rect(
                    coordinates.positionInRoot(),
                    Size(
                        coordinates.size.width.toFloat(),
                        coordinates.size.height.toFloat()
                    )
                )
            }
            .clickable(onClick = onToggle)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        picker.hovered = 0
                        picker.anchor = iconBounds
                        picker.visible = true
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        // 按手指当前所在的浮层行高亮，而不是按拖动距离
                        val pointerY = iconBounds.top + change.position.y
                        val firstRowTop = iconBounds.bottom + firstRowOffsetPx
                        picker.hovered = when {
                            pointerY < firstRowTop + rowHeightPx -> 0
                            pointerY < firstRowTop + rowHeightPx * 2 -> 1
                            else -> 2
                        }.coerceIn(0, options.lastIndex)
                    },
                    onDragEnd = {
                        onEffortChange(options[picker.hovered].first)
                        picker.visible = false
                    },
                    onDragCancel = { picker.visible = false }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Psychology,
            contentDescription = stringResource(R.string.thinking_title),
            tint = if (enabled) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** 长按拖动时显示在思考图标下方的三档强度浮层。 */
@Composable
private fun ThinkingEffortOverlay(
    anchor: Rect,
    hovered: Int,
    currentEffort: String
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val options = listOf(
        "low" to R.string.thinking_effort_low,
        "medium" to R.string.thinking_effort_medium,
        "high" to R.string.thinking_effort_high
    )
    val popupWidth = 168.dp
    val popupWidthPx = with(density) { popupWidth.toPx() }
    val gapPx = with(density) { 8.dp.toPx() }
    val edgePx = with(density) { 8.dp.toPx() }
    val arrowWidthPx = with(density) { 16.dp.toPx() }
    val arrowHeightPx = with(density) { 8.dp.toPx() }
    val cornerPx = with(density) { 12.dp.toPx() }
    val shape = remember(arrowWidthPx, arrowHeightPx, cornerPx) {
        TooltipShape(arrowWidthPx, arrowHeightPx, cornerPx)
    }
    val x = (anchor.center.x - popupWidthPx / 2f)
        .coerceIn(edgePx, (screenWidthPx - popupWidthPx - edgePx).coerceAtLeast(edgePx))

    Surface(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = x.roundToInt(),
                    y = (anchor.bottom + gapPx).roundToInt()
                )
            }
            .width(popupWidth),
        shape = shape,
        color = colors.surfaceContainerHigh,
        border = BorderStroke(0.7.dp, colors.outlineVariant),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            options.forEachIndexed { index, (value, label) ->
                val active = index == hovered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            if (active) {
                                colors.primary.copy(alpha = 0.12f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(label),
                        fontSize = 13.sp,
                        color = if (active) colors.primary else colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (currentEffort == value) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 用量气泡：全屏遮罩 + 按圆环圆心精确放置的气泡。 */
@Composable
private fun UsageTooltipOverlay(
    anchor: Rect,
    used: Int,
    limit: Int,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val gapPx = with(density) { 6.dp.roundToPx() }
    val marginPx = with(density) { 8.dp.roundToPx() }
    // ContextUsageRing 里实际画出来的圆环直径是 16dp
    val ringRadiusPx = with(density) { 8.dp.toPx() }
    val arrowWidthPx = with(density) { 16.dp.toPx() }
    val arrowHeightPx = with(density) { 8.dp.toPx() }
    val cornerPx = with(density) { 12.dp.toPx() }
    val shape = remember(arrowWidthPx, arrowHeightPx, cornerPx) {
        TooltipShape(arrowWidthPx, arrowHeightPx, cornerPx)
    }
    val percent = if (limit > 0) {
        (used * 100f / limit).toInt().coerceIn(0, 999)
    } else {
        0
    }

    Box(Modifier.fillMaxSize()) {
        // 点气泡外面关闭
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                }
        )
        AnchoredTooltipLayout(
            centerX = anchor.center.x,
            topY = anchor.center.y + ringRadiusPx + gapPx,
            marginPx = marginPx
        ) {
            Surface(
                shape = shape,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(0.7.dp, colors.outlineVariant),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.context_usage),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(
                            R.string.context_usage_detail,
                            used,
                            limit,
                            percent
                        ),
                        fontSize = 14.sp,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** 把内容水平中心对准 centerX，顶部放在 topY；超过屏幕时夹在边距内。 */
@Composable
private fun AnchoredTooltipLayout(
    centerX: Float,
    topY: Float,
    marginPx: Int,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = Modifier.fillMaxSize()) { measurables, constraints ->
        val maxContentWidth = (constraints.maxWidth - marginPx * 2).coerceAtLeast(0)
        val placeable = measurables.first().measure(
            Constraints(maxWidth = maxContentWidth)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            val maxX = (constraints.maxWidth - placeable.width - marginPx)
                .coerceAtLeast(marginPx)
            val x = (centerX - placeable.width / 2f)
                .roundToInt()
                .coerceIn(marginPx, maxX)
            placeable.place(x, topY.roundToInt())
        }
    }
}

/** 角色名右侧的上下文用量圆环：按占用比例变色，点击查看详情。 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ContextUsageRing(
    used: Int,
    limit: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onBounds: (Rect) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val fraction = if (limit > 0) {
        (used.toFloat() / limit).coerceIn(0f, 1f)
    } else {
        0f
    }
    val percent = (fraction * 100).toInt()
    val arcColor = when {
        fraction >= 0.85f -> colors.error
        fraction >= 0.6f -> colors.tertiary
        else -> colors.primary
    }
    val description = stringResource(R.string.context_usage_ring, percent)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .onGloballyPositioned { coordinates ->
                onBounds(
                    Rect(
                        coordinates.positionInRoot(),
                        Size(
                            coordinates.size.width.toFloat(),
                            coordinates.size.height.toFloat()
                        )
                    )
                )
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            val stroke = 2.2.dp.toPx()
            val radius = (size.minDimension - stroke) / 2f
            drawCircle(
                color = colors.outlineVariant,
                radius = radius,
                style = Stroke(width = stroke)
            )
            if (fraction > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/** 消息列表最上方的旧对话摘要卡片。 */
@Composable
private fun ContextSummaryCard(
    summary: ChatSummary,
    coveredCount: Int,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.55f))
            .border(0.8.dp, colors.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.context_summary_title),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = colors.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = summary.text,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = colors.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.context_summary_meta, coveredCount),
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onToggle) {
                Text(
                    text = stringResource(
                        if (expanded) {
                            R.string.context_summary_collapse
                        } else {
                            R.string.context_summary_expand
                        }
                    ),
                    fontSize = 12.sp,
                    color = colors.primary
                )
            }
        }
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
    canSendEmpty: Boolean,
    onSend: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canSend = input.isNotBlank() || canSendEmpty

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

        Spacer(Modifier.width(12.dp))

        var chatTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
        val chatDensity = LocalDensity.current
        val chatPadH = with(chatDensity) { 14.dp.toPx() }
        val chatPadV = with(chatDensity) { 10.dp.toPx() }
        // 竖线宽 2dp，水滴尖对准其中心
        val chatCaretHalf = with(chatDensity) { 1.dp.toPx() }
        val maxChatInputHeight = with(chatDensity) { (20.sp.toDp() * 4) + 20.dp }
        val chatFade = AppPrefs.cursorFadeEnabled(LocalContext.current)
        var chatFocused by remember { mutableStateOf(false) }
        var chatValue by remember { mutableStateOf(TextFieldValue(input)) }
        var chatInputHeight by remember { mutableIntStateOf(0) }
        // 自己管理滚动，才能知道真实滚动量；手滑/点选后光标都能跟上
        val chatScrollState = rememberScrollState()
        val chatFocus = remember { FocusRequester() }
        if (chatValue.text != input) {
            chatValue = chatValue.copy(
                text = input,
                selection = TextRange(chatValue.selection.end.coerceIn(0, input.length))
            )
        }
        // 光标变化时把它滚进可视区，替代 BasicTextField 的内部滚动
        LaunchedEffect(
            chatValue.selection,
            chatTextLayout,
            chatScrollState.maxValue,
            chatInputHeight
        ) {
            val layout = chatTextLayout ?: return@LaunchedEffect
            if (chatInputHeight <= 0) return@LaunchedEffect
            val offset = chatValue.selection.start
                .coerceIn(0, layout.layoutInput.text.length)
            val caret = layout.getCursorRect(offset)
            val current = chatScrollState.value.toFloat()
            val caretTop = chatPadV + caret.top - current
            val caretBottom = chatPadV + caret.bottom - current
            val target = when {
                caretTop < chatPadV -> current + caretTop - chatPadV
                caretBottom > chatInputHeight - chatPadV ->
                    current + caretBottom - (chatInputHeight - chatPadV)
                else -> current
            }.coerceIn(0f, chatScrollState.maxValue.toFloat())
            if (kotlin.math.abs(target - current) > 1f) {
                chatScrollState.scrollTo(target.toInt())
            }
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
                val scroll = chatScrollState.value.toFloat()
                CaretInfo(
                    x = chatPadH + caretRect.left,
                    top = chatPadV + caretRect.top - scroll,
                    bottom = chatPadV + caretRect.bottom - scroll
                )
            },
            dropOffset = { tap, _ ->
                val layout = chatTextLayout
                if (layout == null) {
                    tap
                } else {
                    val scroll = chatScrollState.value.toFloat()
                    val offset = layout.getOffsetForPosition(
                        Offset(
                            tap.x - chatPadH,
                            (tap.y + scroll - chatPadV).coerceAtLeast(0f)
                        )
                    )
                    val caret = layout.getCursorRect(offset)
                    Offset(
                        chatPadH + caret.left + chatCaretHalf,
                        chatPadV + caret.bottom - scroll
                    )
                }
            },
            dropTargetProvider = target@{ _ ->
                val layout = chatTextLayout ?: return@target null
                if (!chatValue.selection.collapsed) return@target null
                val offset = chatValue.selection.start
                    .coerceIn(0, layout.layoutInput.text.length)
                val caret = layout.getCursorRect(offset)
                val scroll = chatScrollState.value.toFloat()
                Offset(
                    chatPadH + caret.left + chatCaretHalf,
                    chatPadV + caret.bottom - scroll
                )
            }
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxChatInputHeight)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
                    .verticalScroll(chatScrollState)
                    .onSizeChanged { chatInputHeight = it.height }
            ) {
            BasicTextField(
                value = chatValue,
                onValueChange = { updated ->
                    chatValue = updated
                    if (updated.text != input) onInputChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    // 输入框内边距区域本身不归 BasicTextField 处理，点左边空白时
                    // 手动聚焦并把光标放到对应位置，避免点不到/光标不动
                    .pointerInput(Unit) {
                        detectTapGestures { pos ->
                            val leftPad = chatPadH
                            val right = size.width - chatPadH
                            val top = chatPadV
                            val bottom = size.height - chatPadV
                            val inTextArea = pos.x in leftPad..right &&
                                pos.y in top..bottom
                            if (!inTextArea) {
                                chatFocus.requestFocus()
                                val length = chatValue.text.length
                                val offset = when {
                                    pos.x < leftPad -> 0
                                    pos.x > right -> length
                                    else -> {
                                        val layout = chatTextLayout
                                        if (layout != null) {
                                            layout.getOffsetForPosition(
                                                Offset(
                                                    (pos.x - leftPad).coerceAtLeast(0f),
                                                    (pos.y - top).coerceAtLeast(0f)
                                                )
                                            ).coerceIn(0, length)
                                        } else {
                                            0
                                        }
                                    }
                                }
                                chatValue = chatValue.copy(selection = TextRange(offset))
                            }
                        }
                    }
                    .focusRequester(chatFocus)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .onFocusChanged { chatFocused = it.isFocused },
                textStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, color = colors.onSurface),
                cursorBrush = SolidColor(
                    if (chatFade) Color.Transparent else colors.primary
                ),
                onTextLayout = { chatTextLayout = it },
                maxLines = Int.MAX_VALUE,
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

/** DeepSeek 网页端样式：图标 + 已思考（用时）+ 箭头，默认收起。 */
@Composable
private fun ReasoningBlock(
    text: String,
    durationMs: Long,
    thinking: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "reasonArrow"
    )
    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.45f))
            .border(0.7.dp, colors.outlineVariant, RoundedCornerShape(12.dp))
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Psychology,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            val label = when {
                thinking -> stringResource(R.string.thinking_streaming)
                durationMs > 0L -> stringResource(R.string.reasoning_done) +
                    stringResource(
                        R.string.reasoning_duration,
                        (durationMs / 1000L).toInt().coerceAtLeast(1)
                    )
                else -> stringResource(R.string.reasoning_done)
            }
            Text(
                text = label,
                fontSize = 11.5.sp,
                color = colors.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(
                    if (expanded) R.string.context_summary_collapse
                    else R.string.context_summary_expand
                ),
                tint = colors.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(arrowRotation)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = text,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
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
            if (!isUser && message.reasoning.isNotBlank()) {
                ReasoningBlock(
                    text = message.reasoning,
                    durationMs = message.reasoningMs
                )
                Spacer(Modifier.height(5.dp))
            }
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
