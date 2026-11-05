package com.aichat.app.data

import android.content.Context

/** 应用级设置：语言与主题。默认都跟随系统。 */
object AppPrefs {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_THEME = "app_theme"
    private const val KEY_STREAM = "stream_enabled"

    const val LANGUAGE_SYSTEM = "system"
    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun language(context: Context): String =
        prefs(context).getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM

    fun setLanguage(context: Context, value: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, value).apply()
    }

    fun theme(context: Context): String =
        prefs(context).getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setTheme(context: Context, value: String) {
        prefs(context).edit().putString(KEY_THEME, value).apply()
    }

    /** 流式输出，默认开启。 */
    fun streamEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_STREAM, true)

    fun setStreamEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_STREAM, enabled).apply()
    }
}
