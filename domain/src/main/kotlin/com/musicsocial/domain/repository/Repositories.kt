package com.musicsocial.domain.repository

import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Conversation
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.Track
import com.musicsocial.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/*
 * Contratos de datos. El dominio y los ViewModels solo conocen estas interfaces;
 * la implementación real (Supabase, Firebase, Room...) vive en el módulo data.
 * Las funciones `suspend` hacen una operación; las que devuelven Flow se
 * actualizan solas cuando cambian los datos.
 */

interface AuthRepository {
    /**
     * Crea la cuenta, inicia sesión y devuelve el id del usuario.
     * @throws com.musicsocial.domain.usecase.EmailAlreadyRegisteredException si el email ya existe.
     */
    suspend fun register(email: String, password: String): String

    /**
     * Inicia sesión y devuelve el id del usuario.
     * @throws com.musicsocial.domain.usecase.InvalidCredentialsException si el email o la contraseña no coinciden.
     */
    suspend fun signIn(email: String, password: String): String
    suspend fun signOut()
    fun currentUserId(): String?

    /** Espera a que se cargue la sesión guardada en el teléfono y devuelve el usuario, si hay. */
    suspend fun restoreSession(): String?
}

interface UserRepository {
    suspend fun getProfile(userId: String): UserProfile?
    fun observeProfile(userId: String): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun isUsernameTaken(username: String, exceptUserId: String): Boolean
}

interface TrackRepository {
    fun observeTracks(ownerId: String): Flow<List<Track>>
    suspend fun countTracks(ownerId: String): Int
    suspend fun addTrack(track: Track)
}

data class CallFilter(
    val roles: Set<ArtistRole> = emptySet(),
    val genres: Set<Genre> = emptySet(),
)

interface CollabCallRepository {
    /** Convocatorias abiertas y no vencidas que cumplen el filtro, más recientes primero. */
    fun observeOpenCalls(filter: CallFilter, now: Instant): Flow<List<CollabCall>>
    suspend fun getCall(callId: String): CollabCall?
    suspend fun countActiveCalls(authorId: String, now: Instant): Int
    suspend fun createCall(call: CollabCall)
}

interface ApplicationRepository {
    suspend fun hasApplied(callId: String, applicantId: String): Boolean
    suspend fun apply(application: CollabApplication)
    suspend fun getApplication(applicationId: String): CollabApplication?
    fun observeApplications(callId: String): Flow<List<CollabApplication>>
    suspend fun updateStatus(applicationId: String, status: ApplicationStatus)
}

interface ChatRepository {
    suspend fun createConversation(conversation: Conversation)
    suspend fun findByApplication(applicationId: String): Conversation?
}

/** Sube un audio local y devuelve el mismo audio con la URL remota. */
interface AudioUploader {
    suspend fun upload(audio: AudioFile): AudioFile
}

fun interface IdGenerator {
    fun newId(): String
}
