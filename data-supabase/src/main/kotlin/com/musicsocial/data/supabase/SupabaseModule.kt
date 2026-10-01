package com.musicsocial.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Crea el cliente de Supabase. En Android, `url` y `publishableKey` se leen de
 * BuildConfig (que a su vez los toma de local.properties, fuera del repositorio).
 */
fun createMusicSocialClient(url: String, publishableKey: String): SupabaseClient =
    createSupabaseClient(supabaseUrl = url, supabaseKey = publishableKey) {
        // explicitNulls = false: no enviamos campos nulos (por ejemplo `plan`),
        // así la base usa sus valores por defecto y respeta los permisos por columna.
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

internal object Tables {
    const val PROFILES = "profiles"
    const val TRACKS = "tracks"
    const val CALLS = "collab_calls"
    const val APPLICATIONS = "applications"
    const val CONVERSATIONS = "conversations"
}

internal const val AUDIO_BUCKET = "audio"
