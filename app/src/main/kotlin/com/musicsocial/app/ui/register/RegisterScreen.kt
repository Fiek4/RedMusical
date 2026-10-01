package com.musicsocial.app.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.ui.message
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.auth.RegisterUiState
import com.musicsocial.presentation.auth.RegisterViewModel
import org.koin.androidx.compose.koinViewModel

/** Conecta el ViewModel con la UI. La UI solo dibuja el estado y avisa eventos. */
@Composable
fun RegisterScreen(viewModel: RegisterViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::onSubmit,
        onErrorShown = viewModel::onErrorShown,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val errorText = state.error?.message()
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            onErrorShown()
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
            Text(stringResource(R.string.register_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.register_subtitle), style = MaterialTheme.typography.bodyMedium)

            FormField(
                value = state.form.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.field_email),
                error = state.errors[Field.EMAIL],
                keyboardType = KeyboardType.Email,
            )
            FormField(
                value = state.form.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.field_password),
                error = state.errors[Field.PASSWORD],
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )
            FormField(
                value = state.form.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.field_confirm_password),
                error = state.errors[Field.CONFIRM_PASSWORD],
                keyboardType = KeyboardType.Password,
                isPassword = true,
            )

            Button(onClick = onSubmit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.action_register))
                }
            }

            state.registeredUserId?.let { id ->
                Text(stringResource(R.string.register_success, id), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: ValidationError?,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error.message()) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
    )
}
