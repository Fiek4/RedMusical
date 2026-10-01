package com.musicsocial.domain.validation

import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Plan
import java.time.Instant

object CollabCallValidator {

    /**
     * @param activeCallCount convocatorias abiertas que el usuario ya tiene.
     * @param now hora actual; se recibe como parámetro para poder probarlo.
     */
    fun validate(
        form: CollabCallForm,
        plan: Plan,
        activeCallCount: Int,
        now: Instant,
    ): ValidationResult = validate {
        if (plan == Plan.FREE && activeCallCount >= Limits.FREE_MAX_ACTIVE_CALLS) {
            check(Field.GENERAL, ValidationError.PlanLimitReached(Limits.FREE_MAX_ACTIVE_CALLS))
        }
        checkFields(form, now)
    }

    /** Solo los campos del formulario, sin reglas del plan. Útil para validar mientras se escribe. */
    fun validateFields(form: CollabCallForm, now: Instant): ValidationResult = validate { checkFields(form, now) }

    private fun ValidationBuilder.checkFields(form: CollabCallForm, now: Instant) {
        check(
            Field.TITLE,
            { Rules.required(form.title) },
            { Rules.minLength(form.title, Limits.TITLE_MIN) },
            { Rules.maxLength(form.title, Limits.TITLE_MAX) },
        )
        check(Field.LOOKING_FOR, Rules.notNull(form.lookingFor))
        check(Field.GENRE, Rules.notNull(form.genre))
        check(
            Field.DESCRIPTION,
            { Rules.required(form.description) },
            { Rules.minLength(form.description, Limits.DESCRIPTION_MIN) },
            { Rules.maxLength(form.description, Limits.DESCRIPTION_MAX) },
        )
        check(Field.BPM, Rules.bpm(form.bpm))
        check(Field.MUSICAL_KEY, Rules.musicalKey(form.musicalKey))
        check(Field.AUDIO, Rules.audio(form.referenceAudio, Limits.CALL_AUDIO_MAX_SECONDS))
        check(Field.DEADLINE, Rules.futureDate(form.deadline, now, Limits.MAX_DEADLINE_DAYS))
        check(Field.DEAL_TYPE, Rules.notNull(form.dealType))
        if (form.dealType == DealType.PAID) {
            check(Field.BUDGET, validateBudget(form))
        }
    }

    private fun validateBudget(form: CollabCallForm): ValidationError? {
        if (form.budgetMin.isBlank() || form.budgetMax.isBlank()) return ValidationError.Required
        val min = form.budgetMin.trim().toBigDecimalOrNull()
        val max = form.budgetMax.trim().toBigDecimalOrNull()
        if (min == null || max == null) return ValidationError.InvalidFormat
        if (min.signum() <= 0 || max < min) return ValidationError.InvalidBudgetRange
        if (form.currency.length != 3) return ValidationError.InvalidFormat
        return null
    }
}
