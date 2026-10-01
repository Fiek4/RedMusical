package com.musicsocial.domain.validation

import com.musicsocial.domain.form.ApplicationForm
import com.musicsocial.domain.model.CollabCall
import java.time.Instant

object ApplicationValidator {

    /**
     * @param alreadyApplied si el usuario ya se postuló a esta convocatoria.
     */
    fun validate(
        form: ApplicationForm,
        call: CollabCall,
        applicantId: String,
        alreadyApplied: Boolean,
        now: Instant,
    ): ValidationResult = validate {
        check(
            Field.GENERAL,
            {
                when {
                    call.authorId == applicantId -> ValidationError.CannotApplyToOwnCall
                    !call.isAcceptingApplications(now) -> ValidationError.CallClosed
                    alreadyApplied -> ValidationError.AlreadyApplied
                    else -> null
                }
            },
        )
        checkFields(form)
    }

    /** Solo los campos del formulario, sin reglas de negocio. */
    fun validateFields(form: ApplicationForm): ValidationResult = validate { checkFields(form) }

    private fun ValidationBuilder.checkFields(form: ApplicationForm) {
        check(Field.AUDIO, Rules.audio(form.demo, Limits.DEMO_MAX_SECONDS))
        check(Field.MESSAGE, Rules.maxLength(form.message, Limits.APPLICATION_MESSAGE_MAX))
    }
}
