package com.musicsocial.domain.validation

/** Límites del MVP en un solo lugar para poder ajustarlos fácilmente. */
object Limits {
    const val PASSWORD_MIN = 8
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 20
    const val ARTIST_NAME_MIN = 2
    const val ARTIST_NAME_MAX = 40
    const val BIO_MAX = 300
    const val CITY_MAX = 60
    const val MAX_LINKS = 5

    const val TITLE_MIN = 3
    const val TITLE_MAX = 80
    const val DESCRIPTION_MIN = 10
    const val DESCRIPTION_MAX = 1000
    const val APPLICATION_MESSAGE_MAX = 300
    const val CHAT_MESSAGE_MAX = 2000

    const val BPM_MIN = 40
    const val BPM_MAX = 250

    const val MAX_AUDIO_BYTES = 20L * 1024 * 1024
    const val TRACK_MAX_SECONDS = 5 * 60
    const val CALL_AUDIO_MAX_SECONDS = 5 * 60
    const val DEMO_MAX_SECONDS = 60
    const val VOICE_NOTE_MAX_SECONDS = 2 * 60

    const val MAX_DEADLINE_DAYS = 90L

    const val FREE_MAX_TRACKS = 5
    const val FREE_MAX_ACTIVE_CALLS = 2
}
