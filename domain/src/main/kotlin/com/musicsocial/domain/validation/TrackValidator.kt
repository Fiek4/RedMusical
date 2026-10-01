package com.musicsocial.domain.validation

import com.musicsocial.domain.form.TrackForm
import com.musicsocial.domain.model.Plan

object TrackValidator {

    /**
     * @param currentTrackCount tracks que el usuario ya tiene en su perfil.
     */
    fun validate(form: TrackForm, plan: Plan, currentTrackCount: Int): ValidationResult = validate {
        if (plan == Plan.FREE && currentTrackCount >= Limits.FREE_MAX_TRACKS) {
            check(Field.GENERAL, ValidationError.PlanLimitReached(Limits.FREE_MAX_TRACKS))
        }
        check(
            Field.TITLE,
            { Rules.required(form.title) },
            { Rules.maxLength(form.title, Limits.TITLE_MAX) },
        )
        check(Field.GENRE, Rules.notNull(form.genre))
        check(Field.BPM, Rules.bpm(form.bpm))
        check(Field.AUDIO, Rules.audio(form.audio, Limits.TRACK_MAX_SECONDS))
    }
}
