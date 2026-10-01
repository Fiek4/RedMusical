package com.musicsocial.domain.validation

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import java.time.Duration
import java.time.Instant

val NOW: Instant = Instant.parse("2026-10-01T12:00:00Z")

fun audio(seconds: Int = 30, bytes: Long = 1_000_000) =
    AudioFile(uri = "file://audio.mp3", format = AudioFormat.MP3, sizeBytes = bytes, durationSeconds = seconds)

fun call(
    authorId: String = "ana",
    deadline: Instant = NOW.plus(Duration.ofDays(15)),
) = CollabCall(
    id = "call1",
    authorId = authorId,
    title = "Busco rapero para drill",
    lookingFor = ArtistRole.RAPPER,
    genre = Genre.DRILL,
    description = "Beat de drill a 140 BPM, busco flow agresivo",
    referenceAudio = audio(120),
    bpm = 140,
    deadline = deadline,
    dealType = DealType.FREE_COLLAB,
    createdAt = NOW,
)
