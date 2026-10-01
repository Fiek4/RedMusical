package com.musicsocial.domain.validation

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RulesTest {

    @Test
    fun `email valido e invalido`() {
        assertNull(Rules.email("fer@musica.com"))
        assertEquals(ValidationError.InvalidFormat, Rules.email("fer@musica"))
        assertEquals(ValidationError.InvalidFormat, Rules.email("fer musica.com"))
    }

    @Test
    fun `password necesita largo, letra y numero`() {
        assertEquals(ValidationError.TooShort(8), Rules.password("abc1"))
        assertEquals(ValidationError.PasswordTooWeak, Rules.password("abcdefgh"))
        assertEquals(ValidationError.PasswordTooWeak, Rules.password("12345678"))
        assertNull(Rules.password("beats2026"))
    }

    @Test
    fun `bpm es opcional pero debe estar en rango`() {
        assertNull(Rules.bpm(""))
        assertNull(Rules.bpm("140"))
        assertEquals(ValidationError.InvalidFormat, Rules.bpm("rapido"))
        assertEquals(ValidationError.OutOfRange(40, 250), Rules.bpm("300"))
    }

    @Test
    fun `tonalidad musical`() {
        assertNull(Rules.musicalKey(""))
        assertNull(Rules.musicalKey("C#m"))
        assertNull(Rules.musicalKey("Bb major"))
        assertEquals(ValidationError.InvalidFormat, Rules.musicalKey("H"))
    }

    @Test
    fun `audio vacio, pesado o largo`() {
        assertEquals(ValidationError.Required, Rules.audio(null, 60))
        assertEquals(ValidationError.EmptyAudio, Rules.audio(audio(seconds = 0), 60))
        assertEquals(ValidationError.FileTooLarge(Limits.MAX_AUDIO_BYTES), Rules.audio(audio(bytes = 30_000_000), 60))
        assertEquals(ValidationError.AudioTooLong(60), Rules.audio(audio(seconds = 61), 60))
        assertNull(Rules.audio(audio(seconds = 60), 60))
    }

    @Test
    fun `fecha limite debe ser futura y no muy lejana`() {
        assertEquals(ValidationError.DateInPast, Rules.futureDate(NOW.minusSeconds(1), NOW, 90))
        assertEquals(ValidationError.DateTooFar(90), Rules.futureDate(NOW.plus(Duration.ofDays(91)), NOW, 90))
        assertNull(Rules.futureDate(NOW.plus(Duration.ofDays(10)), NOW, 90))
    }
}
