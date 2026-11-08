package com.aichat.app.data

/**
 * 轻量 token 估算：中文/日文/韩文按 1 字约 1 token，其它字符按 4 字符约 1 token。
 * 不追求精确，只用于上下文预算和用量圆环；发送前会留出余量。
 */
object TokenEstimator {

    fun estimate(text: String): Int {
        if (text.isEmpty()) return 0
        var cjk = 0
        var other = 0
        for (ch in text) {
            if (isCjk(ch)) cjk++ else other++
        }
        return cjk + (other + 3) / 4
    }

    /** 每条消息额外算几个 token，作为 role 标签等开销。 */
    fun estimateMessage(text: String): Int = estimate(text) + 6

    private fun isCjk(ch: Char): Boolean {
        val code = ch.code
        return code in 0x3040..0x30FF ||   // 日文假名
            code in 0x3400..0x4DBF ||      // 扩展 A
            code in 0x4E00..0x9FFF ||      // 基本汉字
            code in 0xAC00..0xD7AF ||      // 韩文
            code in 0xF900..0xFAFF ||      // 兼容汉字
            code in 0xFF00..0xFFEF         // 全角标点/字母
    }
}
