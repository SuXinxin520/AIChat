package com.aichat.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** 某个角色的旧消息滚动摘要；coveredIds 里的消息不再进请求。 */
data class ChatSummary(
    val roleId: String = "",
    val text: String = "",
    val coveredIds: List<String> = emptyList(),
    val updatedAt: Long = 0L
)

object SummaryStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_SUMMARIES = "chat_summaries"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readRoot(context: Context): JSONObject = runCatching {
        JSONObject(prefs(context).getString(KEY_SUMMARIES, "{}") ?: "{}")
    }.getOrElse { JSONObject() }

    fun load(context: Context, roleId: String): ChatSummary? {
        val item = readRoot(context).optJSONObject(roleId) ?: return null
        val text = item.optString("text").trim()
        if (text.isEmpty()) return null
        val ids = mutableListOf<String>()
        item.optJSONArray("coveredIds")?.let { array ->
            for (index in 0 until array.length()) {
                array.optString(index).takeIf { it.isNotBlank() }?.let(ids::add)
            }
        }
        return ChatSummary(
            roleId = roleId,
            text = text,
            coveredIds = ids,
            updatedAt = item.optLong("updatedAt", 0L)
        )
    }

    fun save(context: Context, summary: ChatSummary) {
        val root = readRoot(context)
        if (summary.text.isBlank()) {
            root.remove(summary.roleId)
        } else {
            root.put(
                summary.roleId,
                JSONObject().apply {
                    put("text", summary.text)
                    put("coveredIds", JSONArray(summary.coveredIds))
                    put("updatedAt", summary.updatedAt)
                }
            )
        }
        prefs(context).edit().putString(KEY_SUMMARIES, root.toString()).apply()
    }

    fun clear(context: Context, roleId: String) {
        val root = readRoot(context)
        if (!root.has(roleId)) return
        root.remove(roleId)
        prefs(context).edit().putString(KEY_SUMMARIES, root.toString()).apply()
    }
}
