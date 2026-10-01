package com.musicsocial.domain.validation

import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.Plan
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CollabCallValidatorTest {

    private val validForm = CollabCallForm(
        title = "Busco vocalista R&B",
        lookingFor = ArtistRole.SINGER,
        genre = Genre.RNB,
        description = "Beat suave a 90 BPM, busco voz melódica",
        bpm = "90",
        musicalKey = "Am",
        referenceAudio = audio(180),
        deadline = NOW.plus(Duration.ofDays(15)),
        dealType = DealType.FREE_COLLAB,
    )

    @Test
    fun `convocatoria valida`() {
        assertTrue(CollabCallValidator.validate(validForm, Plan.FREE, activeCallCount = 0, now = NOW).isValid)
    }

    @Test
    fun `plan gratis tiene limite de convocatorias activas`() {
        val result = CollabCallValidator.validate(validForm, Plan.FREE, activeCallCount = 2, now = NOW)
        assertEquals(ValidationError.PlanLimitReached(Limits.FREE_MAX_ACTIVE_CALLS), result.errorFor(Field.GENERAL))
        assertTrue(CollabCallValidator.validate(validForm, Plan.PRO, activeCallCount = 2, now = NOW).isValid)
    }

    @Test
    fun `pago requiere presupuesto valido`() {
        val paid = validForm.copy(dealType = DealType.PAID)
        assertEquals(ValidationError.Required, CollabCallValidator.validate(paid, Plan.FREE, 0, NOW).errorFor(Field.BUDGET))

        val inverted = paid.copy(budgetMin = "200", budgetMax = "100")
        assertEquals(ValidationError.InvalidBudgetRange, CollabCallValidator.validate(inverted, Plan.FREE, 0, NOW).errorFor(Field.BUDGET))

        val ok = paid.copy(budgetMin = "50", budgetMax = "150.50")
        assertNull(CollabCallValidator.validate(ok, Plan.FREE, 0, NOW).errorFor(Field.BUDGET))
    }

    @Test
    fun `formulario vacio`() {
        val result = CollabCallValidator.validate(CollabCallForm(), Plan.FREE, 0, NOW)
        listOf(Field.TITLE, Field.LOOKING_FOR, Field.GENRE, Field.DESCRIPTION, Field.AUDIO, Field.DEADLINE, Field.DEAL_TYPE)
            .forEach { assertEquals(ValidationError.Required, result.errorFor(it), "campo $it") }
    }
}
