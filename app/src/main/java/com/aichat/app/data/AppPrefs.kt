package com.aichat.app.data

import android.content.Context

/** 应用级设置：语言与主题。默认都跟随系统。 */
object AppPrefs {

    private const val PREFS_NAME = "aichat_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_THEME = "app_theme"
    private const val KEY_STREAM = "stream_enabled"
    private const val KEY_TYPEWRITER = "typewriter_enabled"
    private const val KEY_CONFIRM_UNSAVED = "confirm_unsaved"
    private const val KEY_CURSOR_DROP = "cursor_drop_animation"
    private const val KEY_CURSOR_FADE = "cursor_fade_animation"
    private const val KEY_CHAT_KEEP_AWAKE = "chat_keep_awake"

    /** system=跟随系统；always=聊天时始终常亮；数字=无操作多少分钟后允许系统息屏。 */
    const val KEEP_AWAKE_SYSTEM = "system"
    const val KEEP_AWAKE_ALWAYS = "always"

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

    /** 打字机逐字动画，默认开启。 */
    fun typewriterEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_TYPEWRITER, true)

    fun setTypewriterEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_TYPEWRITER, enabled).apply()
    }

    /** 离开有修改的编辑页时是否弹窗提醒，默认提醒。 */
    fun confirmUnsaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CONFIRM_UNSAVED, true)

    fun setConfirmUnsaved(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CONFIRM_UNSAVED, enabled).apply()
    }

    /** 光标水滴下落动画，默认开启；关闭后只做原位淡出。 */
    fun cursorDropEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CURSOR_DROP, true)

    fun setCursorDropEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CURSOR_DROP, enabled).apply()
    }

    /** 光标竖线渐亮渐暗动画，默认开启；关闭后恢复系统闪烁。 */
    fun cursorFadeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CURSOR_FADE, true)

    fun setCursorFadeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_CURSOR_FADE, enabled).apply()
    }

    /** 聊天页屏幕常亮设置，默认跟随系统。 */
    fun chatKeepAwake(context: Context): String =
        prefs(context).getString(KEY_CHAT_KEEP_AWAKE, KEEP_AWAKE_SYSTEM)
            ?: KEEP_AWAKE_SYSTEM

    fun setChatKeepAwake(context: Context, value: String) {
        prefs(context).edit().putString(KEY_CHAT_KEEP_AWAKE, value).apply()
    }
}
