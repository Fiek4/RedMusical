package com.musicsocial.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.form.LoginForm
import com.musicsocial.domain.usecase.SignInUseCase
import com.musicsocial.domain.usecase.UseCaseResult
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.UserValidator
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.common.UiError
import com.musicsocial.presentation.common.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val form: LoginForm = LoginForm(),
    val errors: Map<Field, ValidationError> = emptyMap(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
    /** true cuando inició sesión; la UI navega a la pantalla de inicio. */
    val signedIn: Boolean = false,
)

class LoginViewModel(private val signIn: SignInUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var submitted = false

    fun onEmailChange(value: String) = updateForm { it.copy(email = value) }
    fun onPasswordChange(value: String) = updateForm { it.copy(password = value) }
    fun onErrorShown() = _uiState.update { it.copy(error = null) }

    fun onSubmit() {
        if (_uiState.value.isLoading) return
        submitted = true
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = signIn(_uiState.value.form)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isLoading = false, errors = emptyMap(), signedIn = true)
                }
                is UseCaseResult.Invalid -> _uiState.update {
                    it.copy(isLoading = false, errors = result.validation.errors)
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error.toUiError())
                }
            }
        }
    }

    private fun updateForm(change: (LoginForm) -> LoginForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            errors = if (submitted) UserValidator.validateLogin(form).errors else state.errors,
        )
    }
}
