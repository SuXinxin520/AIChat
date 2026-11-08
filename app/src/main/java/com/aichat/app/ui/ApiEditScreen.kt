package com.aichat.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.ApiConfig
import com.aichat.app.data.ApiConfigStore
import com.aichat.app.data.ApiProvider
import com.aichat.app.data.AppPrefs
import com.aichat.app.data.ApiProviders
import com.aichat.app.data.ConnectionTester
import com.aichat.app.data.TestResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ApiEditScreen(configId: String?, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val existing = remember(configId) {
        configId?.let { id -> ApiConfigStore.loadAll(context).firstOrNull { it.id == id } }
    }
    val fallback = ApiProviders.list.first()
    val customName = stringResource(R.string.api_custom)

    var name by rememberSaveable { mutableStateOf(existing?.name ?: fallback.name) }
    var baseUrl by rememberSaveable { mutableStateOf(existing?.baseUrl ?: fallback.baseUrl) }
    var apiKey by rememberSaveable { mutableStateOf(existing?.apiKey ?: "") }
    var model by rememberSaveable { mutableStateOf(existing?.model ?: fallback.defaultModel) }
    var contextLimitText by rememberSaveable {
        mutableStateOf(
            (existing?.contextLimit ?: ApiConfig.DEFAULT_CONTEXT_LIMIT).toString()
        )
    }
    var autoCompress by rememberSaveable { mutableStateOf(existing?.autoCompress ?: true) }
    var thresholdText by rememberSaveable {
        mutableStateOf((existing?.compressThreshold ?: 100).toString())
    }
    var keyVisible by rememberSaveable { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<TestResult?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUnsaved by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    // 和进来时的内容对比，判断有没有改动
    val initialName = existing?.name ?: fallback.name
    val initialUrl = existing?.baseUrl ?: fallback.baseUrl
    val initialKey = existing?.apiKey ?: ""
    val initialModel = existing?.model ?: fallback.defaultModel
    val initialLimit = (existing?.contextLimit ?: ApiConfig.DEFAULT_CONTEXT_LIMIT).toString()
    val initialAutoCompress = existing?.autoCompress ?: true
    val initialThreshold = (existing?.compressThreshold ?: 100).toString()
    val dirty = name.trim() != initialName ||
        baseUrl.trim() != initialUrl ||
        apiKey.trim() != initialKey ||
        model.trim() != initialModel ||
        contextLimitText.trim() != initialLimit ||
        autoCompress != initialAutoCompress ||
        thresholdText.trim() != initialThreshold

    val saveApi: () -> Boolean = {
        if (baseUrl.isBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.api_url_required))
            }
            false
        } else {
            ApiConfigStore.upsert(
                context = context,
                config = ApiConfig(
                    id = existing?.id ?: "",
                    name = name.trim().ifBlank {
                        ApiProviders.find(baseUrl)?.name ?: customName
                    },
                    baseUrl = baseUrl.trim(),
                    apiKey = apiKey.trim(),
                    model = model.trim(),
                    enabled = existing?.enabled ?: true,
                    contextLimit = contextLimitText.trim().toIntOrNull()
                        ?.coerceIn(
                            ApiConfig.MIN_CONTEXT_LIMIT,
                            ApiConfig.MAX_CONTEXT_LIMIT
                        )
                        ?: ApiConfig.DEFAULT_CONTEXT_LIMIT,
                    autoCompress = autoCompress,
                    compressThreshold = thresholdText.trim().toIntOrNull()
                        ?.coerceIn(
                            ApiConfig.MIN_COMPRESS_THRESHOLD,
                            ApiConfig.MAX_COMPRESS_THRESHOLD
                        )
                        ?: 100
                )
            )
            true
        }
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
                    if (existing == null) R.string.api_edit_add else R.string.api_edit_edit
                ),
                onBack = requestBack,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clickable(interactionSource = null, indication = null) {
                    // 点空白处收起键盘
                    focusManager.clearFocus()
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.api_hint),
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            ProviderChips(
                currentBaseUrl = baseUrl,
                onPick = { provider ->
                    // 名称还是自动生成的才跟着换，用户改过就不动
                    val autoName = ApiProviders.find(baseUrl)?.name ?: fallback.name
                    if (name.isBlank() || name == autoName) name = provider.name
                    baseUrl = provider.baseUrl
                    model = provider.defaultModel
                    result = null
                }
            )

            val currentProvider = ApiProviders.find(baseUrl)
            val keyPageUrl = currentProvider?.keyUrl.orEmpty()

            Spacer(Modifier.height(18.dp))

            ConfigField(
                label = stringResource(R.string.api_name),
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.api_name_hint)
            )

            Spacer(Modifier.height(14.dp))

            ConfigField(
                label = stringResource(R.string.api_url),
                value = baseUrl,
                onValueChange = { baseUrl = it },
                placeholder = "https://api.openai.com/v1",
                keyboardType = KeyboardType.Uri
            )

            Spacer(Modifier.height(14.dp))

            ConfigField(
                label = stringResource(R.string.api_key),
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = "sk-...",
                visualTransformation = if (keyVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = if (keyPageUrl.isBlank()) {
                    null
                } else {
                    { ApiKeyLinkIcon(keyPageUrl) }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text?.trim()
                                if (text.isNullOrBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.api_clipboard_empty)
                                        )
                                    }
                                } else {
                                    apiKey = text
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = stringResource(R.string.api_paste),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { keyVisible = !keyVisible },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                // 图标表示当前状态：明文时睁眼，隐藏时划掉的眼
                                imageVector = if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = stringResource(
                                    if (keyVisible) R.string.api_hide_key else R.string.api_show_key
                                ),
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            ModelField(
                model = model,
                onModelChange = {
                    model = it
                    result = null
                },
                options = currentProvider?.models ?: emptyList()
            )

            Spacer(Modifier.height(14.dp))

            ConfigField(
                label = stringResource(R.string.api_context_limit),
                value = contextLimitText,
                onValueChange = { value ->
                    contextLimitText = value.filter { it.isDigit() }.take(7)
                },
                placeholder = ApiConfig.DEFAULT_CONTEXT_LIMIT.toString(),
                keyboardType = KeyboardType.Number
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.api_context_limit_hint),
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.api_context_presets),
                fontSize = 12.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    32768 to "32K",
                    65536 to "64K",
                    131072 to "128K",
                    262144 to "256K",
                    1048576 to "1M"
                ).forEach { (value, label) ->
                    SelectChip(
                        label = label,
                        selected = contextLimitText.trim().toIntOrNull() == value,
                        onClick = { contextLimitText = value.toString() }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.api_auto_compress),
                        fontSize = 14.5.sp,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.api_auto_compress_desc),
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = autoCompress,
                    onCheckedChange = { autoCompress = it }
                )
            }

            if (autoCompress) {
                Spacer(Modifier.height(12.dp))
                ConfigField(
                    label = stringResource(R.string.api_auto_compress_threshold),
                    value = thresholdText,
                    onValueChange = { value ->
                        thresholdText = value.filter { it.isDigit() }.take(3)
                    },
                    placeholder = "100",
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.api_auto_compress_threshold_hint),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        testing = true
                        result = null
                        scope.launch {
                            result = ConnectionTester.test(baseUrl, apiKey)
                            testing = false
                        }
                    },
                    enabled = !testing,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    if (testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = colors.primary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = stringResource(
                            if (testing) R.string.api_testing else R.string.api_test
                        ),
                        fontSize = 14.5.sp
                    )
                }

                Button(
                    onClick = {
                        if (saveApi()) onBack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(text = stringResource(R.string.api_save), fontSize = 14.5.sp)
                }
            }

            val currentResult = result
            if (currentResult != null) {
                Spacer(Modifier.height(16.dp))
                val success = if (colors.background.luminance() > 0.5f) {
                    Color(0xFF2FA46B)
                } else {
                    Color(0xFF55D69E)
                }
                Text(
                    text = testMessage(currentResult),
                    fontSize = 13.sp,
                    color = if (currentResult is TestResult.Ok) success else colors.error
                )
            }

            if (existing != null) {
                Spacer(Modifier.height(20.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.api_delete),
                        fontSize = 14.sp,
                        color = colors.error
                    )
                }
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
                if (saveApi()) onBack()
            }
        )
    }

    if (showDeleteDialog && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.api_delete_title),
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.api_delete_message),
                    fontSize = 14.sp,
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ApiConfigStore.delete(context, existing.id)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text(text = stringResource(R.string.delete), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.cancel), color = colors.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun testMessage(result: TestResult): String = when (result) {
    is TestResult.Ok -> stringResource(R.string.api_test_ok, result.code)
    is TestResult.HttpError -> when (result.code) {
        401, 403 -> stringResource(R.string.api_test_bad_key, result.code)
        404 -> stringResource(R.string.api_test_bad_url, result.code)
        else -> stringResource(R.string.api_test_failed, result.code)
    }
    is TestResult.NetError -> stringResource(R.string.api_test_error, result.message)
    TestResult.NoUrl -> stringResource(R.string.api_url_required)
    TestResult.NoKey -> stringResource(R.string.api_key_required)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderChips(currentBaseUrl: String, onPick: (ApiProvider) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val active = ApiProviders.find(currentBaseUrl)
        ApiProviders.list.forEach { provider ->
            SelectChip(
                label = provider.name,
                selected = provider.name == active?.name,
                onClick = { onPick(provider) }
            )
        }
    }
}

