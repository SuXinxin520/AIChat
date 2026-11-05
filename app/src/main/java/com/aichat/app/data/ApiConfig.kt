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
    val enabled: Boolean = true
)

/** 常用服务商的预设：选中后填入地址，并给出可选模型。 */
data class ApiProvider(
    val name: String,
    val baseUrl: String,
    val models: List<String>
) {
    val defaultModel: String get() = models.first()
}

object ApiProviders {
    val list = listOf(
        ApiProvider(
            name = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            models = listOf("gpt-6.1-sol", "gpt-6-astra", "gpt-6-luna")
        ),
        ApiProvider(
            name = "Claude",
            baseUrl = "https://api.anthropic.com/v1",
            models = listOf("claude-sonnet-5-5", "claude-opus-5-5", "claude-haiku-5-5")
        ),
        ApiProvider(
            name = "DeepSeek",
            baseUrl = "https://api.deepseek.com",
            models = listOf("deepseek-flash", "deepseek-v4-pro")
        ),
        ApiProvider(
            name = "Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
            models = listOf("gemini-3.8-flash", "gemini-3-flash")
        ),
        ApiProvider(
            name = "Qwen",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            models = listOf("qwen-plus", "qwen-max")
        ),
        ApiProvider(
            name = "Kimi",
            baseUrl = "https://api.moonshot.cn/v1",
            models = listOf("kimi-k3", "kimi-k2.7-code", "kimi-k2.6")
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
                    enabled = item.optBoolean("enabled", true)
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
