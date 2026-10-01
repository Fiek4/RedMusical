package com.musicsocial.domain.validation

/**
 * Errores de validación. No llevan texto: la capa de UI los traduce a
 * mensajes (strings.xml), así el dominio no depende del idioma.
 */
sealed interface ValidationError {
    data object Required : ValidationError
    data class TooShort(val min: Int) : ValidationError
    data class TooLong(val max: Int) : ValidationError
    data object InvalidFormat : ValidationError
    data class OutOfRange(val min: Number, val max: Number) : ValidationError
    data class TooMany(val max: Int) : ValidationError
    data object PasswordTooWeak : ValidationError
    data object PasswordsDoNotMatch : ValidationError
    /** Email o nombre de usuario ya usado por otra cuenta. */
    data object AlreadyTaken : ValidationError

    // Audio
    data class FileTooLarge(val maxBytes: Long) : ValidationError
    data class AudioTooLong(val maxSeconds: Int) : ValidationError
    data object EmptyAudio : ValidationError

    // Fechas
    data object DateInPast : ValidationError
    data class DateTooFar(val maxDays: Long) : ValidationError

    // Reglas de negocio
    data object InvalidBudgetRange : ValidationError
    data class PlanLimitReached(val max: Int) : ValidationError
    data object CannotApplyToOwnCall : ValidationError
    data object AlreadyApplied : ValidationError
    data object CallClosed : ValidationError
    data object NotAParticipant : ValidationError
}

/** Campos que pueden tener error, para que la UI sepa dónde mostrarlo. */
enum class Field {
    EMAIL, PASSWORD, CONFIRM_PASSWORD,
    USERNAME, ARTIST_NAME, BIO, ROLES, GENRES, CITY, LINKS,
    TITLE, DESCRIPTION, LOOKING_FOR, GENRE, BPM, MUSICAL_KEY, AUDIO, DEADLINE, DEAL_TYPE, BUDGET,
    MESSAGE,
    /** Errores que no son de un campo concreto (reglas de negocio). */
    GENERAL,
}
