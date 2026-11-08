package com.aichat.app.data

import kotlin.math.ceil

/**
 * 不打包 6MB 词表的折中计数：
 * - DeepSeek 官方文档给出的比例：中文 1 字≈0.6 token，其它字符 1 个≈0.3 token；
 * - 其它模型沿用轻量字符估算。
 * 真正准确的用量以接口返回的 usage 为准（见 UsageStore）。
 */
object ModelTokenizer {

    fun count(text: String, model: String): Int {
        if (text.isEmpty()) return 0
        if (!isDeepSeek(model)) return TokenEstimator.estimate(text)

        var cjk = 0
        var other = 0
        for (ch in text) {
            if (isCjk(ch)) cjk++ else other++
        }
        return ceil(cjk * 0.6 + other * 0.3).toInt().coerceAtLeast(1)
    }

    fun countMessage(text: String, model: String): Int =
        count(text, model) + MESSAGE_OVERHEAD

    /** DeepSeek 视觉单张图片的上限是 1024 token；预算按上限保守计算。 */
    fun imageTokens(model: String, images: Int): Int {
        if (images <= 0) return 0
        return if (isDeepSeek(model)) images * DEEPSEEK_IMAGE_TOKENS else images * 1024
    }

    fun isDeepSeek(model: String): Boolean =
        model.contains("deepseek", ignoreCase = true)

    private fun isCjk(ch: Char): Boolean {
        val code = ch.code
        return code in 0x3040..0x30FF ||
            code in 0x3400..0x4DBF ||
            code in 0x4E00..0x9FFF ||
            code in 0xAC00..0xD7AF ||
            code in 0xF900..0xFAFF ||
            code in 0xFF00..0xFFEF
    }

    private const val MESSAGE_OVERHEAD = 6
    private const val DEEPSEEK_IMAGE_TOKENS = 1024
}
