package com.musicsocial.app

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat

/**
 * Lee formato, peso y duración del audio que el usuario eligió (content://...).
 * Devuelve null si el formato no es uno de los que aceptamos.
 */
fun Context.readAudioFile(uri: Uri): AudioFile? {
    var name = ""
    var size = 0L
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(0).orEmpty()
                size = cursor.getLong(1)
            }
        }

    val format = formatFromMime(contentResolver.getType(uri))
        ?: AudioFormat.fromExtension(name.substringAfterLast('.', ""))
        ?: return null

    val durationMs = runCatching {
        MediaMetadataRetriever().run {
            try {
                setDataSource(this@readAudioFile, uri)
                extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            } finally {
                release()
            }
        }
    }.getOrNull() ?: 0L

    return AudioFile(
        uri = uri.toString(),
        format = format,
        sizeBytes = size,
        // Redondeamos hacia arriba para que un audio de 0,4 s no cuente como vacío.
        durationSeconds = ((durationMs + 999) / 1000).toInt(),
    )
}

/** Nombre del archivo para mostrarlo en pantalla. */
fun Context.audioDisplayName(uri: String): String =
    contentResolver.query(Uri.parse(uri), arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { if (it.moveToFirst()) it.getString(0) else null }
        ?: uri.substringAfterLast('/')

private fun formatFromMime(mime: String?): AudioFormat? = when (mime?.lowercase()) {
    "audio/mpeg", "audio/mp3" -> AudioFormat.MP3
    "audio/wav", "audio/x-wav", "audio/wave" -> AudioFormat.WAV
    "audio/aac", "audio/aacp" -> AudioFormat.AAC
    "audio/mp4", "audio/m4a", "audio/x-m4a" -> AudioFormat.M4A
    "audio/ogg", "application/ogg" -> AudioFormat.OGG
    else -> null
}
