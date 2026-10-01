package com.musicsocial.data.supabase

import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Conversation
import com.musicsocial.domain.model.Track
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.domain.repository.ApplicationRepository
import com.musicsocial.domain.repository.AudioUploader
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.CallFilter
import com.musicsocial.domain.repository.ChatRepository
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.repository.TrackRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.usecase.EmailAlreadyRegisteredException
import com.musicsocial.domain.usecase.InvalidCredentialsException
import com.musicsocial.domain.usecase.NotLoggedInException
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.time.Instant
import java.util.UUID

/*
 * Implementaciones reales de los repositorios con Supabase.
 *
 * Sobre los Flow: por ahora no usamos Realtime (eso llega con el chat).
 * Cada Flow consulta al empezar y vuelve a consultar cuando este mismo
 * repositorio escribe algo, o cuando la UI llama a refresh() (pull-to-refresh).
 */

/** Señal para volver a consultar. */
private class RefreshSignal {
    private val signal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    fun <T> flowOf(query: suspend () -> T): Flow<T> = signal.onStart { emit(Unit) }.map { query() }
    fun refresh() {
        signal.tryEmit(Unit)
    }
}

class SupabaseAuthRepository(private val client: SupabaseClient) : AuthRepository {

    override suspend fun register(email: String, password: String): String {
        val user = try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: AuthRestException) {
            if (e.errorCode == AuthErrorCode.UserAlreadyExists || e.errorCode == AuthErrorCode.EmailExists) {
                throw EmailAlreadyRegisteredException()
            }
            throw e
        }
        // Con "Confirm email" activado, Supabase no da error si el email ya existe:
        // devuelve un usuario sin identidades para no revelar qué emails existen.
        if (user != null && user.identities?.isEmpty() == true) throw EmailAlreadyRegisteredException()
        return user?.id ?: currentUserId() ?: throw NotLoggedInException()
    }

    override suspend fun signIn(email: String, password: String): String {
        try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: AuthRestException) {
            if (e.errorCode == AuthErrorCode.InvalidCredentials) throw InvalidCredentialsException()
            throw e
        }
        return currentUserId() ?: throw NotLoggedInException()
    }

    override suspend fun signOut() = client.auth.signOut()

    override fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    override suspend fun restoreSession(): String? {
        client.auth.awaitInitialization()
        return currentUserId()
    }
}

class SupabaseUserRepository(private val client: SupabaseClient) : UserRepository {
    private val refresh = RefreshSignal()

    override suspend fun getProfile(userId: String): UserProfile? =
        client.from(Tables.PROFILES)
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<ProfileDto>()
            ?.toModel()

    override fun observeProfile(userId: String): Flow<UserProfile?> = refresh.flowOf { getProfile(userId) }

    override suspend fun saveProfile(profile: UserProfile) {
        client.from(Tables.PROFILES).upsert(profile.toDto())
        refresh.refresh()
    }

    override suspend fun isUsernameTaken(username: String, exceptUserId: String): Boolean =
        client.from(Tables.PROFILES)
            .select {
                head = true
                count(Count.EXACT)
                filter {
                    eq("username", username)
                    neq("id", exceptUserId)
                }
            }
            .countOrNull()
            .let { (it ?: 0) > 0 }
}

class SupabaseTrackRepository(private val client: SupabaseClient) : TrackRepository {
    private val refresh = RefreshSignal()

