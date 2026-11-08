package com.aichat.app.data

import android.content.Context
import org.json.JSONObject

/** 按角色保存的思考/联网开关，默认都关闭。 */
data class RoleChatSettings(
    val thinkingEnabled: Boolean = false,
    /** 统一三档：low / medium / high；DeepSeek 侧再映射到 low/high/max。 */
    val thinkingEffort: String = "medium",
    val webSearchEnabled: Boolean = false
)

object RoleChatSettingsStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_SETTINGS = "role_chat_settings"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readRoot(context: Context): JSONObject = runCatching {
        JSONObject(prefs(context).getString(KEY_SETTINGS, "{}") ?: "{}")
    }.getOrElse { JSONObject() }

    fun load(context: Context, roleId: String): RoleChatSettings {
        val item = readRoot(context).optJSONObject(roleId) ?: return RoleChatSettings()
        val rawEffort = item.optString("thinkingEffort", "medium")
        val effort = when (rawEffort) {
            "low" -> "low"
            "medium" -> "medium"
            // 旧版 high/max 迁移到新的中/高
            "high" -> "medium"
            "max" -> "high"
            else -> "medium"
        }
        return RoleChatSettings(
            thinkingEnabled = item.optBoolean("thinkingEnabled", false),
            thinkingEffort = effort,
            webSearchEnabled = item.optBoolean("webSearchEnabled", false)
        )
    }

    fun save(context: Context, roleId: String, settings: RoleChatSettings) {
        val root = readRoot(context)
        root.put(
            roleId,
            JSONObject().apply {
                put("thinkingEnabled", settings.thinkingEnabled)
                put("thinkingEffort", settings.thinkingEffort)
                put("webSearchEnabled", settings.webSearchEnabled)
            }
        )
        prefs(context).edit().putString(KEY_SETTINGS, root.toString()).apply()
    }

    fun clear(context: Context, roleId: String) {
        val root = readRoot(context)
        if (!root.has(roleId)) return
        root.remove(roleId)
        prefs(context).edit().putString(KEY_SETTINGS, root.toString()).apply()
    }
}
