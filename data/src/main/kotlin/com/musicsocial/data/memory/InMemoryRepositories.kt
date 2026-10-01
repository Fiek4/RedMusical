package com.musicsocial.data.memory

import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.CallStatus
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
import com.musicsocial.domain.repository.IdGenerator
import com.musicsocial.domain.repository.TrackRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.usecase.EmailAlreadyRegisteredException
import com.musicsocial.domain.usecase.InvalidCredentialsException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID

/*
 * Implementaciones en memoria: sirven para desarrollar la app y probar los
 * ViewModels antes de tener backend. Cuando elijamos Supabase o Firebase,
 * se crean otras clases que implementen las mismas interfaces.
 */

class UuidIdGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}

class InMemoryAuthRepository(private val ids: IdGenerator = UuidIdGenerator()) : AuthRepository {
    private data class Account(val userId: String, val password: String)

    private val accounts = MutableStateFlow<Map<String, Account>>(emptyMap()) // email -> cuenta
    private var currentUserId: String? = null

    override suspend fun register(email: String, password: String): String {
        if (email in accounts.value) throw EmailAlreadyRegisteredException()
        val userId = ids.newId()
        accounts.update { it + (email to Account(userId, password)) }
        currentUserId = userId
        return userId
    }

    override suspend fun signIn(email: String, password: String): String {
        val account = accounts.value[email]?.takeIf { it.password == password } ?: throw InvalidCredentialsException()
        currentUserId = account.userId
        return account.userId
    }

    override suspend fun signOut() {
        currentUserId = null
    }

    override fun currentUserId(): String? = currentUserId

    /** Útil en pruebas para simular que otro usuario inicia sesión. */
    fun signInAs(userId: String) {
        currentUserId = userId
    }
}

class InMemoryUserRepository : UserRepository {
    private val profiles = MutableStateFlow<Map<String, UserProfile>>(emptyMap())

    override suspend fun getProfile(userId: String) = profiles.value[userId]

    override fun observeProfile(userId: String): Flow<UserProfile?> = profiles.map { it[userId] }

    override suspend fun saveProfile(profile: UserProfile) = profiles.update { it + (profile.id to profile) }

    override suspend fun isUsernameTaken(username: String, exceptUserId: String) =
        profiles.value.values.any { it.username == username && it.id != exceptUserId }
}

class InMemoryTrackRepository : TrackRepository {
    private val tracks = MutableStateFlow<List<Track>>(emptyList())

    override fun observeTracks(ownerId: String) = tracks.map { list -> list.filter { it.ownerId == ownerId } }

    override suspend fun countTracks(ownerId: String) = tracks.value.count { it.ownerId == ownerId }

    override suspend fun addTrack(track: Track) = tracks.update { it + track }
}

class InMemoryCollabCallRepository : CollabCallRepository {
    private val calls = MutableStateFlow<List<CollabCall>>(emptyList())

    override fun observeOpenCalls(filter: CallFilter, now: Instant): Flow<List<CollabCall>> = calls.map { list ->
        list.filter { call ->
            call.isAcceptingApplications(now) &&
                (filter.roles.isEmpty() || call.lookingFor in filter.roles) &&
                (filter.genres.isEmpty() || call.genre in filter.genres)
        }.sortedByDescending { it.createdAt }
    }

    override suspend fun getCall(callId: String) = calls.value.firstOrNull { it.id == callId }

    override suspend fun countActiveCalls(authorId: String, now: Instant) =
        calls.value.count { it.authorId == authorId && it.status == CallStatus.OPEN && it.deadline.isAfter(now) }

    override suspend fun createCall(call: CollabCall) = calls.update { it + call }
}

class InMemoryApplicationRepository : ApplicationRepository {
    private val applications = MutableStateFlow<List<CollabApplication>>(emptyList())

    override suspend fun hasApplied(callId: String, applicantId: String) =
        applications.value.any { it.callId == callId && it.applicantId == applicantId }

    override suspend fun apply(application: CollabApplication) = applications.update { it + application }

    override suspend fun getApplication(applicationId: String) =
        applications.value.firstOrNull { it.id == applicationId }

    override fun observeApplications(callId: String) =
        applications.map { list -> list.filter { it.callId == callId }.sortedBy { it.createdAt } }

    override suspend fun updateStatus(applicationId: String, status: ApplicationStatus) = applications.update { list ->
        list.map { if (it.id == applicationId) it.copy(status = status) else it }
    }
}

class InMemoryChatRepository : ChatRepository {
    private val conversations = MutableStateFlow<List<Conversation>>(emptyList())

    override suspend fun createConversation(conversation: Conversation) = conversations.update { it + conversation }

    override suspend fun findByApplication(applicationId: String) =
        conversations.value.firstOrNull { it.applicationId == applicationId }
}

/** Simula la subida: devuelve el audio con una URL falsa. */
class FakeAudioUploader : AudioUploader {
    override suspend fun upload(audio: AudioFile) =
        audio.copy(uri = "https://storage.musicsocial.dev/audio/${UUID.randomUUID()}.${audio.format.extension}")
}
