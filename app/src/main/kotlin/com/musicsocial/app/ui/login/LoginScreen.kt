package com.musicsocial.app.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)

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
