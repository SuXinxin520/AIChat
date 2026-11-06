package com.aichat.app.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.InputStream

object ImageStore {

    /** 取文件的显示名。 */
    fun displayName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }.getOrNull() ?: "file"

    /**
     * 支持两种引用：文件夹里的文档 URI（content://）和旧版本遗留的本地文件路径。
     * 按目标尺寸降采样解码。
     */
    fun loadBitmap(context: Context, ref: String?, targetPx: Int = 240): ImageBitmap? {
        if (ref.isNullOrBlank()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open(context, ref)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            var sample = 1
            val width = bounds.outWidth
            if (width > 0) {
                while (width / (sample * 2) >= targetPx) {
                    sample *= 2
                }
            }

            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            open(context, ref)?.use { BitmapFactory.decodeStream(it, null, options)?.asImageBitmap() }
        } catch (e: Exception) {
            null
        }
    }

    private fun open(context: Context, ref: String): InputStream? = try {
        if (ref.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(ref))
        } else {
            File(ref).takeIf { it.exists() }?.inputStream()
        }
    } catch (e: Exception) {
        null
    }
}
