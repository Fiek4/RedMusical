package com.musicsocial.app.ui.call

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.audioDisplayName
import com.musicsocial.app.readAudioFile
import com.musicsocial.app.ui.components.AppBackground
import com.musicsocial.app.ui.components.AudioPlayerButton
import com.musicsocial.app.ui.components.FormField
import com.musicsocial.app.ui.components.FormScreen
import com.musicsocial.app.ui.components.GradientText
import com.musicsocial.app.ui.components.LoadingButton
import com.musicsocial.app.ui.label
import com.musicsocial.app.ui.message
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.Limits
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.call.ApplyViewModel
import com.musicsocial.presentation.common.UiError
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Detalle de una convocatoria: escuchar el audio y postularse con una demo. */
@Composable
fun CallDetailScreen(
    callId: String,
    onBack: () -> Unit,
    viewModel: ApplyViewModel = koinViewModel(key = callId) { parametersOf(callId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val errorText = state.error?.message()
    val unsupportedAudio = stringResource(R.string.call_audio_unsupported)

    LaunchedEffect(errorText) {
        // "No encontrada" se muestra en la pantalla misma, no en un aviso.
        if (errorText != null && state.error != UiError.NOT_FOUND) {
            snackbar.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }

    val pickDemo = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val audio = context.readAudioFile(uri)
        if (audio == null) {
            scope.launch { snackbar.showSnackbar(unsupportedAudio) }
        } else {
            viewModel.onDemoSelected(audio)
        }
    }

    if (state.isLoading) {
        AppBackground { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
        return
    }

    FormScreen(snackbar, centered = false) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }

        val call = state.call
        if (call != null) CallInfo(call)

        when {
            call == null -> Text(stringResource(R.string.ui_error_not_found), style = MaterialTheme.typography.bodyLarge)
            state.isOwnCall -> Notice(stringResource(R.string.detail_own_call))
            state.applied || state.alreadyApplied -> Notice(stringResource(R.string.detail_applied))
            else -> {
                GradientText(stringResource(R.string.detail_apply_title), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.detail_apply_hint, Limits.DEMO_MAX_SECONDS),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { pickDemo.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        state.form.demo?.let { demo ->
                            stringResource(
                                R.string.call_audio_selected,
                                context.audioDisplayName(demo.uri),
                                demo.durationSeconds / 60,
                                demo.durationSeconds % 60,
                            )
                        } ?: stringResource(R.string.detail_pick_demo),
                    )
                }
                FieldError(state.errors[Field.AUDIO])
                FormField(
                    value = state.form.message,
                    onValueChange = viewModel::onMessageChange,
                    label = stringResource(R.string.field_apply_message, Limits.APPLICATION_MESSAGE_MAX),
                    error = state.errors[Field.MESSAGE],
                    singleLine = false,
                )
                FieldError(state.errors[Field.GENERAL])
                LoadingButton(stringResource(R.string.action_send_application), state.isSending, viewModel::onSend)
            }
        }
    }
}

private val deadlineFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

@Composable
private fun CallInfo(call: CollabCall) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pill(call.lookingFor.label(), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        Pill(call.genre.label(), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }
    GradientText(call.title, style = MaterialTheme.typography.headlineSmall)
    Text(call.description, style = MaterialTheme.typography.bodyLarge)

    AudioPlayerButton(call.referenceAudio.uri, call.referenceAudio.durationSeconds)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        call.bpm?.let { InfoLine(stringResource(R.string.detail_bpm), it.toString()) }
        call.musicalKey?.let { InfoLine(stringResource(R.string.detail_key), it) }
        InfoLine(
            stringResource(R.string.call_deal_type),
            call.budget?.takeIf { call.dealType == DealType.PAID }?.let {
                stringResource(R.string.detail_budget, call.dealType.label(), it.minCents / 100, it.maxCents / 100, it.currency)
            } ?: call.dealType.label(),
        )
        InfoLine(
            stringResource(R.string.call_deadline),
            deadlineFormat.format(call.deadline.atZone(ZoneId.systemDefault())),
        )
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun Notice(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Pill(text: String, container: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun FieldError(error: ValidationError?) {
    error?.let { Text(it.message(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
}
