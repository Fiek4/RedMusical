package com.musicsocial.presentation

import com.musicsocial.data.memory.FakeAudioUploader
import com.musicsocial.data.memory.InMemoryApplicationRepository
import com.musicsocial.data.memory.InMemoryAuthRepository
import com.musicsocial.data.memory.InMemoryChatRepository
import com.musicsocial.data.memory.InMemoryCollabCallRepository
import com.musicsocial.data.memory.InMemoryUserRepository
import com.musicsocial.data.memory.UuidIdGenerator
import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.domain.usecase.ApplyToCallUseCase
import com.musicsocial.domain.usecase.CreateCallUseCase
import com.musicsocial.domain.usecase.RegisterUseCase
import com.musicsocial.domain.usecase.ReviewApplicationUseCase
import com.musicsocial.domain.usecase.SaveProfileUseCase
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

val NOW: Instant = Instant.parse("2026-10-01T12:00:00Z")

fun audio(seconds: Int = 30) = AudioFile("file://local.mp3", AudioFormat.MP3, 1_000_000, seconds)

fun validCallForm() = CollabCallForm(
    title = "Busco rapero para drill",
    lookingFor = ArtistRole.RAPPER,
    genre = Genre.DRILL,
    description = "Beat de drill a 140 BPM, busco flow agresivo",
    bpm = "140",
    referenceAudio = audio(120),
    deadline = NOW.plus(Duration.ofDays(15)),
    dealType = DealType.FREE_COLLAB,
)

/** Arma toda la app con repositorios en memoria y un reloj fijo. */
class TestApp {
    val clock: Clock = Clock.fixed(NOW, ZoneOffset.UTC)
    val ids = UuidIdGenerator()
    val auth = InMemoryAuthRepository(ids)
    val users = InMemoryUserRepository()
    val calls = InMemoryCollabCallRepository()
    val applications = InMemoryApplicationRepository()
    val chats = InMemoryChatRepository()
    val uploader = FakeAudioUploader()

    val register = RegisterUseCase(auth)
    val saveProfile = SaveProfileUseCase(auth, users, clock)
    val createCall = CreateCallUseCase(auth, users, calls, uploader, ids, clock)
    val applyToCall = ApplyToCallUseCase(auth, calls, applications, uploader, ids, clock)
    val review = ReviewApplicationUseCase(auth, calls, applications, chats, ids, clock)

    /** Inicia sesión como un artista con perfil completo. */
    suspend fun signIn(id: String, roles: Set<ArtistRole> = setOf(ArtistRole.PRODUCER), genres: Set<Genre> = setOf(Genre.DRILL)) {
        auth.signInAs(id)
        users.saveProfile(
            UserProfile(
                id = id,
                username = id,
                artistName = id.replaceFirstChar { it.uppercase() },
                roles = roles,
                genres = genres,
                createdAt = NOW,
            ),
        )
    }
}
