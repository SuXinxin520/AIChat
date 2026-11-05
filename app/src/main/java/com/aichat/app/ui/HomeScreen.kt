package com.aichat.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.R
import com.aichat.app.data.Draft
import com.aichat.app.data.DraftStore
import com.aichat.app.data.ProfileStore
import com.aichat.app.data.Role
import com.aichat.app.data.UserProfile

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddRole: () -> Unit,
    onOpenRole: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val profile = remember { ProfileStore.loadUser(context) }
    val roles = remember { ProfileStore.loadRoles(context) }
    val drafts = remember { DraftStore.loadAll(context) }
    var menuOpen by remember { mutableStateOf(false) }

    // 加号旋转 135° 变成叉，带一点回弹
    val fabRotation by animateFloatAsState(
        targetValue = if (menuOpen) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabRotation"
    )

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            ) {
                AnimatedVisibility(
                    visible = menuOpen,
                    enter = fadeIn() +
                        slideInVertically(initialOffsetY = { it / 3 }) +
                        scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    FloatingActionItem(
                        label = stringResource(R.string.role_sheet_title),
                        icon = Icons.Outlined.PersonAdd,
                        onClick = {
                            menuOpen = false
                            onAddRole()
                        }
                    )
                }

                FloatingActionButton(
                    onClick = { menuOpen = !menuOpen },
                    shape = CircleShape,
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                        focusedElevation = 6.dp,
                        hoveredElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.role_sheet_title),
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(fabRotation)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeHeader(
                profile = profile,
                onOpenProfile = onOpenProfile,
                onOpenSettings = onOpenSettings
            )

            // 头部与角色列表之间的分界线
            HorizontalDivider(thickness = 0.8.dp, color = colors.outline)

            if (roles.isEmpty()) {
                EmptyRoles(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(roles, key = { it.id }) { role ->
                        RoleRow(
                            role = role,
                            draft = drafts[role.id],
                            onClick = { onOpenRole(role.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 82.dp, end = 20.dp),
                            thickness = 0.6.dp,
                            color = colors.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

/** 浮动菜单项：左边标签胶囊，右边圆形按钮。 */
@Composable
private fun FloatingActionItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            color = colors.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HomeHeader(
    profile: UserProfile,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val displayName = if (profile.name.isBlank()) {
        stringResource(R.string.home_user)
    } else {
        profile.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onOpenProfile),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(path = profile.avatarPath, size = 42.dp)

            Spacer(Modifier.width(12.dp))

            Text(
                text = displayName,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onOpenSettings, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun RoleRow(role: Role, draft: Draft?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val time = relativeTime(
        when {
            draft != null && draft.updatedAt > 0L -> draft.updatedAt
            role.lastMessageAt > 0L -> role.lastMessageAt
            role.createdAt > 0L -> role.createdAt
            else -> 0L
        }
    )
    val draftLabel = stringResource(R.string.draft_prefix)
    val preview = when {
        draft != null && draft.text.isNotBlank() -> buildAnnotatedString {
            withStyle(SpanStyle(color = colors.error)) { append(draftLabel) }
            append(" ")
            append(draft.text)
        }
        role.lastMessage.isNotBlank() -> AnnotatedString(role.lastMessage)
        else -> AnnotatedString(stringResource(R.string.chat_no_message))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(path = role.avatarPath, size = 48.dp)

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = role.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (time.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = time,
                        fontSize = 11.5.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = preview,
                fontSize = 12.5.sp,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyRoles(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(colors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat_bubble),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.home_empty_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.home_empty_subtitle),
            fontSize = 13.sp,
            color = colors.onSurfaceVariant
        )
    }
}
