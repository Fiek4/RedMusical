package com.musicsocial.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.form.ApplicationForm
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.usecase.ApplyToCallUseCase
import com.musicsocial.domain.usecase.UseCaseResult
import com.musicsocial.domain.validation.ApplicationValidator
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.common.UiError
import com.musicsocial.presentation.common.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ApplyUiState(
    val call: CollabCall? = null,
    val form: ApplicationForm = ApplicationForm(),
    val errors: Map<Field, ValidationError> = emptyMap(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: UiError? = null,
    val applied: Boolean = false,
)

/** Detalle de una convocatoria + formulario para postularse. */
class ApplyViewModel(
    private val callId: String,
    private val calls: CollabCallRepository,
    private val applyToCall: ApplyToCallUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplyUiState())
    val uiState: StateFlow<ApplyUiState> = _uiState.asStateFlow()

    private var submitted = false

    init {
        viewModelScope.launch {
            val call = calls.getCall(callId)
            _uiState.update {
                it.copy(call = call, isLoading = false, error = if (call == null) UiError.NOT_FOUND else null)
            }
        }
    }

    fun onDemoSelected(audio: AudioFile?) = updateForm { it.copy(demo = audio) }
    fun onMessageChange(value: String) = updateForm { it.copy(message = value) }
    fun onErrorShown() = _uiState.update { it.copy(error = null) }

    fun onSend() {
        if (_uiState.value.isSending) return
        submitted = true
        _uiState.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            when (val result = applyToCall(callId, _uiState.value.form)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isSending = false, errors = emptyMap(), applied = true)
                }
                is UseCaseResult.Invalid -> _uiState.update {
                    it.copy(isSending = false, errors = result.validation.errors)
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isSending = false, error = result.error.toUiError())
                }
            }
        }
    }

    private fun updateForm(change: (ApplicationForm) -> ApplicationForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            errors = if (submitted) ApplicationValidator.validateFields(form).errors else state.errors,
        )
    }
}
