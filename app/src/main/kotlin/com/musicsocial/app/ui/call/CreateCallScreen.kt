package com.musicsocial.app.ui.call

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.audioDisplayName
import com.musicsocial.app.readAudioFile
import com.musicsocial.app.ui.components.ChipGroup
import com.musicsocial.app.ui.components.FormField
import com.musicsocial.app.ui.components.FormScreen
import com.musicsocial.app.ui.components.GradientText
import com.musicsocial.app.ui.components.LoadingButton
import com.musicsocial.app.ui.label
import com.musicsocial.app.ui.message
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.Limits
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.call.CreateCallViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Formulario para publicar una convocatoria: qué busco, con qué audio y hasta cuándo. */
@Composable
fun CreateCallScreen(
    onPublished: () -> Unit,
    onCancel: () -> Unit,
    viewModel: CreateCallViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val errorText = state.error?.message()
    val unsupportedAudio = stringResource(R.string.call_audio_unsupported)
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.publishedCallId) { if (state.publishedCallId != null) onPublished() }
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }

    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val audio = context.readAudioFile(uri)
        if (audio == null) {
            scope.launch { snackbar.showSnackbar(unsupportedAudio) }
        } else {
            viewModel.onAudioSelected(audio)
        }
    }

    FormScreen(snackbar, centered = false) {
        GradientText(stringResource(R.string.call_title), style = MaterialTheme.typography.headlineMedium)

        FormField(
            value = form.title,
            onValueChange = viewModel::onTitleChange,
            label = stringResource(R.string.field_call_title),
            error = state.errors[Field.TITLE],
        )

        SectionTitle(stringResource(R.string.call_looking_for))
        ChipGroup(ArtistRole.entries, setOfNotNull(form.lookingFor), viewModel::onLookingForChange) { it.label() }
        FieldError(state.errors[Field.LOOKING_FOR])

        SectionTitle(stringResource(R.string.call_genre))
        ChipGroup(Genre.entries, setOfNotNull(form.genre), viewModel::onGenreChange) { it.label() }
        FieldError(state.errors[Field.GENRE])

        FormField(
            value = form.description,
            onValueChange = viewModel::onDescriptionChange,
            label = stringResource(R.string.field_call_description),
            error = state.errors[Field.DESCRIPTION],
            singleLine = false,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                FormField(
                    value = form.bpm,
                    onValueChange = viewModel::onBpmChange,
                    label = stringResource(R.string.field_bpm),
                    error = state.errors[Field.BPM],
                    keyboardType = KeyboardType.Number,
                )
            }
            Box(Modifier.weight(1f)) {
                FormField(
                    value = form.musicalKey,
                    onValueChange = viewModel::onMusicalKeyChange,
                    label = stringResource(R.string.field_musical_key),
                    error = state.errors[Field.MUSICAL_KEY],
                )
            }
        }

        SectionTitle(stringResource(R.string.call_audio))
        OutlinedButton(onClick = { pickAudio.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(
                form.referenceAudio?.let { audio ->
                    stringResource(
                        R.string.call_audio_selected,
                        context.audioDisplayName(audio.uri),
                        audio.durationSeconds / 60,
                        audio.durationSeconds % 60,
                    )
                } ?: stringResource(R.string.call_audio_pick, Limits.CALL_AUDIO_MAX_SECONDS / 60),
            )
        }
        FieldError(state.errors[Field.AUDIO])

        SectionTitle(stringResource(R.string.call_deadline))
        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                form.deadline?.let { deadlineFormat.format(it.atZone(ZoneId.systemDefault())) }
                    ?: stringResource(R.string.call_deadline_pick),
            )
        }
        FieldError(state.errors[Field.DEADLINE])

        SectionTitle(stringResource(R.string.call_deal_type))
        ChipGroup(DealType.entries, setOfNotNull(form.dealType), viewModel::onDealTypeChange) { it.label() }
        FieldError(state.errors[Field.DEAL_TYPE])

        if (state.showBudget) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    FormField(
                        value = form.budgetMin,
                        onValueChange = viewModel::onBudgetMinChange,
                        label = stringResource(R.string.field_budget_min, form.currency),
                        error = null,
                        keyboardType = KeyboardType.Decimal,
                    )
                }
                Box(Modifier.weight(1f)) {
                    FormField(
                        value = form.budgetMax,
                        onValueChange = viewModel::onBudgetMaxChange,
                        label = stringResource(R.string.field_budget_max, form.currency),
                        error = null,
                        keyboardType = KeyboardType.Decimal,
                    )
                }
            }
            FieldError(state.errors[Field.BUDGET])
        }

        FieldError(state.errors[Field.GENERAL])

        LoadingButton(stringResource(R.string.action_publish_call), state.isPublishing, viewModel::onPublish)
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_cancel))
        }
    }

    if (showDatePicker) {
        DeadlinePicker(
            initial = form.deadline,
            onPicked = {
                viewModel.onDeadlineChange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

private val deadlineFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

/** Calendario que solo deja elegir desde mañana hasta el máximo de días permitido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePicker(initial: Instant?, onPicked: (Instant) -> Unit, onDismiss: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    // El DatePicker trabaja con la medianoche UTC de cada día.
    val firstDay = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val lastDay = today.plusDays(Limits.MAX_DEADLINE_DAYS - 1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atZone(zone)?.toLocalDate()?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in firstDay..lastDay
            override fun isSelectableYear(year: Int) = year in today.year..today.plusDays(Limits.MAX_DEADLINE_DAYS).year
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    // La convocatoria vence al final del día elegido, en la hora del teléfono.
                    val day = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onPicked(day.atTime(LocalTime.of(23, 59)).atZone(zone).toInstant())
                },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.action_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun FieldError(error: ValidationError?) {
    error?.let { Text(it.message(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
}
