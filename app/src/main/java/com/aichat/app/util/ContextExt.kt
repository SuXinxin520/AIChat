package com.aichat.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** 从 Compose 的 LocalContext 里找到宿主 Activity。 */
fun Context.findActivity(): Activity? {
    var context: Context? = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
