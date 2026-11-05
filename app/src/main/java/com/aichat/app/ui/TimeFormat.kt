package com.aichat.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aichat.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 会话列表的时间：一分钟内"刚刚"，再往上按 1/3/5/10 分钟、半小时分档；
 * 超过一小时显示当天的具体时间（如 22.12），不是当天则显示日期。
 */
@Composable
fun relativeTime(millis: Long): String {
    if (millis <= 0L) return ""

    val now = System.currentTimeMillis()
    val diff = now - millis

    return when {
        diff < 60_000L -> stringResource(R.string.time_just_now)
        diff < 3 * 60_000L -> stringResource(R.string.time_minutes_ago, 1)
        diff < 5 * 60_000L -> stringResource(R.string.time_minutes_ago, 3)
        diff < 10 * 60_000L -> stringResource(R.string.time_minutes_ago, 5)
        diff < 30 * 60_000L -> stringResource(R.string.time_minutes_ago, 10)
        diff < 60 * 60_000L -> stringResource(R.string.time_half_hour_ago)
        isSameDay(millis, now) -> clockTime(millis)
        else -> formatDate(millis)
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val left = Calendar.getInstance().apply { timeInMillis = a }
    val right = Calendar.getInstance().apply { timeInMillis = b }
    return left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
        left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)
}

private fun clockTime(millis: Long): String =
    SimpleDateFormat("HH.mm", Locale.getDefault()).format(Date(millis))

private fun formatDate(millis: Long): String {
    val locale = Locale.getDefault()
    val yearFormat = SimpleDateFormat("yyyy", locale)
    val sameYear = yearFormat.format(Date(millis)) == yearFormat.format(Date())
    return SimpleDateFormat(if (sameYear) "MM-dd" else "yyyy-MM-dd", locale).format(Date(millis))
}
