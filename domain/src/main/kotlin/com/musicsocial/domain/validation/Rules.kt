package com.musicsocial.domain.validation

import com.musicsocial.domain.model.AudioFile
import java.time.Duration
import java.time.Instant

/**
 * Reglas sueltas y reutilizables. Cada una devuelve null si el valor es válido.
 * El ViewModel también puede usarlas para validar campo por campo mientras el usuario escribe.
 */
object Rules {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val USERNAME_REGEX = Regex("^[a-z0-9._]+$")
    private val URL_REGEX = Regex("^https?://[^\\s/$.?#].[^\\s]*$", RegexOption.IGNORE_CASE)
    private val MUSICAL_KEY_REGEX = Regex("^[A-G](#|b)?\\s?(m|maj|min|major|minor)?$", RegexOption.IGNORE_CASE)

    fun required(value: String): ValidationError? =
        if (value.isBlank()) ValidationError.Required else null

    fun minLength(value: String, min: Int): ValidationError? =
        if (value.trim().length < min) ValidationError.TooShort(min) else null

    fun maxLength(value: String, max: Int): ValidationError? =
        if (value.trim().length > max) ValidationError.TooLong(max) else null

    fun email(value: String): ValidationError? =
        if (!EMAIL_REGEX.matches(value.trim())) ValidationError.InvalidFormat else null

    /** Mínimo 8 caracteres, con al menos una letra y un número. */
    fun password(value: String): ValidationError? = when {
        value.length < Limits.PASSWORD_MIN -> ValidationError.TooShort(Limits.PASSWORD_MIN)
        !value.any { it.isLetter() } || !value.any { it.isDigit() } -> ValidationError.PasswordTooWeak
        else -> null
    }

    /** Solo minúsculas, números, punto y guion bajo (como @usuario). */
    fun username(value: String): ValidationError? =
        if (!USERNAME_REGEX.matches(value.trim())) ValidationError.InvalidFormat else null

    fun url(value: String): ValidationError? =
        if (!URL_REGEX.matches(value.trim())) ValidationError.InvalidFormat else null

    fun musicalKey(value: String): ValidationError? =
        if (value.isNotBlank() && !MUSICAL_KEY_REGEX.matches(value.trim())) ValidationError.InvalidFormat else null

    /** BPM es opcional: vacío es válido. */
    fun bpm(value: String): ValidationError? {
        if (value.isBlank()) return null
        val bpm = value.trim().toIntOrNull() ?: return ValidationError.InvalidFormat
        return if (bpm !in Limits.BPM_MIN..Limits.BPM_MAX) {
            ValidationError.OutOfRange(Limits.BPM_MIN, Limits.BPM_MAX)
        } else {
            null
        }
    }

    fun <T> notNull(value: T?): ValidationError? =
        if (value == null) ValidationError.Required else null

    fun <T> notEmpty(values: Collection<T>): ValidationError? =
        if (values.isEmpty()) ValidationError.Required else null

    fun <T> maxItems(values: Collection<T>, max: Int): ValidationError? =
        if (values.size > max) ValidationError.TooMany(max) else null

    fun audio(audio: AudioFile?, maxSeconds: Int): ValidationError? = when {
        audio == null -> ValidationError.Required
        audio.sizeBytes <= 0 || audio.durationSeconds <= 0 -> ValidationError.EmptyAudio
        audio.sizeBytes > Limits.MAX_AUDIO_BYTES -> ValidationError.FileTooLarge(Limits.MAX_AUDIO_BYTES)
        audio.durationSeconds > maxSeconds -> ValidationError.AudioTooLong(maxSeconds)
        else -> null
    }

    fun futureDate(date: Instant?, now: Instant, maxDays: Long): ValidationError? = when {
        date == null -> ValidationError.Required
        !date.isAfter(now) -> ValidationError.DateInPast
        Duration.between(now, date).toDays() > maxDays -> ValidationError.DateTooFar(maxDays)
        else -> null
    }
}
