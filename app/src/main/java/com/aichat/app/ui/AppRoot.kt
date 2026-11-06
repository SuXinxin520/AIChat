package com.aichat.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/** 0.0.1 的极简路由，用一个栈维护返回顺序。 */
sealed interface Screen {
    data object Home : Screen
    data object UserProfile : Screen
    data object Settings : Screen
    data object General : Screen
    data object ApiList : Screen
    data class ApiEdit(val id: String?) : Screen
    data object Language : Screen
    data object Theme : Screen
    data object Storage : Screen
    data object About : Screen
    data class RoleEdit(val id: String?) : Screen
    data class Chat(val roleId: String) : Screen
    data class Placeholder(val title: String, val body: String) : Screen
}

@Composable
fun AppRoot(themeMode: String, onThemeChange: (String) -> Unit) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val pop: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    BackHandler(enabled = backStack.size > 1, onBack = pop)

    when (val current = backStack.last()) {
        Screen.Home -> HomeScreen(
            onOpenSettings = { backStack.add(Screen.Settings) },
            onOpenProfile = { backStack.add(Screen.UserProfile) },
            onAddRole = { backStack.add(Screen.RoleEdit(null)) },
            onOpenRole = { id -> backStack.add(Screen.Chat(id)) }
        )

        is Screen.Chat -> ChatScreen(
            roleId = current.roleId,
            onBack = pop,
            onOpenSettings = { backStack.add(Screen.RoleEdit(current.roleId)) }
        )

        Screen.UserProfile -> UserProfileScreen(onBack = pop)

        Screen.Settings -> SettingsScreen(
            onBack = pop,
            onOpen = { backStack.add(it) }
        )

        Screen.General -> GeneralScreen(onBack = pop)

        Screen.ApiList -> ApiListScreen(
            onBack = pop,
            onAdd = { backStack.add(Screen.ApiEdit(null)) },
            onEdit = { id -> backStack.add(Screen.ApiEdit(id)) }
        )

        is Screen.ApiEdit -> ApiEditScreen(
            configId = current.id,
            onBack = pop
        )

        Screen.Language -> LanguageScreen(onBack = pop)

        Screen.Theme -> ThemeScreen(
            current = themeMode,
            onSelect = onThemeChange,
            onBack = pop
        )

        Screen.Storage -> StorageScreen(onBack = pop)

        Screen.About -> AboutScreen(onBack = pop)

        is Screen.RoleEdit -> RoleEditScreen(
            roleId = current.id,
            onBack = pop
        )

        is Screen.Placeholder -> PlaceholderScreen(
            title = current.title,
            body = current.body,
            onBack = pop
        )
    }
}
