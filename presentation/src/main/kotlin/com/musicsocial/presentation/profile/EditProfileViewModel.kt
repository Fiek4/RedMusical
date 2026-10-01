package com.musicsocial.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.form.ProfileForm
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.ExternalLink
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.usecase.SaveProfileUseCase
import com.musicsocial.domain.usecase.UseCaseResult
import com.musicsocial.domain.usecase.toForm
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

data class EditProfileUiState(
    val form: ProfileForm = ProfileForm(),
    val errors: Map<Field, ValidationError> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** true si el usuario aún no tenía perfil (pantalla de onboarding). */
    val isNewProfile: Boolean = false,
    val error: UiError? = null,
    val saved: Boolean = false,
)

/** Sirve para el onboarding (perfil nuevo) y para editar el perfil después. */
class EditProfileViewModel(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val saveProfile: SaveProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var submitted = false

    init {
        viewModelScope.launch {
            val profile = auth.currentUserId()?.let { users.getProfile(it) }
            _uiState.update {
                it.copy(
                    form = profile?.toForm() ?: ProfileForm(),
                    isNewProfile = profile == null,
                    isLoading = false,
                )
            }
        }
    }

    fun onUsernameChange(value: String) = updateForm { it.copy(username = value) }
    fun onArtistNameChange(value: String) = updateForm { it.copy(artistName = value) }
    fun onBioChange(value: String) = updateForm { it.copy(bio = value) }
    fun onCityChange(value: String) = updateForm { it.copy(city = value) }
    fun onRoleToggle(role: ArtistRole) = updateForm { it.copy(roles = it.roles.toggle(role)) }
    fun onGenreToggle(genre: Genre) = updateForm { it.copy(genres = it.genres.toggle(genre)) }
    fun onAddLink(link: ExternalLink) = updateForm { it.copy(links = it.links + link) }
    fun onRemoveLink(link: ExternalLink) = updateForm { it.copy(links = it.links - link) }
    fun onErrorShown() = _uiState.update { it.copy(error = null) }

    fun onSave() {
        if (_uiState.value.isSaving) return
        submitted = true
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            when (val result = saveProfile(_uiState.value.form)) {
                is UseCaseResult.Success -> _uiState.update {
                    it.copy(isSaving = false, errors = emptyMap(), saved = true, isNewProfile = false)
                }
                is UseCaseResult.Invalid -> _uiState.update {
                    it.copy(isSaving = false, errors = result.validation.errors)
                }
                is UseCaseResult.Failure -> _uiState.update {
                    it.copy(isSaving = false, error = result.error.toUiError())
                }
            }
        }
    }

    private fun updateForm(change: (ProfileForm) -> ProfileForm) = _uiState.update { state ->
        val form = change(state.form)
        state.copy(
            form = form,
            saved = false,
            errors = if (submitted) UserValidator.validateProfile(form).errors else state.errors,
        )
    }

    private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
}
