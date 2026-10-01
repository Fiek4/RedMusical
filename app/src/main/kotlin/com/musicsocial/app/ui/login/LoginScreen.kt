package com.musicsocial.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.ui.components.FormField
import com.musicsocial.app.ui.components.LoadingButton
import com.musicsocial.app.ui.message
import com.musicsocial.domain.validation.Field
import com.musicsocial.presentation.auth.LoginViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorText = state.error?.message()

    LaunchedEffect(state.signedIn) { if (state.signedIn) onSignedIn() }
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MusicHeader()
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                FormField(
                    value = state.form.email,
                    onValueChange = viewModel::onEmailChange,
                    label = stringResource(R.string.field_email),
                    error = state.errors[Field.EMAIL],
                    keyboardType = KeyboardType.Email,
                )
                FormField(
                    value = state.form.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = stringResource(R.string.field_password),
                    error = state.errors[Field.PASSWORD],
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                )
                state.errors[Field.GENERAL]?.let {
                    Text(it.message(), color = MaterialTheme.colorScheme.error)
                }

                LoadingButton(stringResource(R.string.action_login), state.isLoading, viewModel::onSubmit)
                TextButton(onClick = onGoToRegister) { Text(stringResource(R.string.login_no_account)) }
            }
        }
    }
}

/** Ilustración provisional: audífonos dentro de un círculo con degradado y dos notas musicales. */
@Composable
private fun MusicHeader() {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .background(
                    brush = Brush.linearGradient(listOf(colors.primary, colors.tertiary)),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_headphones),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .rotate(15f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = colors.tertiary,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp)
                .size(24.dp)
                .rotate(-20f),
        )
    }
}