    override fun observeTracks(ownerId: String): Flow<List<Track>> = refresh.flowOf {
        client.from(Tables.TRACKS)
            .select {
                filter { eq("owner_id", ownerId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<TrackDto>()
            .map { it.toModel() }
    }

    override suspend fun countTracks(ownerId: String): Int =
        client.from(Tables.TRACKS)
            .select {
                head = true
                count(Count.EXACT)
                filter { eq("owner_id", ownerId) }
            }
            .countOrNull()?.toInt() ?: 0

    override suspend fun addTrack(track: Track) {
        client.from(Tables.TRACKS).insert(track.toDto())
        refresh.refresh()
    }

    fun refresh() = refresh.refresh()
}

class SupabaseCollabCallRepository(private val client: SupabaseClient) : CollabCallRepository {
    private val refresh = RefreshSignal()

    override fun observeOpenCalls(filter: CallFilter, now: Instant): Flow<List<CollabCall>> = refresh.flowOf {
        client.from(Tables.CALLS)
            .select {
                filter {
                    eq("status", "OPEN")
                    gt("deadline", now.toString())
                    if (filter.roles.isNotEmpty()) isIn("looking_for", filter.roles.map { it.name })
                    if (filter.genres.isNotEmpty()) isIn("genre", filter.genres.map { it.name })
                }
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<CollabCallDto>()
            .map { it.toModel() }
    }

    override suspend fun getCall(callId: String): CollabCall? =
        client.from(Tables.CALLS)
            .select { filter { eq("id", callId) } }
            .decodeSingleOrNull<CollabCallDto>()
            ?.toModel()

    override suspend fun countActiveCalls(authorId: String, now: Instant): Int =
        client.from(Tables.CALLS)
            .select {
                head = true
                count(Count.EXACT)
                filter {
                    eq("author_id", authorId)
                    eq("status", "OPEN")
                    gt("deadline", now.toString())
                }
            }
            .countOrNull()?.toInt() ?: 0

    override suspend fun createCall(call: CollabCall) {
        client.from(Tables.CALLS).insert(call.toDto())
        refresh.refresh()
    }

    fun refresh() = refresh.refresh()
}

class SupabaseApplicationRepository(private val client: SupabaseClient) : ApplicationRepository {
    private val refresh = RefreshSignal()

    override suspend fun hasApplied(callId: String, applicantId: String): Boolean =
        client.from(Tables.APPLICATIONS)
            .select {
                head = true
                count(Count.EXACT)
                filter {
                    eq("call_id", callId)
                    eq("applicant_id", applicantId)
                }
            }
            .countOrNull()
            .let { (it ?: 0) > 0 }

    override suspend fun apply(application: CollabApplication) {
        client.from(Tables.APPLICATIONS).insert(application.toDto())
        refresh.refresh()
    }

    override suspend fun getApplication(applicationId: String): CollabApplication? =
        client.from(Tables.APPLICATIONS)
            .select { filter { eq("id", applicationId) } }
            .decodeSingleOrNull<ApplicationDto>()
            ?.toModel()

    override fun observeApplications(callId: String): Flow<List<CollabApplication>> = refresh.flowOf {
        client.from(Tables.APPLICATIONS)
            .select {
                filter { eq("call_id", callId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<ApplicationDto>()
            .map { it.toModel() }
    }

    override suspend fun updateStatus(applicationId: String, status: ApplicationStatus) {
        client.from(Tables.APPLICATIONS).update({ set("status", status.name) }) {
            filter { eq("id", applicationId) }
        }
        refresh.refresh()
    }

    fun refresh() = refresh.refresh()
}

class SupabaseChatRepository(private val client: SupabaseClient) : ChatRepository {

    override suspend fun createConversation(conversation: Conversation) {
        client.from(Tables.CONVERSATIONS).insert(conversation.toDto())
    }

    override suspend fun findByApplication(applicationId: String): Conversation? =
        client.from(Tables.CONVERSATIONS)
            .select { filter { eq("application_id", applicationId) } }
            .decodeSingleOrNull<ConversationDto>()
            ?.toModel()
}

/** Lee los bytes de un audio local. En Android se implementa con ContentResolver. */
fun interface AudioBytesReader {
    suspend fun read(uri: String): ByteArray
}

/**
 * Sube audios al bucket "audio" dentro de la carpeta del usuario
 * (audio/<user_id>/<uuid>.mp3) y devuelve el audio con la URL pública.
 */
class SupabaseAudioUploader(
    private val client: SupabaseClient,
    private val auth: AuthRepository,
    private val reader: AudioBytesReader,
) : AudioUploader {

    override suspend fun upload(audio: AudioFile): AudioFile {
        val userId = auth.currentUserId() ?: throw NotLoggedInException()
        val path = "$userId/${UUID.randomUUID()}.${audio.format.extension}"
        val bucket = client.storage.from(AUDIO_BUCKET)
        bucket.upload(path, reader.read(audio.uri)) {
            upsert = false
            contentType = ContentType.parse(audio.format.mimeType())
        }
        return audio.copy(uri = bucket.publicUrl(path))
    }
}

internal fun com.musicsocial.domain.model.AudioFormat.mimeType(): String = when (this) {
    com.musicsocial.domain.model.AudioFormat.MP3 -> "audio/mpeg"
    com.musicsocial.domain.model.AudioFormat.WAV -> "audio/wav"
    com.musicsocial.domain.model.AudioFormat.AAC -> "audio/aac"
    com.musicsocial.domain.model.AudioFormat.M4A -> "audio/mp4"
    com.musicsocial.domain.model.AudioFormat.OGG -> "audio/ogg"
}
