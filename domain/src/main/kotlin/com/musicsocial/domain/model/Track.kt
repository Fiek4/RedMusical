package com.musicsocial.domain.model

import java.time.Instant

/** Track destacado en el perfil de un artista. */
data class Track(
    val id: String,
    val ownerId: String,
    val title: String,
    val genre: Genre,
    val audio: AudioFile,
    val bpm: Int? = null,
    val createdAt: Instant,
)
