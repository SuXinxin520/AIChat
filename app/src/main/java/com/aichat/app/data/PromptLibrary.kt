package com.aichat.app.data

import android.content.Context
import com.aichat.app.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** 提示词分组。key 非空表示预置分类，名称交给资源按当前语言显示；用户改过名后 name 即覆盖名。 */
data class PromptFolder(
    val id: String,
    val name: String = "",
    val key: String = "",
    val createdAt: Long = 0L
)

/** 一条提示词：名称用于选择，内容会拼进角色/用户提示。 */
data class PromptItem(
    val id: String,
    val folderId: String,
    val name: String = "",
    val content: String = "",
    val createdAt: Long = 0L
)

data class PromptLibrary(
    val folders: List<PromptFolder> = emptyList(),
    val prompts: List<PromptItem> = emptyList(),
    val seeded: Boolean = false
) {
    fun folder(id: String): PromptFolder? = folders.firstOrNull { it.id == id }

    fun prompt(id: String): PromptItem? = prompts.firstOrNull { it.id == id }

    fun promptsIn(folderId: String): List<PromptItem> = prompts.filter { it.folderId == folderId }

    /** 按库里的排列顺序拼出最终提示词，忽略已删除的条目。 */
    fun composePrompt(ids: List<String>): String {
        val wanted = ids.toSet()
        return prompts
            .filter { it.id in wanted }
            .mapNotNull { item -> item.content.trim().takeIf { it.isNotEmpty() } }
            .joinToString("\n\n")
    }
}

/**
 * 提示词库：和项目里其它 Store 一样存在 aichat_prefs 的一个 JSON 字符串里，
 * 会被 Storage 的 aichat-data.json 快照自动镜像，不需要手动 markDirty。
 */
