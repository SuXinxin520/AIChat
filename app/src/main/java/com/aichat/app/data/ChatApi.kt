package com.aichat.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** 走 OpenAI 兼容的 /chat/completions 接口，流式取回复。 */
object ChatApi {

    fun streamReply(
        config: ApiConfig,
        systemPrompt: String,
        history: List<ChatMessage>
    ): Flow<String> = flow {
        val base = config.baseUrl.trim().trimEnd('/')
        val body = JSONObject().apply {
            put("model", config.model.trim())
            put("stream", true)
            put("messages", buildMessages(systemPrompt, history))
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
                            val delta = parseDelta(data)
                            if (!delta.isNullOrEmpty()) {
                                emitted = true
                                emit(delta)
                            }
                        }

                        // 有些服务端不认 stream，直接回整段 JSON
                        trimmed.startsWith("{") -> plainBody.append(trimmed)

                        else -> Unit
                    }
                }
            }

            if (!emitted) {
                val content = parseFullContent(plainBody.toString())
                if (content.isEmpty()) throw IllegalStateException("empty reply")
                emit(content)
            }
        } finally {
            conn.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    /** 非流式：一次性拿完整回复。 */
    suspend fun reply(
        config: ApiConfig,
        systemPrompt: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val base = config.baseUrl.trim().trimEnd('/')
        val body = JSONObject().apply {
            put("model", config.model.trim())
            put("stream", false)
            put("messages", buildMessages(systemPrompt, history))
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

            val content = parseFullContent(response)
            if (content.isEmpty()) throw IllegalStateException("empty reply")
            content
        } finally {
            conn.disconnect()
        }
    }

    private fun buildMessages(systemPrompt: String, history: List<ChatMessage>): JSONArray {
        val array = JSONArray()
        if (systemPrompt.isNotBlank()) {
            array.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        history.forEach { message ->
            val content = message.text.trim()
            if (content.isNotEmpty()) {
                array.put(
                    JSONObject()
                        .put("role", if (message.fromUser) "user" else "assistant")
                        .put("content", content)
                )
            }
        }
        return array
    }

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
