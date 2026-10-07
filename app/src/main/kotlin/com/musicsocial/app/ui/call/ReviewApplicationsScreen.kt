package com.musicsocial.app.ui.call

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.ui.components.AppBackground
import com.musicsocial.app.ui.components.AudioPlayerButton
import com.musicsocial.app.ui.components.FormScreen
import com.musicsocial.app.ui.components.GradientText
import com.musicsocial.app.ui.label
import com.musicsocial.app.ui.message
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.presentation.call.ReviewApplicationsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** El autor escucha las demos de su convocatoria y acepta o descarta a cada postulante. */
@Composable
fun ReviewApplicationsScreen(
    callId: String,
    onBack: () -> Unit,
    viewModel: ReviewApplicationsViewModel = koinViewModel(key = "review-$callId") { parametersOf(callId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorText = state.error?.message()
    val chatOpened = stringResource(R.string.review_chat_opened)

    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }
    // El chat todavía no existe en la app: por ahora avisamos que quedó abierto.
    LaunchedEffect(state.openConversationId) {
        if (state.openConversationId != null) {
            viewModel.onConversationOpened()
            snackbar.showSnackbar(chatOpened)
        }
    }

    if (state.isLoading) {
        AppBackground { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
        return
    }

    FormScreen(snackbar, centered = false) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
        GradientText(stringResource(R.string.review_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(R.string.review_pending_count, state.pending.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.pending.isEmpty() && state.accepted.isEmpty()) {
            Text(stringResource(R.string.review_empty), style = MaterialTheme.typography.bodyLarge)
        }

        state.pending.forEach { application ->
            ApplicationCard(
                application = application,
                applicant = state.applicants[application.applicantId],
                isProcessing = state.processingId == application.id,
                enabled = state.processingId == null,
                onAccept = { viewModel.onAccept(application.id) },
                onReject = { viewModel.onReject(application.id) },
            )
        }

        if (state.accepted.isNotEmpty()) {
            Text(stringResource(R.string.review_accepted), style = MaterialTheme.typography.titleMedium)
            state.accepted.forEach { application ->
                val name = state.applicants[application.applicantId]?.artistName ?: stringResource(R.string.review_unknown_artist)
                Text(stringResource(R.string.review_accepted_item, name), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ApplicationCard(
    application: CollabApplication,
    applicant: UserProfile?,
    isProcessing: Boolean,
    enabled: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                applicant?.artistName ?: stringResource(R.string.review_unknown_artist),
                style = MaterialTheme.typography.titleLarge,
            )
            applicant?.let { profile ->
                Text(
                    listOfNotNull(profile.roles.map { it.label() }.joinToString(" · "), profile.city).joinToString(" — "),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            if (application.message.isNotBlank()) {
                Text("“${application.message}”", style = MaterialTheme.typography.bodyMedium)
            }
            AudioPlayerButton(application.demo.uri, application.demo.durationSeconds)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onReject, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_reject))
                }
                Button(
                    onClick = onAccept,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(if (isProcessing) R.string.review_processing else R.string.action_accept))
                }
            }
        }
    }
}
