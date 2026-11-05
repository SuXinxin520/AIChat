package com.aichat.app.data

import android.content.Context
import org.json.JSONObject

/** 已输入但没发出去的草稿。 */
data class Draft(val text: String, val updatedAt: Long = 0L)

object DraftStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_DRAFTS = "chat_drafts"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readRoot(context: Context): JSONObject = runCatching {
        JSONObject(prefs(context).getString(KEY_DRAFTS, "{}") ?: "{}")
    }.getOrElse { JSONObject() }

    fun load(context: Context, roleId: String): Draft? {
        val item = readRoot(context).optJSONObject(roleId) ?: return null
        val text = item.optString("text")
        if (text.isBlank()) return null
        return Draft(text = text, updatedAt = item.optLong("at", 0L))
    }

    fun loadAll(context: Context): Map<String, Draft> {
        val root = readRoot(context)
        val result = mutableMapOf<String, Draft>()
        root.keys().forEach { key ->
            val item = root.optJSONObject(key) ?: return@forEach
            val text = item.optString("text")
            if (text.isNotBlank()) {
                result[key] = Draft(text = text, updatedAt = item.optLong("at", 0L))
            }
        }
        return result
    }

    fun save(context: Context, roleId: String, text: String) {
        val prefs = prefs(context)
        val root = readRoot(context)

        if (text.isBlank()) {
            if (!root.has(roleId)) return
            root.remove(roleId)
        } else {
            root.put(
                roleId,
                JSONObject()
                    .put("text", text)
                    .put("at", System.currentTimeMillis())
            )
        }

        prefs.edit().putString(KEY_DRAFTS, root.toString()).apply()
    }

    fun clear(context: Context, roleId: String) = save(context, roleId, "")
}
