package com.musicsocial.domain.usecase

import com.musicsocial.domain.validation.ValidationResult

/** Resultado de un caso de uso: éxito, datos inválidos o error inesperado (red, servidor). */
sealed interface UseCaseResult<out T> {
    data class Success<T>(val value: T) : UseCaseResult<T>
    data class Invalid(val validation: ValidationResult) : UseCaseResult<Nothing>
    data class Failure(val error: Throwable) : UseCaseResult<Nothing>
}

/** Ejecuta [block] y convierte cualquier excepción en [UseCaseResult.Failure]. */
internal inline fun <T> runCatchingUseCase(block: () -> UseCaseResult<T>): UseCaseResult<T> =
    try {
        block()
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        UseCaseResult.Failure(e)
    }
