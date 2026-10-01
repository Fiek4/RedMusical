package com.musicsocial.data.supabase

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.AudioFormat
import com.musicsocial.domain.model.Budget
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.ExternalLink
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.LinkPlatform
import com.musicsocial.domain.model.Plan
import com.musicsocial.domain.model.UserProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class DtoMappingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun `lee fechas de Postgres con zona horaria`() {
        assertEquals(Instant.parse("2026-10-01T12:00:00.123456Z"), "2026-10-01T12:00:00.123456+00:00".toInstant())
        assertEquals(Instant.parse("2026-10-01T15:00:00Z"), "2026-10-01T12:00:00-03:00".toInstant())
    }

    @Test
    fun `convocatoria ida y vuelta`() {
        val call = CollabCall(
            id = "c1",
            authorId = "ana",
            title = "Busco vocalista",
            lookingFor = ArtistRole.SINGER,
            genre = Genre.RNB,
            description = "Beat suave a 90 BPM",
            referenceAudio = AudioFile("https://x/a.mp3", AudioFormat.MP3, 1000, 120),
            bpm = 90,
            deadline = Instant.parse("2026-10-15T00:00:00Z"),
            dealType = DealType.PAID,
            budget = Budget(5000, 15000, "USD"),
            createdAt = Instant.parse("2026-10-01T00:00:00Z"),
        )
        val decoded = json.decodeFromString<CollabCallDto>(json.encodeToString(call.toDto())).toModel()
        assertEquals(call, decoded)
    }

    @Test
    fun `al guardar perfil no se envia el plan`() {
        val profile = UserProfile(
            id = "u1",
            username = "fer.beats",
            artistName = "Fer Beats",
            roles = setOf(ArtistRole.PRODUCER),
            genres = setOf(Genre.TRAP),
            links = listOf(ExternalLink(LinkPlatform.INSTAGRAM, "https://instagram.com/fer")),
            plan = Plan.PRO,
            createdAt = Instant.EPOCH,
        )
        val body = json.encodeToString(profile.toDto())
        assertFalse("plan" in body, body)
        assertFalse("created_at" in body, body)
        assert("\"artist_name\":\"Fer Beats\"" in body) { body }
    }

    @Test
    fun `perfil desde la base`() {
        val row = """
            {"id":"u1","username":"fer","artist_name":"Fer","bio":"","roles":["PRODUCER"],"genres":["TRAP"],
             "links":[],"plan":"PRO","created_at":"2026-10-01T12:00:00+00:00","extra":"ignorado"}
        """.trimIndent()
        val profile = json.decodeFromString<ProfileDto>(row).toModel()
        assertEquals(Plan.PRO, profile.plan)
        assertEquals(setOf(ArtistRole.PRODUCER), profile.roles)
    }
}
