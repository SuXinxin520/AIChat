package com.aichat.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class StorageUsage(
    val dataBytes: Long,
    val avatarBytes: Long,
    val attachmentBytes: Long
) {
    val total: Long get() = dataBytes + avatarBytes + attachmentBytes
}

/**
 * 用户自选的文件夹就是应用的存储本体：
 * 设置与聊天记录写进 aichat-data.json，头像与附件直接以文档形式存在同名子目录里，
 * 应用内部只保留一份很小的配置缓存。
 */
object Storage {

    const val PREFS_NAME = "aichat_prefs"
    const val DIR_AVATARS = "avatars"
    const val DIR_ATTACHMENTS = "attachments"
    const val DIR_IMAGES = "images"

    private const val KEY_TREE = "storage_tree_uri"
    private const val KEY_NAME = "storage_tree_name"
    private const val KEY_INIT = "storage_initialized"

    private const val SNAPSHOT = "aichat-data.json"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pending: Job? = null

    fun sharedPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun prefs(context: Context) = sharedPrefs(context)

    fun hasFolder(context: Context): Boolean {
        val uri = folderUri(context) ?: return false
        return context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isWritePermission
        }
    }

    fun folderUri(context: Context): Uri? =
        prefs(context).getString(KEY_TREE, null)?.let { runCatching { Uri.parse(it) }.getOrNull() }

    fun folderName(context: Context): String =
        prefs(context).getString(KEY_NAME, "").orEmpty()

    /** 选中文件夹：首次会尝试恢复里面的数据，然后把当前数据写进去。 */
    fun setFolder(context: Context, uri: Uri): Boolean = runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        val root = DocumentFile.fromTreeUri(context, uri) ?: return@runCatching false
        val p = prefs(context)

        if (!p.getBoolean(KEY_INIT, false)) {
            restoreFrom(context, root)
        }

        p.edit()
            .putString(KEY_TREE, uri.toString())
            .putString(KEY_NAME, root.name.orEmpty())
            .putBoolean(KEY_INIT, true)
            .apply()

        writeSnapshot(context, root)
        true
    }.getOrDefault(false)

    /** 把外部选中的图片/文件直接存进文件夹，返回文档 URI 字符串。 */
    fun saveMedia(context: Context, source: Uri, folder: String): String? = runCatching {
        val tree = folderUri(context) ?: return@runCatching null
        val root = DocumentFile.fromTreeUri(context, tree) ?: return@runCatching null
        val dir = root.findFile(folder) ?: root.createDirectory(folder) ?: return@runCatching null

        val doc = dir.createFile("application/octet-stream", "${UUID.randomUUID()}.bin")
            ?: return@runCatching null

        val copied = context.contentResolver.openInputStream(source)?.use { input ->
            context.contentResolver.openOutputStream(doc.uri)?.use { output -> input.copyTo(output) }
            true
        } ?: false

        if (copied) doc.uri.toString() else null
    }.getOrNull()

    fun flush(context: Context) {
        val uri = folderUri(context) ?: return
        runCatching {
            val root = DocumentFile.fromTreeUri(context, uri) ?: return
            writeSnapshot(context, root)
        }
    }

    /** 立即在后台写一次（离开应用时用）。 */
    fun flushAsync(context: Context) {
        val app = context.applicationContext
        scope.launch { flush(app) }
    }

    /** 数据变动后延迟合并写入，避免输入过程中频繁写盘。 */
    fun markDirty(context: Context) {
        val app = context.applicationContext
        pending?.cancel()
        pending = scope.launch {
            delay(1500)
            flush(app)
        }
    }

    fun usage(context: Context): StorageUsage = runCatching {
        val uri = folderUri(context) ?: return@runCatching StorageUsage(0, 0, 0)
        val root = DocumentFile.fromTreeUri(context, uri) ?: return@runCatching StorageUsage(0, 0, 0)
        StorageUsage(
            dataBytes = root.findFile(SNAPSHOT)?.length() ?: 0L,
            avatarBytes = dirSize(root.findFile(DIR_AVATARS)),
            attachmentBytes = dirSize(root.findFile(DIR_ATTACHMENTS)) +
                dirSize(root.findFile(DIR_IMAGES))
        )
    }.getOrDefault(StorageUsage(0, 0, 0))

    private fun dirSize(dir: DocumentFile?): Long =
        if (dir == null) 0L else dir.listFiles().sumOf { it.length() }

    private fun buildSnapshot(context: Context): JSONObject {
        val prefsJson = JSONObject()
        prefs(context).all.forEach { (key, value) ->
            if (key.startsWith("storage_")) return@forEach
            when (value) {
                is String -> prefsJson.put(key, value)
                is Boolean -> prefsJson.put(key, value)
                is Int -> prefsJson.put(key, value)
                is Long -> prefsJson.put(key, value)
                is Float -> prefsJson.put(key, value)
                else -> Unit
            }
        }
        return JSONObject().apply {
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("prefs", prefsJson)
        }
    }

    private fun writeSnapshot(context: Context, root: DocumentFile) {
        val json = buildSnapshot(context)
        val doc = root.findFile(SNAPSHOT) ?: root.createFile("application/json", SNAPSHOT) ?: return
        context.contentResolver.openOutputStream(doc.uri, "wt")?.use { out ->
            out.write(json.toString().toByteArray(Charsets.UTF_8))
        }
    }

    private fun restoreFrom(context: Context, root: DocumentFile) {
        val doc = root.findFile(SNAPSHOT) ?: return
        val text = context.contentResolver.openInputStream(doc.uri)?.bufferedReader()?.use { it.readText() }
            ?: return
        val json = runCatching { JSONObject(text) }.getOrNull() ?: return

        val editor = prefs(context).edit()
        val saved = json.optJSONObject("prefs") ?: return
        saved.keys()?.forEach { key ->
            when (val value = saved.get(key)) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> editor.putFloat(key, value.toFloat())
                else -> Unit
            }
        }
        editor.putBoolean(KEY_INIT, true).apply()
    }
}
