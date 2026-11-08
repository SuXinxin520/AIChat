package com.aichat.app.data

import android.content.Context
import org.json.JSONObject

/** 接口返回的真实 token 用量，用于校准圆环显示。 */
data class ApiUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val updatedAt: Long = 0L
) {
    val total: Int get() = promptTokens + completionTokens
}

object UsageStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_USAGE = "chat_usage"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readRoot(context: Context): JSONObject = runCatching {
        JSONObject(prefs(context).getString(KEY_USAGE, "{}") ?: "{}")
    }.getOrElse { JSONObject() }

    fun load(context: Context, roleId: String): ApiUsage? {
        val item = readRoot(context).optJSONObject(roleId) ?: return null
        val prompt = item.optInt("promptTokens", 0)
        val completion = item.optInt("completionTokens", 0)
        if (prompt <= 0 && completion <= 0) return null
        return ApiUsage(prompt, completion, item.optLong("updatedAt", 0L))
    }

    fun save(context: Context, roleId: String, usage: ApiUsage) {
        val root = readRoot(context)
        root.put(
            roleId,
            JSONObject().apply {
                put("promptTokens", usage.promptTokens)
                put("completionTokens", usage.completionTokens)
                put("updatedAt", usage.updatedAt)
            }
        )
        prefs(context).edit().putString(KEY_USAGE, root.toString()).apply()
    }

    fun clear(context: Context, roleId: String) {
        val root = readRoot(context)
        if (!root.has(roleId)) return
        root.remove(roleId)
        prefs(context).edit().putString(KEY_USAGE, root.toString()).apply()
    }
}
