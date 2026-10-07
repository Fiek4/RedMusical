package com.musicsocial.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.domain.repository.ApplicationRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.usecase.ReviewApplicationUseCase
import com.musicsocial.domain.usecase.UseCaseResult
import com.musicsocial.presentation.common.UiError
import com.musicsocial.presentation.common.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewApplicationsUiState(
    val pending: List<CollabApplication> = emptyList(),
    val accepted: List<CollabApplication> = emptyList(),
    /** Perfil de cada postulante por id, para mostrar su nombre y roles. */
    val applicants: Map<String, UserProfile> = emptyMap(),
    val isLoading: Boolean = true,
    /** Postulación que se está aceptando o descartando, para deshabilitar sus botones. */
    val processingId: String? = null,
    val error: UiError? = null,
    /** Cuando no es null, la UI abre ese chat. */
    val openConversationId: String? = null,
)

/** El autor escucha las demos y acepta (abre chat) o descarta. */
class ReviewApplicationsViewModel(
    callId: String,
    applications: ApplicationRepository,
    private val users: UserRepository,
    private val review: ReviewApplicationUseCase,
) : ViewModel() {

    private data class Local(
        val processingId: String? = null,
        val error: UiError? = null,
        val openConversationId: String? = null,
    )

    private val local = MutableStateFlow(Local())
    private val applicants = MutableStateFlow<Map<String, UserProfile>>(emptyMap())

    val uiState: StateFlow<ReviewApplicationsUiState> =
        combine(
            applications.observeApplications(callId).onEach(::loadApplicants),
            local,
            applicants,
        ) { list, l, profiles ->
            ReviewApplicationsUiState(
                pending = list.filter { it.status == ApplicationStatus.PENDING },
                accepted = list.filter { it.status == ApplicationStatus.ACCEPTED },
                applicants = profiles,
                isLoading = false,
                processingId = l.processingId,
                error = l.error,
                openConversationId = l.openConversationId,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReviewApplicationsUiState())

    fun onAccept(applicationId: String) = process(applicationId) {
        when (val result = review.accept(applicationId)) {
            is UseCaseResult.Success -> local.update { it.copy(openConversationId = result.value.id) }
            is UseCaseResult.Failure -> local.update { it.copy(error = result.error.toUiError()) }
            is UseCaseResult.Invalid -> Unit
        }
    }

    fun onReject(applicationId: String) = process(applicationId) {
        val result = review.reject(applicationId)
        if (result is UseCaseResult.Failure) local.update { it.copy(error = result.error.toUiError()) }
    }

    fun onConversationOpened() = local.update { it.copy(openConversationId = null) }
    fun onErrorShown() = local.update { it.copy(error = null) }

    /** Busca los perfiles que aún no tenemos. Si uno falla, la tarjeta muestra un nombre genérico. */
    private suspend fun loadApplicants(list: List<CollabApplication>) {
        list.map { it.applicantId }.distinct().filter { it !in applicants.value }.forEach { id ->
            runCatching { users.getProfile(id) }.getOrNull()?.let { profile -> applicants.update { it + (id to profile) } }
        }
    }

    private fun process(applicationId: String, action: suspend () -> Unit) {
        if (local.value.processingId != null) return
        local.update { it.copy(processingId = applicationId, error = null) }
        viewModelScope.launch {
            action()
            local.update { it.copy(processingId = null) }
        }
    }
}
