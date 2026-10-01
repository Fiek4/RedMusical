package com.musicsocial.domain.model

import java.time.Instant

/** Postulación de un artista a una [CollabCall], con una demo corta. */
data class CollabApplication(
    val id: String,
    val callId: String,
    val applicantId: String,
    val demo: AudioFile,
    val message: String = "",
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val createdAt: Instant,
)
