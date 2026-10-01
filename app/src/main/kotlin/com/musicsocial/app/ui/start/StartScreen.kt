package com.musicsocial.app.ui.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.domain.usecase.StartDestination
import com.musicsocial.presentation.session.StartViewModel
import org.koin.androidx.compose.koinViewModel

/** Spinner mientras se revisa si hay sesión guardada. */
@Composable
fun StartScreen(
    onDestination: (StartDestination) -> Unit,
    viewModel: StartViewModel = koinViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    LaunchedEffect(destination) {
        destination?.let(onDestination)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
