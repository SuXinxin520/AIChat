package com.aichat.app.util

import android.content.Context
import android.content.res.Configuration
import com.aichat.app.data.AppPrefs
import java.util.Locale

object Locales {

    /** 按用户设置包一层 Context；跟随系统时原样返回。 */
    fun wrap(context: Context): Context {
        val language = AppPrefs.language(context)
        if (language == AppPrefs.LANGUAGE_SYSTEM) return context

        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
