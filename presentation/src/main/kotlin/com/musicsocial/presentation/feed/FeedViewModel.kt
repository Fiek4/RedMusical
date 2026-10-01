package com.musicsocial.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.CallFilter
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock

data class FeedUiState(
    val calls: List<CollabCall> = emptyList(),
    val filter: CallFilter = CallFilter(),
    val isLoading: Boolean = true,
)

/** Pantalla de inicio: convocatorias abiertas filtradas por rol y género. */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val calls: CollabCallRepository,
    private val clock: Clock,
) : ViewModel() {

    private val filter = MutableStateFlow(CallFilter())

    val uiState: StateFlow<FeedUiState> = filter
        .flatMapLatest { f ->
            calls.observeOpenCalls(f, clock.instant()).map { list -> FeedUiState(list, f, isLoading = false) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    init {
        // Por defecto mostramos convocatorias que buscan lo que el usuario sabe hacer.
        viewModelScope.launch {
            val profile = auth.currentUserId()?.let { users.getProfile(it) } ?: return@launch
            filter.value = CallFilter(roles = profile.roles, genres = profile.genres)
        }
    }

    fun onRoleToggle(role: ArtistRole) = filter.update {
        it.copy(roles = if (role in it.roles) it.roles - role else it.roles + role)
    }

    fun onGenreToggle(genre: Genre) = filter.update {
        it.copy(genres = if (genre in it.genres) it.genres - genre else it.genres + genre)
    }

    fun onClearFilters() {
        filter.value = CallFilter()
    }
}
