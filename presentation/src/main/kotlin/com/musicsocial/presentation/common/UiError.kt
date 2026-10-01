package com.musicsocial.presentation.common

import com.musicsocial.domain.usecase.NotAllowedException
import com.musicsocial.domain.usecase.NotFoundException
import com.musicsocial.domain.usecase.NotLoggedInException

/** Errores generales que la UI muestra en un Snackbar o diálogo (no son de un campo). */
enum class UiError { GENERIC, NOT_LOGGED_IN, NOT_FOUND, NOT_ALLOWED }

fun Throwable.toUiError(): UiError = when (this) {
    is NotLoggedInException -> UiError.NOT_LOGGED_IN
    is NotFoundException -> UiError.NOT_FOUND
    is NotAllowedException -> UiError.NOT_ALLOWED
    else -> UiError.GENERIC
}
