package com.aichat.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 用户资料。名称与提示词会在后续版本的对话中作为系统提示交给 AI。 */
data class UserProfile(
    val name: String = "",
    val prompt: String = "",
    val avatarPath: String? = null,
    /** 提示词库里选中的条目；prompt 保留为拼好的快照/旧数据。 */
    val promptIds: List<String> = emptyList()
)

/** 角色。lastMessage / lastMessageAt 留给后续的对话功能写入。 */
data class Role(
    val id: String = "",
    val name: String = "",
    val prompt: String = "",
    val avatarPath: String? = null,
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val createdAt: Long = 0L,
    /** 提示词库里选中的条目；prompt 保留为拼好的快照/旧数据。 */
    val promptIds: List<String> = emptyList()
)

private fun JSONObject.optStringList(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return (0 until array.length()).mapNotNull { index ->
        array.optString(index).takeIf { it.isNotBlank() }
    }
}

object ProfileStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_USER = "user_profile"
    private const val KEY_ROLES = "roles"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadUser(context: Context): UserProfile {
        val raw = prefs(context).getString(KEY_USER, null) ?: return UserProfile()
        return runCatching {
            val item = JSONObject(raw)
            UserProfile(
                name = item.optString("name"),
                prompt = item.optString("prompt"),
                avatarPath = item.optString("avatarPath").takeIf { it.isNotBlank() },
                promptIds = item.optStringList("promptIds")
            )
        }.getOrDefault(UserProfile())
    }

    fun saveUser(context: Context, profile: UserProfile) {
        val item = JSONObject().apply {
            put("name", profile.name)
            put("prompt", profile.prompt)
            put("avatarPath", profile.avatarPath ?: "")
            put("promptIds", JSONArray().apply { profile.promptIds.forEach { put(it) } })
        }
        prefs(context).edit().putString(KEY_USER, item.toString()).apply()
    }

    fun loadRoles(context: Context): List<Role> {
        val raw = prefs(context).getString(KEY_ROLES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                Role(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    prompt = item.optString("prompt"),
                    avatarPath = item.optString("avatarPath").takeIf { it.isNotBlank() },
                    lastMessage = item.optString("lastMessage"),
                    lastMessageAt = item.optLong("lastMessageAt", 0L),
                    createdAt = item.optLong("createdAt", 0L),
                    promptIds = item.optStringList("promptIds")
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun saveRoles(context: Context, roles: List<Role>) {
        val array = JSONArray()
        roles.forEach { role ->
            array.put(
                JSONObject().apply {
                    put("id", role.id)
                    put("name", role.name)
                    put("prompt", role.prompt)
                    put("avatarPath", role.avatarPath ?: "")
                    put("lastMessage", role.lastMessage)
                    put("lastMessageAt", role.lastMessageAt)
                    put("createdAt", role.createdAt)
                    put("promptIds", JSONArray().apply { role.promptIds.forEach { put(it) } })
                }
            )
        }
        prefs(context).edit().putString(KEY_ROLES, array.toString()).apply()
    }

    fun upsertRole(context: Context, role: Role): Role {
        val roles = loadRoles(context).toMutableList()
        val saved = if (role.id.isBlank()) role.copy(id = UUID.randomUUID().toString()) else role
        val index = roles.indexOfFirst { it.id == saved.id }
        if (index >= 0) roles[index] = saved else roles.add(saved)
        saveRoles(context, roles)
        return saved
    }

    fun deleteRole(context: Context, id: String) {
        saveRoles(context, loadRoles(context).filterNot { it.id == id })
    }
}
