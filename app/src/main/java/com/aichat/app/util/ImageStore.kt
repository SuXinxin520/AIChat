package com.aichat.app.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.util.UUID

object ImageStore {

    /** 把选中的文件复制到应用私有目录，返回本地绝对路径。 */
    fun copyToAppStorage(context: Context, uri: Uri, folder: String = "avatars"): String? = runCatching {
        val dir = File(context.filesDir, folder).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return@runCatching null
        file.absolutePath
    }.getOrNull()

    /** 取文件的显示名。 */
    fun displayName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull() ?: "file"

    /** 按目标尺寸降采样解码，头像不需要原图。 */
    fun loadBitmap(path: String?, targetPx: Int = 240): ImageBitmap? {
        if (path.isNullOrBlank()) return null
        if (!File(path).exists()) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)

            var sample = 1
            val width = bounds.outWidth
            if (width > 0) {
                while (width / (sample * 2) >= targetPx) {
                    sample *= 2
                }
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeFile(path, options)?.asImageBitmap()
        }.getOrNull()
    }
}
