package com.musicsocial.domain.model

enum class ArtistRole {
    PRODUCER,
    SINGER,
    RAPPER,
    INSTRUMENTALIST,
    MIX_ENGINEER,
    SONGWRITER,
}

enum class Genre {
    TRAP,
    REGGAETON,
    HIP_HOP,
    DRILL,
    RNB,
    POP,
    ROCK,
    ELECTRONIC,
    LATIN,
    OTHER,
}

/** Tipo de acuerdo que propone quien publica una convocatoria. */
enum class DealType {
    /** Colaboración libre, regalías a acordar. */
    FREE_COLLAB,
    /** Se paga al colaborador (requiere [Budget]). */
    PAID,
    /** Intercambio de servicios. */
    EXCHANGE,
}

enum class CallStatus { OPEN, CLOSED }

enum class ApplicationStatus { PENDING, ACCEPTED, REJECTED }

enum class AudioFormat(val extension: String) {
    MP3("mp3"),
    WAV("wav"),
    AAC("aac"),
    M4A("m4a"),
    OGG("ogg"),
    ;

    companion object {
        fun fromExtension(ext: String): AudioFormat? =
            entries.firstOrNull { it.extension.equals(ext.trim().removePrefix("."), ignoreCase = true) }
    }
}

enum class Plan { FREE, PRO }
