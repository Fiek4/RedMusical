package com.musicsocial.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicsocial.app.R
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.common.UiError

/** Traduce los errores del dominio (sin texto) a mensajes de strings.xml. */
@Composable
fun ValidationError.message(): String = when (this) {
    ValidationError.Required -> stringResource(R.string.error_required)
    is ValidationError.TooShort -> stringResource(R.string.error_too_short, min)
    is ValidationError.TooLong -> stringResource(R.string.error_too_long, max)
    ValidationError.InvalidFormat -> stringResource(R.string.error_invalid_format)
    is ValidationError.OutOfRange -> stringResource(R.string.error_out_of_range, min.toString(), max.toString())
    is ValidationError.TooMany -> stringResource(R.string.error_too_many, max)
    ValidationError.PasswordTooWeak -> stringResource(R.string.error_password_weak)
    ValidationError.PasswordsDoNotMatch -> stringResource(R.string.error_passwords_do_not_match)
    ValidationError.AlreadyTaken -> stringResource(R.string.error_already_taken)
    ValidationError.InvalidCredentials -> stringResource(R.string.error_invalid_credentials)
    is ValidationError.FileTooLarge -> stringResource(R.string.error_file_too_large, (maxBytes / (1024 * 1024)).toInt())
    is ValidationError.AudioTooLong -> stringResource(R.string.error_audio_too_long, maxSeconds)
    ValidationError.EmptyAudio -> stringResource(R.string.error_empty_audio)
    ValidationError.DateInPast -> stringResource(R.string.error_date_in_past)
    is ValidationError.DateTooFar -> stringResource(R.string.error_date_too_far, maxDays.toInt())
    ValidationError.InvalidBudgetRange -> stringResource(R.string.error_invalid_budget)
    is ValidationError.PlanLimitReached -> stringResource(R.string.error_plan_limit, max)
    ValidationError.CannotApplyToOwnCall -> stringResource(R.string.error_own_call)
    ValidationError.AlreadyApplied -> stringResource(R.string.error_already_applied)
    ValidationError.CallClosed -> stringResource(R.string.error_call_closed)
    ValidationError.NotAParticipant -> stringResource(R.string.error_not_participant)
}

@Composable
fun UiError.message(): String = when (this) {
    UiError.GENERIC -> stringResource(R.string.ui_error_generic)
    UiError.NOT_LOGGED_IN -> stringResource(R.string.ui_error_not_logged_in)
    UiError.NOT_FOUND -> stringResource(R.string.ui_error_not_found)
    UiError.NOT_ALLOWED -> stringResource(R.string.ui_error_not_allowed)
}
