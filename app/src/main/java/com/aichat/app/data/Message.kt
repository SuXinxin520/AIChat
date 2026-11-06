package com.aichat.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 一条聊天消息：文本、图片、文件三者取其一。 */
data class ChatMessage(
    val id: String = "",
    val roleId: String = "",
    val fromUser: Boolean = true,
    val text: String = "",
    val imagePath: String = "",
    val fileName: String = "",
    val filePath: String = "",
    val at: Long = 0L
)

object MessageStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_MESSAGES = "messages"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadAll(context: Context): List<ChatMessage> {
        val raw = prefs(context).getString(KEY_MESSAGES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                ChatMessage(
                    id = item.optString("id"),
                    roleId = item.optString("roleId"),
                    fromUser = item.optBoolean("fromUser", true),
                    text = item.optString("text"),
                    imagePath = item.optString("imagePath"),
                    fileName = item.optString("fileName"),
                    filePath = item.optString("filePath"),
                    at = item.optLong("at", 0L)
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveAll(context: Context, messages: List<ChatMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject().apply {
                    put("id", message.id)
                    put("roleId", message.roleId)
                    put("fromUser", message.fromUser)
                    put("text", message.text)
                    put("imagePath", message.imagePath)
                    put("fileName", message.fileName)
                    put("filePath", message.filePath)
                    put("at", message.at)
                }
            )
        }
        prefs(context).edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    fun load(context: Context, roleId: String): List<ChatMessage> =
        loadAll(context)
            .filter { it.roleId == roleId }
            .map { message ->
                // 旧版本流式解析会把 JSON null 写成字面量 "null"，这里清掉开头残留
                if (message.fromUser) message else message.copy(text = cleanLegacyNull(message.text))
            }
            .sortedBy { it.at }

    private fun cleanLegacyNull(text: String): String {
        var result = text
        while (result.startsWith("null")) {
            result = result.removePrefix("null")
        }
        return result
    }

    fun add(context: Context, message: ChatMessage): ChatMessage {
        val all = loadAll(context).toMutableList()
        val saved = if (message.id.isBlank()) message.copy(id = UUID.randomUUID().toString()) else message
        all.add(saved)
        saveAll(context, all)
        return saved
    }

    fun updateText(context: Context, id: String, text: String) {
        val all = loadAll(context).toMutableList()
        val index = all.indexOfFirst { it.id == id }
        if (index < 0) return
        all[index] = all[index].copy(text = text)
        saveAll(context, all)
    }

    fun delete(context: Context, id: String) {
        saveAll(context, loadAll(context).filterNot { it.id == id })
    }

    fun deleteMany(context: Context, ids: Collection<String>) {
        if (ids.isEmpty()) return
        val targets = ids.toSet()
        saveAll(context, loadAll(context).filterNot { it.id in targets })
    }

    /** 删除该消息之后的所有消息（含其它角色的不动）。 */
    fun deleteAfter(context: Context, roleId: String, id: String) {
        val all = loadAll(context)
        val roleMessages = all.filter { it.roleId == roleId }.sortedBy { it.at }
        val index = roleMessages.indexOfFirst { it.id == id }
        if (index < 0) return
        val keep = roleMessages.take(index + 1).map { it.id }.toSet()
        saveAll(context, all.filterNot { it.roleId == roleId && it.id !in keep })
    }

    fun clearRole(context: Context, roleId: String) {
        saveAll(context, loadAll(context).filterNot { it.roleId == roleId })
    }
}
