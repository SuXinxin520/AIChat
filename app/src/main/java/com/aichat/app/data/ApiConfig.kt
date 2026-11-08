package com.aichat.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 一条已保存的 API 配置。 */
data class ApiConfig(
    val id: String = "",
    val name: String = "自定义",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val enabled: Boolean = true,
    /** 模型上下文窗口，单位 token；用于上下文预算和用量提示。 */
    val contextLimit: Int = DEFAULT_CONTEXT_LIMIT,
    /** 达到阈值时是否自动把旧消息压缩成摘要。 */
    val autoCompress: Boolean = true,
    /** 上下文使用率达到该百分比时触发自动压缩（10–100）。 */
    val compressThreshold: Int = 100,
    /** 旧版全局思考开关；聊天实际使用按角色的 RoleChatSettings。 */
    val thinkingEnabled: Boolean = false,
    /** DeepSeek 思考强度：low / high / max。 */
    val thinkingEffort: String = "high",
    /** 是否在发送前调用 DeepSeek 的网页搜索/抓取。 */
    val webSearchEnabled: Boolean = false
) {
    companion object {
        const val DEFAULT_CONTEXT_LIMIT = 32_768
        const val MIN_CONTEXT_LIMIT = 4_096
        const val MAX_CONTEXT_LIMIT = 2_000_000
        const val MIN_COMPRESS_THRESHOLD = 10
        const val MAX_COMPRESS_THRESHOLD = 100
    }
}

/** 常用服务商的预设：选中后填入地址，并给出可选模型。 */
data class ApiProvider(
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    /** 开放平台里创建/查看 API Key 的页面。 */
    val keyUrl: String = ""
) {
    val defaultModel: String get() = models.first()
}

object ApiProviders {
    val list = listOf(
        ApiProvider(
            name = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            models = listOf("gpt-6.1-sol", "gpt-6-astra", "gpt-6-luna"),
            keyUrl = "https://platform.openai.com/api-keys"
        ),
        ApiProvider(
            name = "Claude",
            baseUrl = "https://api.anthropic.com/v1",
            models = listOf("claude-sonnet-5-5", "claude-opus-5-5", "claude-haiku-5-5"),
            keyUrl = "https://console.anthropic.com/settings/keys"
        ),
        ApiProvider(
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com",
            models = listOf("deepseek-flash", "deepseek-v4-pro"),
            keyUrl = "https://platform.deepseek.com/api_keys"
        ),
        ApiProvider(
            name = "火山引擎",
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3",
            models = listOf(
                "doubao-seed-2-1-pro-260915",
                "doubao-seed-2-1-lite-260915",
                "doubao-seed-2-1-turbo-260628",
                "doubao-seed-evolving"
            ),
            keyUrl = "https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey"
        ),
        ApiProvider(
            name = "Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
            models = listOf("gemini-3.8-flash", "gemini-3-flash"),
            keyUrl = "https://aistudio.google.com/app/apikey"
        ),
        ApiProvider(
            name = "Qwen",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            models = listOf("qwen-plus", "qwen-max"),
            keyUrl = "https://bailian.console.aliyun.com/?apiKey=1"
        ),
        ApiProvider(
            name = "Kimi",
            baseUrl = "https://api.moonshot.cn/v1",
            models = listOf("kimi-k3", "kimi-k2.7-code", "kimi-k2.6"),
            keyUrl = "https://platform.moonshot.cn/console/api-keys"
        )
    )

    fun find(baseUrl: String): ApiProvider? {
        val target = normalizeUrl(baseUrl)
        if (target.isEmpty()) return null
        return list.firstOrNull { normalizeUrl(it.baseUrl) == target }
    }

    fun normalizeUrl(url: String): String = url.trim().trimEnd('/').lowercase()
}

object ApiConfigStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_CONFIGS = "api_configs"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAll(context: Context): List<ApiConfig> {
        val raw = prefs(context).getString(KEY_CONFIGS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                ApiConfig(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    baseUrl = item.optString("baseUrl"),
                    apiKey = item.optString("apiKey"),
                    model = item.optString("model"),
                    enabled = item.optBoolean("enabled", true),
                    contextLimit = item.optInt(
                        "contextLimit",
                        ApiConfig.DEFAULT_CONTEXT_LIMIT
                    ).coerceIn(ApiConfig.MIN_CONTEXT_LIMIT, ApiConfig.MAX_CONTEXT_LIMIT),
                    autoCompress = item.optBoolean("autoCompress", true),
                    compressThreshold = item.optInt("compressThreshold", 100).coerceIn(
                        ApiConfig.MIN_COMPRESS_THRESHOLD,
                        ApiConfig.MAX_COMPRESS_THRESHOLD
                    ),
                    thinkingEnabled = item.optBoolean("thinkingEnabled", false),
                    thinkingEffort = item.optString("thinkingEffort", "high")
                        .takeIf { it in setOf("low", "high", "max") } ?: "high",
                    webSearchEnabled = item.optBoolean("webSearchEnabled", false)
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveAll(context: Context, list: List<ApiConfig>) {
        val array = JSONArray()
        list.forEach { config ->
            array.put(
                JSONObject().apply {
                    put("id", config.id)
                    put("name", config.name)
                    put("baseUrl", config.baseUrl)
                    put("apiKey", config.apiKey)
                    put("model", config.model)
                    put("enabled", config.enabled)
                    put("contextLimit", config.contextLimit)
                    put("autoCompress", config.autoCompress)
                    put("compressThreshold", config.compressThreshold)
                    put("thinkingEnabled", config.thinkingEnabled)
                    put("thinkingEffort", config.thinkingEffort)
                    put("webSearchEnabled", config.webSearchEnabled)
                }
            )
        }
        prefs(context).edit().putString(KEY_CONFIGS, array.toString()).apply()
    }

    /** 新增或更新，返回落库后的对象（新增时补上 id）。 */
    fun upsert(context: Context, config: ApiConfig): ApiConfig {
        val list = loadAll(context).toMutableList()
        val saved = if (config.id.isBlank()) config.copy(id = UUID.randomUUID().toString()) else config
        val index = list.indexOfFirst { it.id == saved.id }
        if (index >= 0) list[index] = saved else list.add(saved)
        saveAll(context, list)
        return saved
    }

    fun delete(context: Context, id: String) {
        saveAll(context, loadAll(context).filterNot { it.id == id })
    }
}
