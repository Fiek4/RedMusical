package com.musicsocial.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.form.RegisterForm
import com.musicsocial.domain.usecase.RegisterUseCase
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

data class RegisterUiState(
    val form: RegisterForm = RegisterForm(),
    val errors: Map<Field, ValidationError> = emptyMap(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
    /** Cuando no es null, la UI navega al onboarding. */
    val registeredUserId: String? = null,
)

class RegisterViewModel(private val register: RegisterUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /** Los errores se muestran recién después del primer intento de envío. */
    private var submitted = false

    fun onEmailChange(value: String) = updateForm { it.copy(email = value) }
    fun onPasswordChange(value: String) = updateForm { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = updateForm { it.copy(confirmPassword = value) }
    fun onErrorShown() = _uiState.update { it.copy(error = null) }

    fun onSubmit() {
        if (_uiState.value.isLoading) return
        submitted = true
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = register(_uiState.value.form)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isLoading = false, errors = emptyMap(), registeredUserId = result.value)
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

    private fun updateForm(change: (RegisterForm) -> RegisterForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            errors = if (submitted) UserValidator.validateRegister(form).errors else state.errors,
        )
    }
}