@Composable
private fun ModelField(
    model: String,
    onModelChange: (String) -> Unit,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val baseColors = LocalTextSelectionColors.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val measureStyle = TextStyle(fontSize = 14.sp)
    val textPad = with(density) { 16.dp.toPx() }
    // M3 带 label 的 OutlinedTextField 顶部多留 8sp，文字整体下移，自绘光标同步修正
    val outlinedTopPad = with(density) { 8.sp.toPx() }
    val cursorWidth = with(density) { 2.dp.toPx() }
    val context = LocalContext.current
    val fadeEnabled = AppPrefs.cursorFadeEnabled(context)
    var tfv by remember { mutableStateOf(TextFieldValue(model)) }
    var focused by remember { mutableStateOf(false) }
    if (tfv.text != model) {
        tfv = tfv.copy(
            text = model,
            selection = TextRange(tfv.selection.start.coerceIn(0, model.length))
        )
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        FallingCursorDrop(
            modifier = Modifier.fillMaxWidth(),
            dropOffset = { tap, boxSize ->
                val layout = measurer.measure(
                    text = AnnotatedString(model),
                    style = measureStyle,
                    maxLines = 1,
                    softWrap = false
                )
                val localX = (tap.x - textPad)
                    .coerceIn(0f, layout.size.width.toFloat())
                val charOffset = layout.getOffsetForPosition(
                    Offset(localX, layout.size.height / 2f)
                )
                val caret = layout.getCursorRect(charOffset)
                val caretLeft = caret.left.coerceIn(
                    0f,
                    (layout.size.width - cursorWidth).coerceAtLeast(0f)
                )
                val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
                // 水滴尖对准竖线中心
                Offset(textPad + caretLeft + cursorWidth / 2f, textTop + caret.bottom)
            },
            caretProvider = caret@{ boxSize ->
                if (!fadeEnabled || !focused || !tfv.selection.collapsed) return@caret null
                val layout = measurer.measure(
                    text = AnnotatedString(tfv.text),
                    style = measureStyle,
                    maxLines = 1,
                    softWrap = false
                )
                val caret = layout.getCursorRect(tfv.selection.start)
                val caretLeft = caret.left.coerceIn(
                    0f,
                    (layout.size.width - cursorWidth).coerceAtLeast(0f)
                )
                val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
                CaretInfo(
                    x = textPad + caretLeft,
                    top = textTop + caret.top,
                    bottom = textTop + caret.bottom
                )
            }
        ) { handleColor ->
            OutlinedTextField(
                value = tfv,
                onValueChange = { updated ->
                    tfv = updated
                    if (updated.text != model) onModelChange(updated.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
                label = { Text(text = stringResource(R.string.api_model), fontSize = 13.sp) },
                placeholder = { Text(text = "gpt-6.1-sol", fontSize = 14.sp, color = colors.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(fontSize = 14.sp, color = colors.onSurface),
                trailingIcon = {
                    if (options.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                // 只开列表，不进入输入状态
                                focusManager.clearFocus()
                                expanded = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = stringResource(R.string.api_choose_model),
                                tint = colors.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.onSurfaceVariant,
                    cursorColor = if (fadeEnabled) Color.Transparent else colors.primary,
                    selectionColors = TextSelectionColors(
                        handleColor = handleColor,
                        backgroundColor = baseColors.backgroundColor
                    )
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, fontSize = 14.sp, color = colors.onSurface) },
                    onClick = {
                        onModelChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(50)
    Column(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) colors.primary else colors.background)
            .border(1.dp, if (selected) colors.primary else colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) colors.onPrimary else colors.onSurface
        )
    }
}

@Composable
private fun ConfigField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val baseColors = LocalTextSelectionColors.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val measureStyle = TextStyle(fontSize = 14.sp)
    val textPad = with(density) { 16.dp.toPx() }
    // M3：有前置图标时，文字从图标占位(48dp)+4dp 之后开始
    val textPadStart = with(density) { (if (leadingIcon != null) 52.dp else 16.dp).toPx() }
    // M3 带 label 的 OutlinedTextField 顶部多留 8sp，文字整体下移，自绘光标同步修正
    val outlinedTopPad = with(density) { 8.sp.toPx() }
    val cursorWidth = with(density) { 2.dp.toPx() }
    val context = LocalContext.current
    val fadeEnabled = AppPrefs.cursorFadeEnabled(context) &&
        visualTransformation == VisualTransformation.None
    var tfv by remember { mutableStateOf(TextFieldValue(value)) }
    var focused by remember { mutableStateOf(false) }
    if (tfv.text != value) {
        tfv = tfv.copy(
            text = value,
            selection = TextRange(tfv.selection.start.coerceIn(0, value.length))
        )
    }
    FallingCursorDrop(
        modifier = Modifier.fillMaxWidth(),
        dropOffset = { tap, boxSize ->
            if (visualTransformation != VisualTransformation.None) {
                tap
            } else {
                val layout = measurer.measure(
                    text = AnnotatedString(value),
                    style = measureStyle,
                    maxLines = 1,
                    softWrap = false
                )
                val localX = (tap.x - textPadStart)
                    .coerceIn(0f, layout.size.width.toFloat())
                val charOffset = layout.getOffsetForPosition(
                    Offset(localX, layout.size.height / 2f)
                )
                val caret = layout.getCursorRect(charOffset)
                val caretLeft = caret.left.coerceIn(
                    0f,
                    (layout.size.width - cursorWidth).coerceAtLeast(0f)
                )
                val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
                // 水滴尖对准竖线中心
                Offset(textPadStart + caretLeft + cursorWidth / 2f, textTop + caret.bottom)
            }
        },
        caretProvider = caret@{ boxSize ->
            if (!fadeEnabled || !focused || !tfv.selection.collapsed) return@caret null
            val layout = measurer.measure(
                text = AnnotatedString(tfv.text),
                style = measureStyle,
                maxLines = 1,
                softWrap = false
            )
            val caret = layout.getCursorRect(tfv.selection.start)
            val caretLeft = caret.left.coerceIn(
                0f,
                (layout.size.width - cursorWidth).coerceAtLeast(0f)
            )
            val textTop = (boxSize.height - layout.size.height) / 2f + outlinedTopPad / 2f
            CaretInfo(
                x = textPadStart + caretLeft,
                top = textTop + caret.top,
                bottom = textTop + caret.bottom
            )
        }
    ) { handleColor ->
        OutlinedTextField(
            value = tfv,
            onValueChange = { updated ->
                tfv = updated
                if (updated.text != value) onValueChange(updated.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            label = { Text(text = label, fontSize = 13.sp) },
            placeholder = { Text(text = placeholder, fontSize = 14.sp, color = colors.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = colors.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                focusedLabelColor = colors.primary,
                unfocusedLabelColor = colors.onSurfaceVariant,
                cursorColor = if (fadeEnabled) Color.Transparent else colors.primary,
                selectionColors = TextSelectionColors(
                    handleColor = handleColor,
                    backgroundColor = baseColors.backgroundColor
                )
            )
        )
    }
}

/** API Key 左侧的快捷入口：跳到服务商开放平台的密钥管理页。 */
@Composable
private fun ApiKeyLinkIcon(url: String) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            },
            modifier = Modifier
                .size(40.dp)
                // 图标往文字方向靠一点，缩小右侧空白
                .offset(x = 7.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = stringResource(R.string.api_get_key),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
