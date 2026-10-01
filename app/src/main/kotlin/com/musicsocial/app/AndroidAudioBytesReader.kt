package com.musicsocial.app

import android.content.Context
import android.net.Uri
import com.musicsocial.data.supabase.AudioBytesReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lee el audio que el usuario eligió en el teléfono (content://...). */
class AndroidAudioBytesReader(private val context: Context) : AudioBytesReader {
    override suspend fun read(uri: String): ByteArray = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(Uri.parse(uri))
            ?.use { it.readBytes() }
            ?: error("No se pudo abrir el audio: $uri")
    }
}
