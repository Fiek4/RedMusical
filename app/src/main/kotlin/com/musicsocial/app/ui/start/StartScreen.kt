package com.musicsocial.app.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.ui.components.AppBackground
import com.musicsocial.app.ui.components.MusicLogo
import com.musicsocial.domain.usecase.StartDestination
import com.musicsocial.presentation.session.StartViewModel
import org.koin.androidx.compose.koinViewModel

/** Logo y spinner mientras se revisa si hay sesión guardada. */
@Composable
fun StartScreen(
    onDestination: (StartDestination) -> Unit,
    viewModel: StartViewModel = koinViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    LaunchedEffect(destination) {
        destination?.let(onDestination)
    }
    AppBackground {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MusicLogo()
            CircularProgressIndicator()
        }
    }
}
