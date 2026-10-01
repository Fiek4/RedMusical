package com.musicsocial.domain.usecase

import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.form.ProfileForm
import com.musicsocial.domain.model.Budget
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.UserProfile
import java.math.BigDecimal
import java.time.Instant

/*
 * Conversión de formulario a modelo. Solo se llama DESPUÉS de validar,
 * por eso usamos `!!` y `toInt()` sin miedo: el validador ya lo garantizó.
 */

internal fun CollabCallForm.toModel(id: String, authorId: String, now: Instant) = CollabCall(
    id = id,
    authorId = authorId,
    title = title.trim(),
    lookingFor = lookingFor!!,
    genre = genre!!,
    description = description.trim(),
    referenceAudio = referenceAudio!!,
    bpm = bpm.trim().toIntOrNull(),
    musicalKey = musicalKey.trim().ifBlank { null },
    deadline = deadline!!,
    dealType = dealType!!,
    budget = if (dealType == DealType.PAID) {
        Budget(budgetMin.toCents(), budgetMax.toCents(), currency.uppercase())
    } else {
        null
    },
    createdAt = now,
)

private fun String.toCents(): Long = BigDecimal(trim()).movePointRight(2).toLong()

internal fun ProfileForm.applyTo(profile: UserProfile) = profile.copy(
    username = username.trim().lowercase(),
    artistName = artistName.trim(),
    bio = bio.trim(),
    roles = roles,
    genres = genres,
    city = city.trim().ifBlank { null },
    links = links,
)

fun UserProfile.toForm() = ProfileForm(
    username = username,
    artistName = artistName,
    bio = bio,
    roles = roles,
    genres = genres,
    city = city.orEmpty(),
    links = links,
)