object PromptLibraryStore {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_LIBRARY = "prompt_library"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): PromptLibrary {
        val raw = prefs(context).getString(KEY_LIBRARY, null) ?: return PromptLibrary()
        return runCatching {
            val root = JSONObject(raw)
            val folders = root.optJSONArray("folders").toObjectList { item ->
                PromptFolder(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    key = item.optString("key"),
                    createdAt = item.optLong("createdAt", 0L)
                )
            }.filter { it.id.isNotBlank() }
            val prompts = root.optJSONArray("prompts").toObjectList { item ->
                PromptItem(
                    id = item.optString("id"),
                    folderId = item.optString("folderId"),
                    name = item.optString("name"),
                    content = item.optString("content"),
                    createdAt = item.optLong("createdAt", 0L)
                )
            }.filter { it.id.isNotBlank() }
            PromptLibrary(
                folders = folders,
                prompts = prompts,
                seeded = root.optBoolean("seeded", false)
            )
        }.getOrDefault(PromptLibrary())
    }

    private fun save(context: Context, library: PromptLibrary) {
        val root = JSONObject().apply {
            put("version", 1)
            put("seeded", library.seeded)
            put("folders", JSONArray().apply {
                library.folders.forEach { folder ->
                    put(JSONObject().apply {
                        put("id", folder.id)
                        put("name", folder.name)
                        put("key", folder.key)
                        put("createdAt", folder.createdAt)
                    })
                }
            })
            put("prompts", JSONArray().apply {
                library.prompts.forEach { prompt ->
                    put(JSONObject().apply {
                        put("id", prompt.id)
                        put("folderId", prompt.folderId)
                        put("name", prompt.name)
                        put("content", prompt.content)
                        put("createdAt", prompt.createdAt)
                    })
                }
            })
        }
        prefs(context).edit().putString(KEY_LIBRARY, root.toString()).apply()
    }

    private inline fun <T> JSONArray?.toObjectList(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optJSONObject(index)?.let(transform) }
    }

    /** 首次使用时播种默认分类/提示词，并把旧的自由文本提示词迁移成「自定义」条目。 */
    fun ensureReady(context: Context): PromptLibrary {
        var library = load(context)
        var changed = false

        if (!library.seeded) {
            val chinese = isChinese(context)
            val folders = library.folders.toMutableList()
            val folderIds = folders.mapTo(mutableSetOf()) { it.id }
            PromptSeeds.folders().forEach { if (it.id !in folderIds) folders.add(it) }

            val prompts = library.prompts.toMutableList()
            val promptIds = prompts.mapTo(mutableSetOf()) { it.id }
            PromptSeeds.prompts(chinese).forEach { if (it.id !in promptIds) prompts.add(it) }

            library = library.copy(folders = folders, prompts = prompts, seeded = true)
            changed = true
        }

        val migrated = migrateLegacy(context, library)
        if (migrated != library) {
            library = migrated
            changed = true
        }

        // 「全局」分类固定在第一位，老数据也在这里纠正顺序
        val orderedFolders = library.folders.sortedBy { if (it.key == "custom") 0 else 1 }
        if (orderedFolders != library.folders) {
            library = library.copy(folders = orderedFolders)
            changed = true
        }

        if (changed) save(context, library)
        return library
    }

    private fun isChinese(context: Context): Boolean = when (AppPrefs.language(context)) {
        "zh" -> true
        AppPrefs.LANGUAGE_SYSTEM -> java.util.Locale.getDefault().language == "zh"
        else -> false
    }

    private fun migrateLegacy(context: Context, source: PromptLibrary): PromptLibrary {
        val folders = source.folders.toMutableList()
        val prompts = source.prompts.toMutableList()
        var changed = false
        val now = System.currentTimeMillis()

        fun ensureCustomFolder() {
            if (folders.none { it.key == "custom" }) {
                folders.add(
                    PromptFolder(
                        id = PromptSeeds.FOLDER_CUSTOM,
                        key = "custom",
                        createdAt = now
                    )
                )
                changed = true
            }
        }

        fun upsert(item: PromptItem) {
            val index = prompts.indexOfFirst { it.id == item.id }
            if (index >= 0) prompts[index] = item else prompts.add(item)
            changed = true
        }

        val user = ProfileStore.loadUser(context)
        if (user.prompt.isNotBlank() && user.promptIds.isEmpty()) {
            ensureCustomFolder()
            val id = "pp_legacy_user"
            upsert(
                PromptItem(
                    id = id,
                    folderId = PromptSeeds.FOLDER_CUSTOM,
                    name = context.getString(R.string.prompt_legacy_user_name),
                    content = user.prompt.trim(),
                    createdAt = now
                )
            )
            ProfileStore.saveUser(context, user.copy(promptIds = listOf(id)))
        }

        ProfileStore.loadRoles(context).forEach { role ->
            if (role.id.isNotBlank() && role.prompt.isNotBlank() && role.promptIds.isEmpty()) {
                ensureCustomFolder()
                val id = "pp_legacy_role_${role.id}"
                val name = if (role.name.isNotBlank()) {
                    context.getString(R.string.prompt_legacy_role_name, role.name)
                } else {
                    context.getString(R.string.prompt_legacy_role_fallback)
                }
                upsert(
                    PromptItem(
                        id = id,
                        folderId = PromptSeeds.FOLDER_CUSTOM,
                        name = name,
                        content = role.prompt.trim(),
                        createdAt = now
                    )
                )
                ProfileStore.upsertRole(context, role.copy(promptIds = listOf(id)))
            }
        }

        return if (changed) {
            source.copy(folders = folders, prompts = prompts)
        } else {
            source
        }
    }

    fun addFolder(context: Context, name: String): Pair<PromptLibrary, PromptFolder> {
        val library = load(context)
        val folder = PromptFolder(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            createdAt = System.currentTimeMillis()
        )
        val updated = library.copy(folders = library.folders + folder)
        save(context, updated)
        return updated to folder
    }

    fun renameFolder(context: Context, id: String, name: String): PromptLibrary {
        val library = load(context)
        val updated = library.copy(
            folders = library.folders.map { folder ->
                if (folder.id == id) folder.copy(name = name.trim()) else folder
            }
        )
        save(context, updated)
        return updated
    }

    fun deleteFolder(context: Context, id: String): PromptLibrary {
        val library = load(context)
        val updated = library.copy(
            folders = library.folders.filterNot { it.id == id },
            prompts = library.prompts.filterNot { it.folderId == id }
        )
        save(context, updated)
        return updated
    }

    fun addPrompt(
        context: Context,
        folderId: String,
        name: String,
        content: String
    ): Pair<PromptLibrary, PromptItem> {
        var library = load(context)
        var targetFolder = library.folder(folderId)
        if (targetFolder == null) {
            targetFolder = library.folders.firstOrNull { it.key == "custom" }
                ?: PromptFolder(
                    id = PromptSeeds.FOLDER_CUSTOM,
                    key = "custom",
                    createdAt = System.currentTimeMillis()
                )
            if (library.folder(targetFolder.id) == null) {
                library = library.copy(folders = library.folders + targetFolder)
            }
        }
        val item = PromptItem(
            id = UUID.randomUUID().toString(),
            folderId = targetFolder.id,
            name = name.trim(),
            content = content.trim(),
            createdAt = System.currentTimeMillis()
        )
        library = library.copy(prompts = library.prompts + item)
        save(context, library)
        return library to item
    }

    fun updatePrompt(context: Context, item: PromptItem): PromptLibrary {
        val library = load(context)
        val updated = library.copy(
            prompts = library.prompts.map { existing ->
                if (existing.id == item.id) item else existing
            }
        )
        save(context, updated)
        return updated
    }

    fun deletePrompt(context: Context, id: String): PromptLibrary {
        val library = load(context)
        val updated = library.copy(prompts = library.prompts.filterNot { it.id == id })
        save(context, updated)
        return updated
    }

    /** 聊天时用：有选择就用库里的最新内容，否则回退到保存时的快照。 */
    fun resolvePrompt(context: Context, ids: List<String>, fallback: String): String =
        if (ids.isEmpty()) fallback.trim() else load(context).composePrompt(ids)
}
