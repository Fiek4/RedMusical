package com.musicsocial.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicsocial.domain.usecase.GetStartDestinationUseCase
import com.musicsocial.domain.usecase.StartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Pantalla de carga al abrir la app: decide si ir a login, onboarding o inicio. */
class StartViewModel(private val getStartDestination: GetStartDestinationUseCase) : ViewModel() {

    /** null mientras se carga la sesión. */
    private val _destination = MutableStateFlow<StartDestination?>(null)
    val destination: StateFlow<StartDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch { _destination.value = getStartDestination() }
    }
}
