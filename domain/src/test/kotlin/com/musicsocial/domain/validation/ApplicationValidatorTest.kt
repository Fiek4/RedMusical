package com.musicsocial.domain.validation

import com.musicsocial.domain.form.ApplicationForm
import com.musicsocial.domain.model.CallStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationValidatorTest {

    private val form = ApplicationForm(demo = audio(45), message = "Me encanta el beat")

    @Test
    fun `postulacion valida`() {
        assertTrue(ApplicationValidator.validate(form, call(), "luis", alreadyApplied = false, now = NOW).isValid)
    }

    @Test
    fun `no puedo postularme a mi propia convocatoria`() {
        val result = ApplicationValidator.validate(form, call(authorId = "luis"), "luis", false, NOW)
        assertEquals(ValidationError.CannotApplyToOwnCall, result.errorFor(Field.GENERAL))
    }

    @Test
    fun `convocatoria cerrada o vencida`() {
        val closed = call().copy(status = CallStatus.CLOSED)
        assertEquals(ValidationError.CallClosed, ApplicationValidator.validate(form, closed, "luis", false, NOW).errorFor(Field.GENERAL))

        val expired = call(deadline = NOW.minusSeconds(60))
        assertEquals(ValidationError.CallClosed, ApplicationValidator.validate(form, expired, "luis", false, NOW).errorFor(Field.GENERAL))
    }

    @Test
    fun `solo una postulacion por convocatoria`() {
        val result = ApplicationValidator.validate(form, call(), "luis", alreadyApplied = true, now = NOW)
        assertEquals(ValidationError.AlreadyApplied, result.errorFor(Field.GENERAL))
    }

    @Test
    fun `demo maximo 60 segundos`() {
        val result = ApplicationValidator.validate(form.copy(demo = audio(90)), call(), "luis", false, NOW)
        assertEquals(ValidationError.AudioTooLong(Limits.DEMO_MAX_SECONDS), result.errorFor(Field.AUDIO))
    }
}
