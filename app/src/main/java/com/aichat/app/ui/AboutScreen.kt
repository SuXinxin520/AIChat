package com.aichat.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.app.BuildConfig
import com.aichat.app.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            SubPageTopBar(
                title = stringResource(R.string.settings_about),
                onBack = onBack,
                showDivider = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_about_logo),
                contentDescription = null,
                modifier = Modifier.size(88.dp)
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.about_author, stringResource(R.string.author_name)),
                fontSize = 13.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}
