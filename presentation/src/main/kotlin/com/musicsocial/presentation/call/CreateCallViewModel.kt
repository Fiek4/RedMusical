package com.musicsocial.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.usecase.CreateCallUseCase
import com.musicsocial.domain.usecase.UseCaseResult
import com.musicsocial.domain.validation.CollabCallValidator
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.common.UiError
import com.musicsocial.presentation.common.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

data class CreateCallUiState(
    val form: CollabCallForm = CollabCallForm(),
    val errors: Map<Field, ValidationError> = emptyMap(),
    val isPublishing: Boolean = false,
    val error: UiError? = null,
    /** Cuando no es null, la UI navega al detalle de la convocatoria creada. */
    val publishedCallId: String? = null,
) {
    /** El campo de presupuesto solo se muestra si el acuerdo es pagado. */
    val showBudget: Boolean get() = form.dealType == DealType.PAID
}

class CreateCallViewModel(
    private val createCall: CreateCallUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCallUiState())
    val uiState: StateFlow<CreateCallUiState> = _uiState.asStateFlow()

    private var submitted = false

    fun onTitleChange(value: String) = updateForm { it.copy(title = value) }
    fun onLookingForChange(value: ArtistRole) = updateForm { it.copy(lookingFor = value) }
    fun onGenreChange(value: Genre) = updateForm { it.copy(genre = value) }
    fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }
    fun onBpmChange(value: String) = updateForm { it.copy(bpm = value.filter(Char::isDigit).take(3)) }
    fun onMusicalKeyChange(value: String) = updateForm { it.copy(musicalKey = value) }
    fun onAudioSelected(audio: AudioFile?) = updateForm { it.copy(referenceAudio = audio) }
    fun onDeadlineChange(value: Instant) = updateForm { it.copy(deadline = value) }
    fun onDealTypeChange(value: DealType) = updateForm { it.copy(dealType = value) }
    fun onBudgetMinChange(value: String) = updateForm { it.copy(budgetMin = value) }
    fun onBudgetMaxChange(value: String) = updateForm { it.copy(budgetMax = value) }
    fun onErrorShown() = _uiState.update { it.copy(error = null) }

    fun onPublish() {
        if (_uiState.value.isPublishing) return
        submitted = true
        _uiState.update { it.copy(isPublishing = true, error = null) }

        viewModelScope.launch {
            when (val result = createCall(_uiState.value.form)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isPublishing = false, errors = emptyMap(), publishedCallId = result.value.id)
                }
                is UseCaseResult.Invalid -> _uiState.update {
                    it.copy(isPublishing = false, errors = result.validation.errors)
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isPublishing = false, error = result.error.toUiError())
                }
            }
        }
    }

    private fun updateForm(change: (CollabCallForm) -> CollabCallForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            errors = if (submitted) CollabCallValidator.validateFields(form, clock.instant()).errors else state.errors,
        )
    }
}
