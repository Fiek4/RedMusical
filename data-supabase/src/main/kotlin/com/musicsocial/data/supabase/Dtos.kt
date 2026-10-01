package com.musicsocial.data.supabase

import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat
import com.musicsocial.domain.model.Budget
import com.musicsocial.domain.model.CallStatus
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Conversation
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.ExternalLink
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.LinkPlatform
import com.musicsocial.domain.model.Plan
import com.musicsocial.domain.model.Track
import com.musicsocial.domain.model.UserProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime

/*
 * DTO = cómo se ve cada fila en la base de datos (snake_case, texto, números).
 * Las funciones toModel()/toDto() traducen entre la base y nuestro dominio,
 * así el resto de la app nunca ve detalles de Supabase.
 */

// Postgres devuelve fechas como "2026-10-01T12:00:00.123+00:00".
internal fun String.toInstant(): Instant = OffsetDateTime.parse(this).toInstant()

private fun audioFormat(value: String) = AudioFormat.valueOf(value)

@Serializable
internal data class LinkDto(val platform: String, val url: String)

@Serializable
internal data class ProfileDto(
    val id: String,
    val username: String,
    @SerialName("artist_name") val artistName: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val bio: String = "",
    val roles: List<String>,
    val genres: List<String>,
    val city: String? = null,
    val country: String? = null,
    val links: List<LinkDto> = emptyList(),
    // Solo lectura: el usuario no puede cambiar su plan (ver migración SQL).
    val plan: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

internal fun ProfileDto.toModel() = UserProfile(
    id = id,
    username = username,
    artistName = artistName,
    photoUrl = photoUrl,
    bio = bio,
    roles = roles.map(ArtistRole::valueOf).toSet(),
    genres = genres.map(Genre::valueOf).toSet(),
    city = city,
    country = country,
    links = links.map { ExternalLink(LinkPlatform.valueOf(it.platform), it.url) },
    plan = plan?.let(Plan::valueOf) ?: Plan.FREE,
    createdAt = createdAt?.toInstant() ?: Instant.EPOCH,
)

/** Al guardar no enviamos plan ni created_at: los pone la base. */
internal fun UserProfile.toDto() = ProfileDto(
    id = id,
    username = username,
    artistName = artistName,
    photoUrl = photoUrl,
    bio = bio,
    roles = roles.map { it.name },
    genres = genres.map { it.name },
    city = city,
    country = country,
    links = links.map { LinkDto(it.platform.name, it.url) },
)

@Serializable
internal data class TrackDto(
    val id: String,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    val genre: String,
    val bpm: Int? = null,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("audio_format") val audioFormat: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("created_at") val createdAt: String,
)

internal fun TrackDto.toModel() = Track(
    id = id,
    ownerId = ownerId,
    title = title,
    genre = Genre.valueOf(genre),
    audio = AudioFile(audioUrl, audioFormat(audioFormat), sizeBytes, durationSeconds),
    bpm = bpm,
    createdAt = createdAt.toInstant(),
)

internal fun Track.toDto() = TrackDto(
    id = id,
    ownerId = ownerId,
    title = title,
    genre = genre.name,
    bpm = bpm,
    audioUrl = audio.uri,
    audioFormat = audio.format.name,
    sizeBytes = audio.sizeBytes,
    durationSeconds = audio.durationSeconds,
    createdAt = createdAt.toString(),
)

@Serializable
internal data class CollabCallDto(
    val id: String,
    @SerialName("author_id") val authorId: String,
    val title: String,
    @SerialName("looking_for") val lookingFor: String,
    val genre: String,
    val description: String,
    @SerialName("audio_url") val audioUrl: String,
    @SerialName("audio_format") val audioFormat: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    val bpm: Int? = null,
    @SerialName("musical_key") val musicalKey: String? = null,
    val deadline: String,
    @SerialName("deal_type") val dealType: String,
    @SerialName("budget_min_cents") val budgetMinCents: Long? = null,
    @SerialName("budget_max_cents") val budgetMaxCents: Long? = null,
    val currency: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

internal fun CollabCallDto.toModel() = CollabCall(
    id = id,
    authorId = authorId,
    title = title,
    lookingFor = ArtistRole.valueOf(lookingFor),
    genre = Genre.valueOf(genre),
    description = description,
    referenceAudio = AudioFile(audioUrl, audioFormat(audioFormat), sizeBytes, durationSeconds),
    bpm = bpm,
    musicalKey = musicalKey,
    deadline = deadline.toInstant(),
    dealType = DealType.valueOf(dealType),
    budget = if (budgetMinCents != null && budgetMaxCents != null && currency != null) {
        Budget(budgetMinCents, budgetMaxCents, currency)
    } else {
        null
    },
    status = CallStatus.valueOf(status),
    createdAt = createdAt.toInstant(),
)

internal fun CollabCall.toDto() = CollabCallDto(
    id = id,
    authorId = authorId,
    title = title,
    lookingFor = lookingFor.name,
    genre = genre.name,
    description = description,
    audioUrl = referenceAudio.uri,
    audioFormat = referenceAudio.format.name,
    sizeBytes = referenceAudio.sizeBytes,
    durationSeconds = referenceAudio.durationSeconds,
    bpm = bpm,
    musicalKey = musicalKey,
    deadline = deadline.toString(),
    dealType = dealType.name,
    budgetMinCents = budget?.minCents,
    budgetMaxCents = budget?.maxCents,
    currency = budget?.currency,
    status = status.name,
    createdAt = createdAt.toString(),
)

@Serializable
internal data class ApplicationDto(
    val id: String,
    @SerialName("call_id") val callId: String,
    @SerialName("applicant_id") val applicantId: String,
    @SerialName("demo_url") val demoUrl: String,
    @SerialName("demo_format") val demoFormat: String,
    @SerialName("size_bytes") val sizeBytes: Long,
    @SerialName("duration_seconds") val durationSeconds: Int,
    val message: String = "",
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

internal fun ApplicationDto.toModel() = CollabApplication(
    id = id,
    callId = callId,
    applicantId = applicantId,
    demo = AudioFile(demoUrl, audioFormat(demoFormat), sizeBytes, durationSeconds),
    message = message,
    status = ApplicationStatus.valueOf(status),
    createdAt = createdAt.toInstant(),
)

internal fun CollabApplication.toDto() = ApplicationDto(
    id = id,
    callId = callId,
    applicantId = applicantId,
    demoUrl = demo.uri,
    demoFormat = demo.format.name,
    sizeBytes = demo.sizeBytes,
    durationSeconds = demo.durationSeconds,
    message = message,
    status = status.name,
    createdAt = createdAt.toString(),
)

@Serializable
internal data class ConversationDto(
    val id: String,
    @SerialName("application_id") val applicationId: String,
    @SerialName("participant_ids") val participantIds: List<String>,
    @SerialName("created_at") val createdAt: String,
)

internal fun ConversationDto.toModel() = Conversation(
    id = id,
    applicationId = applicationId,
    participantIds = participantIds.toSet(),
    createdAt = createdAt.toInstant(),
)

internal fun Conversation.toDto() = ConversationDto(
    id = id,
    applicationId = applicationId,
    participantIds = participantIds.toList(),
    createdAt = createdAt.toString(),
)
