package com.musicsocial.domain.model

/** Metadatos de un archivo de audio subido o por subir. */
data class AudioFile(
    val uri: String,
    val format: AudioFormat,
    val sizeBytes: Long,
    val durationSeconds: Int,
)
