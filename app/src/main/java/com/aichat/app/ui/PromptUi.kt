package com.aichat.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aichat.app.R
import com.aichat.app.data.PromptFolder

/** 预置分类的名称跟随当前界面语言；用户改过名后使用覆盖名。 */
@Composable
fun promptFolderName(folder: PromptFolder): String = folder.name.ifBlank {
    when (folder.key) {
        "personality" -> stringResource(R.string.prompt_folder_personality)
        "age" -> stringResource(R.string.prompt_folder_age)
        "body" -> stringResource(R.string.prompt_folder_body)
        "hobbies" -> stringResource(R.string.prompt_folder_hobbies)
        "identity" -> stringResource(R.string.prompt_folder_identity)
        "custom" -> stringResource(R.string.prompt_folder_custom)
        else -> stringResource(R.string.prompt_folder_untitled)
    }
}
