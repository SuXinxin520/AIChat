package com.aichat.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

sealed interface ChatStreamEvent {
    data class Content(val text: String) : ChatStreamEvent
    data class Reasoning(val text: String) : ChatStreamEvent
}

data class ChatReply(
    val content: String,
    val reasoning: String = ""
)

/** 头像上下文：标签 + 本地图片路径，作为一条隐藏的 user 消息放在最前面。 */
data class ChatAvatar(val label: String, val path: String)

data class ChatAvatarContext(
    val intro: String,
    val images: List<ChatAvatar>
)

/** 走 OpenAI 兼容的 /chat/completions 接口，流式取回复。 */
object ChatApi {

    fun streamReply(
        context: Context,
        config: ApiConfig,
        systemPrompt: String,
        history: List<ChatMessage>,
        avatars: ChatAvatarContext? = null,
        onUsage: ((promptTokens: Int, completionTokens: Int) -> Unit)? = null
    ): Flow<ChatStreamEvent> = flow {
        val base = config.baseUrl.trim().trimEnd('/')
        val body = JSONObject().apply {
            put("model", config.model.trim())
            put("stream", true)
            put("messages", buildMessages(context, systemPrompt, history, config.model, avatars))
            applyThinking(this, config, enabled = null)
            if (isDeepSeek(config)) {
                // DeepSeek 支持流式最终块返回 usage
                put("stream_options", JSONObject().put("include_usage", true))
            }
        }

        val conn = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("Authorization", "Bearer ${config.apiKey.trim()}")
            if (base.contains("anthropic.com")) {
                setRequestProperty("x-api-key", config.apiKey.trim())
                setRequestProperty("anthropic-version", "2023-06-01")
            }
        }

        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code !in 200..299) {
                val error = conn.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException(errorMessage(error) ?: "HTTP $code")
            }

            var emitted = false
            val plainBody = StringBuilder()

            conn.inputStream.bufferedReader().use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    val trimmed = line.trim()
                    when {
                        trimmed.isEmpty() -> Unit

                        trimmed.startsWith("data:") -> {
                            val data = trimmed.removePrefix("data:").trim()
                            if (data == "[DONE]") break
                            parseUsage(data)?.let { (prompt, completion) ->
                                onUsage?.invoke(prompt, completion)
                            }
                            parseReasoningDelta(data)?.takeIf { it.isNotEmpty() }?.let { reasoning ->
                                emit(ChatStreamEvent.Reasoning(reasoning))
                            }
                            val delta = parseDelta(data)
                            if (!delta.isNullOrEmpty()) {
                                emitted = true
                                emit(ChatStreamEvent.Content(delta))
                            }
                        }

                        // 有些服务端不认 stream，直接回整段 JSON
                        trimmed.startsWith("{") -> plainBody.append(trimmed)

                        else -> Unit
                    }
                }
            }

            if (!emitted) {
                val body = plainBody.toString()
                val content = parseFullContent(body)
                if (content.isEmpty()) throw IllegalStateException("empty reply")
                val reasoning = parseFullReasoning(body)
                if (reasoning.isNotEmpty()) emit(ChatStreamEvent.Reasoning(reasoning))
                emit(ChatStreamEvent.Content(content))
            }
        } finally {
            conn.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    /** 非流式：一次性拿完整回复（含思考过程）。 */
    suspend fun replyDetailed(
        context: Context,
        config: ApiConfig,
        systemPrompt: String,
        history: List<ChatMessage>,
        avatars: ChatAvatarContext? = null,
        useThinking: Boolean = true,
        onUsage: ((promptTokens: Int, completionTokens: Int) -> Unit)? = null
    ): ChatReply = withContext(Dispatchers.IO) {
        val base = config.baseUrl.trim().trimEnd('/')
        val body = JSONObject().apply {
            put("model", config.model.trim())
            put("stream", false)
            put("messages", buildMessages(context, systemPrompt, history, config.model, avatars))
            applyThinking(this, config, enabled = if (useThinking) null else false)
        }

        val conn = (URL("$base/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.apiKey.trim()}")
            if (base.contains("anthropic.com")) {
                setRequestProperty("x-api-key", config.apiKey.trim())
                setRequestProperty("anthropic-version", "2023-06-01")
            }
        }

        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (code !in 200..299) {
                throw IllegalStateException(errorMessage(response) ?: "HTTP $code")
            }

            parseUsage(response)?.let { (prompt, completion) ->
                onUsage?.invoke(prompt, completion)
            }
            val content = parseFullContent(response)
            if (content.isEmpty()) throw IllegalStateException("empty reply")
            ChatReply(content = content, reasoning = parseFullReasoning(response))
        } finally {
            conn.disconnect()
        }
    }

    /** DeepSeek V4 的思考开关/强度；其它服务商不发送这些字段。 */
    private fun applyThinking(body: JSONObject, config: ApiConfig, enabled: Boolean?) {
        if (!isDeepSeek(config)) return
        val thinking = enabled ?: config.thinkingEnabled
        body.put(
            "thinking",
            JSONObject().put("type", if (thinking) "enabled" else "disabled")
        )
        if (thinking) {
            // 统一 UI 的低/中/高映射到 DeepSeek 实际的三档 low/high/max
            val effort = when (config.thinkingEffort) {
                "low" -> "low"
                "medium" -> "high"
                "high" -> "max"
                else -> "high"
            }
            body.put("reasoning_effort", effort)
        }
    }

    /** 只要正文，摘要等场景用。 */
    suspend fun reply(
        context: Context,
        config: ApiConfig,
        systemPrompt: String,
        history: List<ChatMessage>,
        useThinking: Boolean = true,
        onUsage: ((promptTokens: Int, completionTokens: Int) -> Unit)? = null
    ): String = replyDetailed(
        context = context,
        config = config,
        systemPrompt = systemPrompt,
        history = history,
        useThinking = useThinking,
        onUsage = onUsage
    ).content

    /** DeepSeek Search + Fetch：返回可直接拼进 system 的带来源上下文。 */
    suspend fun webSearch(config: ApiConfig, query: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (!config.baseUrl.contains("api.deepseek.com", ignoreCase = true)) {
                return@runCatching null
            }
            val base = "https://api.deepseek.com"
            val request = JSONObject().apply {
                put("query", query.take(128))
                put("count", 5)
                put("need_content", true)
            }
            val response = postJson("$base/v1/tools/search", config.apiKey, request)
            val results = JSONObject(response).optJSONArray("results") ?: return@runCatching null
            if (results.length() == 0) return@runCatching null

            val builder = StringBuilder()
            builder.append("以下是实时网页搜索结果。请结合这些结果回答，并在使用处标注来源 URL；")
            builder.append("如果结果与问题无关可以忽略。\n\n")

            val limit = minOf(results.length(), 4)
            for (index in 0 until limit) {
                val item = results.optJSONObject(index) ?: continue
                val title = item.optString("title").trim()
                val url = item.optString("url").trim()
                val summary = item.optString("summary").trim()
                var content = item.optString("content").trim()
                if (content.isEmpty() && index == 0 && url.startsWith("http")) {
                    content = fetchPage(config, url).orEmpty()
                }
                if (title.isEmpty() && url.isEmpty() && summary.isEmpty() && content.isEmpty()) {
                    continue
                }
                builder.append("${index + 1}. ")
                if (title.isNotEmpty()) builder.append(title)
                if (url.isNotEmpty()) builder.append(" — ").append(url)
                builder.append('\n')
                if (summary.isNotEmpty()) builder.append(summary).append('\n')
                if (content.isNotEmpty()) builder.append(content.take(2500)).append('\n')
                builder.append('\n')
                if (builder.length > 14_000) break
            }
            builder.toString().take(14_000).ifBlank { null }
        }.getOrNull()
    }

    private suspend fun fetchPage(config: ApiConfig, url: String): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = postJson(
                    "https://api.deepseek.com/v1/tools/fetch",
                    config.apiKey,
                    JSONObject().put("url", url)
                )
                val page = JSONObject(response)
                val title = page.optString("title").trim()
                val content = page.optString("content").trim()
                if (content.isEmpty()) return@runCatching null
                buildString {
                    if (title.isNotEmpty()) append(title).append('\n')
                    append(content.take(4000))
                }
            }.getOrNull()
        }

    private fun postJson(url: String, apiKey: String, body: JSONObject): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 40_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) {
                throw IllegalStateException(errorMessage(response) ?: "HTTP $code")
            }
            return response
        } finally {
            conn.disconnect()
        }
    }

    private fun isDeepSeek(config: ApiConfig): Boolean =
        config.baseUrl.contains("deepseek", ignoreCase = true) ||
            config.model.startsWith("deepseek", ignoreCase = true)

    private fun parseUsage(data: String): Pair<Int, Int>? = runCatching {
        val usage = JSONObject(data).optJSONObject("usage") ?: return@runCatching null
        val prompt = usage.optInt("prompt_tokens", 0)
        val completion = usage.optInt("completion_tokens", 0)
        if (prompt <= 0 && completion <= 0) null else prompt to completion
    }.getOrNull()

    private fun buildMessages(
        context: Context,
        systemPrompt: String,
        history: List<ChatMessage>,
        model: String,
        avatars: ChatAvatarContext? = null
    ): JSONArray {
        val array = JSONArray()
        if (systemPrompt.isNotBlank()) {
            array.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }

        // 头像作为一条不显示在聊天记录里的 user 消息，放在历史最前面。
        // 视觉模型才有意义；只构造一条，避免每轮重复文字说明。
        if (avatars != null && supportsVision(model)) {
            val parts = JSONArray()
            if (avatars.intro.isNotBlank()) parts.put(textPart(avatars.intro))
            var imageCount = 0
            avatars.images.forEach { avatar ->
                buildImageDataUrl(context, avatar.path)?.let { dataUrl ->
                    parts.put(textPart(avatar.label))
                    parts.put(imagePart(dataUrl))
                    imageCount++
                }
            }
            if (imageCount > 0) {
                array.put(JSONObject().put("role", "user").put("content", parts))
            }
        }

        history.forEach { message ->
            val role = if (message.fromUser) "user" else "assistant"
            val text = message.text.trim()
            val hasAttachment = message.fromUser &&
                (message.imagePath.isNotBlank() || message.filePath.isNotBlank())

            if (hasAttachment) {
                val parts = JSONArray()
                if (text.isNotEmpty()) parts.put(textPart(text))

                if (message.imagePath.isNotBlank()) {
                    if (supportsVision(model)) {
                        if (text.isEmpty() && parts.length() == 0) {
                            parts.put(textPart("请看这张图片"))
                        }
                        buildImageDataUrl(context, message.imagePath)?.let {
                            parts.put(imagePart(it))
                        } ?: parts.put(textPart("[图片无法读取]"))
                    } else {
                        parts.put(textPart("[图片：当前模型不支持视觉]"))
                    }
                }

                if (message.filePath.isNotBlank()) {
                    appendFileParts(context, message, model, parts)
                }

                if (parts.length() > 0) {
                    array.put(JSONObject().put("role", role).put("content", parts))
                }
            } else if (text.isNotEmpty()) {
                array.put(JSONObject().put("role", role).put("content", text))
            }
        }
        return array
    }

    private fun textPart(text: String): JSONObject =
        JSONObject().put("type", "text").put("text", text)

    private fun imagePart(dataUrl: String): JSONObject =
        JSONObject().put(
            "type",
            "image_url"
        ).put("image_url", JSONObject().put("url", dataUrl))

    fun supportsVision(model: String): Boolean {
        val name = model.lowercase()
        return !name.contains("deepseek") ||
            name.contains("flash") ||
            name.contains("vision")
    }

    private fun buildImageDataUrl(context: Context, path: String): String? = runCatching {
        val bytes = context.contentResolver
            .openInputStream(Uri.parse(path))
            ?.use { it.readBytes() }
            ?: return@runCatching null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

        var sample = 1
        while (bounds.outWidth / sample > 1280 || bounds.outHeight / sample > 1280) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: return@runCatching null
        val scaled = scaleDown(decoded)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        if (scaled !== decoded) decoded.recycle()
        scaled.recycle()
        "data:image/jpeg;base64," +
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= 1280) return bitmap
        val scale = 1280f / maxSide
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private fun appendFileParts(
        context: Context,
        message: ChatMessage,
        model: String,
        parts: JSONArray
    ) {
        val name = message.fileName.ifBlank { "file" }
        val ext = name.substringAfterLast('.', "").lowercase()
        when {
            ext in IMAGE_EXTENSIONS -> {
                if (supportsVision(model)) {
                    parts.put(textPart("【图片文件：$name】"))
                    buildImageDataUrl(context, message.filePath)?.let {
                        parts.put(imagePart(it))
                    } ?: parts.put(textPart("【文件：$name】读取失败"))
                } else {
                    parts.put(textPart("【文件：$name】当前模型不支持图片"))
                }
            }

            ext == "pdf" -> appendPdfParts(context, message, model, parts)

            ext == "docx" -> {
                val text = readDocxText(context, message.filePath)
                if (text.isNullOrBlank()) {
                    parts.put(textPart("【文件：$name】无法解析内容"))
                } else {
                    parts.put(textPart(fileHeader(name, text)))
                }
            }

            else -> {
                val text = readTextFile(context, message.filePath)
                if (text.isNullOrBlank()) {
                    parts.put(textPart("【文件：$name】暂不支持解析该格式"))
                } else {
                    parts.put(textPart(fileHeader(name, text)))
                }
            }
        }
    }

    private fun fileHeader(name: String, text: String): String {
        val clipped = text.take(MAX_FILE_CHARS)
        val suffix = if (clipped.length < text.length) "\n…（内容已截断）" else ""
        return "【文件：$name】\n$clipped$suffix"
    }

    private fun readTextFile(context: Context, path: String): String? = runCatching {
        val bytes = context.contentResolver
            .openInputStream(Uri.parse(path))
            ?.use { it.readBytes() }
            ?: return@runCatching null
        if (bytes.isEmpty()) return@runCatching ""
        // 简单二进制判断：前 1024 字节里有 NUL 就不当文本
        val probe = bytes.take(1024)
        if (probe.any { it == 0.toByte() }) return@runCatching null
        bytes.toString(Charsets.UTF_8)
    }.getOrNull()

    private fun readDocxText(context: Context, path: String): String? = runCatching {
        val input = context.contentResolver.openInputStream(Uri.parse(path))
            ?: return@runCatching null
        input.use { stream ->
            ZipInputStream(stream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") {
                        val xml = zip.readBytes().toString(Charsets.UTF_8)
                        return@runCatching xml
                            .replace(Regex("<[^>]+>"), " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&quot;", "\"")
                            .replace("&apos;", "'")
                            .replace(Regex("\\s+"), " ")
                            .trim()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        null
    }.getOrNull()

    private fun appendPdfParts(
        context: Context,
        message: ChatMessage,
        model: String,
        parts: JSONArray
    ) {
        val name = message.fileName.ifBlank { "document.pdf" }
        if (!supportsVision(model)) {
            parts.put(textPart("【PDF：$name】当前模型不支持视觉，无法读取 PDF"))
            return
        }
        runCatching {
            val pfd = context.contentResolver
                .openFileDescriptor(Uri.parse(message.filePath), "r")
                ?: return@runCatching
            try {
                PdfRenderer(pfd).use { renderer ->
                    val pages = minOf(renderer.pageCount, MAX_PDF_PAGES)
                    parts.put(
                        textPart(
                            "【PDF：$name（共 ${renderer.pageCount} 页，发送前 $pages 页）】"
                        )
                    )
                    for (index in 0 until pages) {
                        renderer.openPage(index).use { page ->
                            val width = 1280
                            val height = (page.height.toFloat() * width / page.width)
                                .toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(
                                width,
                                height,
                                Bitmap.Config.ARGB_8888
                            )
                            bitmap.eraseColor(AndroidColor.WHITE)
                            page.render(
                                bitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                            val out = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
                            bitmap.recycle()
                            val dataUrl = "data:image/jpeg;base64," +
                                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                            parts.put(imagePart(dataUrl))
                        }
                    }
                }
            } finally {
                pfd.close()
            }
        }.onFailure {
            parts.put(textPart("【PDF：$name】读取失败"))
        }
    }

    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
    private const val MAX_FILE_CHARS = 12_000
    private const val MAX_PDF_PAGES = 5

    /**
     * 注意：分片里的 content 常常是 JSON null，Android 的 optString 会把它变成字面量 "null"，
     * 所以这里必须显式判断 isNull。
     */
    private fun parseDelta(data: String): String? = runCatching {
        val delta = JSONObject(data)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("delta")
            ?: return@runCatching null

        if (delta.isNull("content")) return@runCatching null

        delta.optString("content").takeIf { it.isNotEmpty() && it != "null" }
    }.getOrNull()

    private fun parseReasoningDelta(data: String): String? = runCatching {
        val delta = JSONObject(data)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("delta")
            ?: return@runCatching null

        if (delta.isNull("reasoning_content")) return@runCatching null

        delta.optString("reasoning_content")
            .takeIf { it.isNotEmpty() && it != "null" }
    }.getOrNull()

    private fun parseFullReasoning(body: String): String {
        val message = runCatching {
            JSONObject(body)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
        }.getOrNull() ?: return ""
        if (message.isNull("reasoning_content")) return ""
        return message.optString("reasoning_content").trim().let {
            if (it == "null") "" else it
        }
    }

    private fun parseFullContent(body: String): String {
        val message = runCatching {
            JSONObject(body)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
        }.getOrNull() ?: return ""

        if (message.isNull("content")) return ""

        val content = message.optString("content").trim()
        return if (content == "null") "" else content
    }

    private fun errorMessage(response: String): String? = runCatching {
        val root = JSONObject(response)
        root.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
            ?: root.optString("message").takeIf { it.isNotBlank() }
    }.getOrNull()
}
