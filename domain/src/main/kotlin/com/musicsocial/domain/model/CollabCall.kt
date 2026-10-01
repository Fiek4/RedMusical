package com.musicsocial.domain.model

import java.time.Instant

/** Convocatoria de colaboración: "Busco vocalista R&B para este beat". */
data class CollabCall(
    val id: String,
    val authorId: String,
    val title: String,
    val lookingFor: ArtistRole,
    val genre: Genre,
    val description: String,
    val referenceAudio: AudioFile,
    val bpm: Int? = null,
    val musicalKey: String? = null,
    val deadline: Instant,
    val dealType: DealType,
    val budget: Budget? = null,
    val status: CallStatus = CallStatus.OPEN,
    val createdAt: Instant,
) {
    fun isAcceptingApplications(now: Instant): Boolean =
        status == CallStatus.OPEN && now.isBefore(deadline)
}

/** Rango de pago, solo para [DealType.PAID]. Montos en centavos para evitar errores de redondeo. */
data class Budget(
    val minCents: Long,
    val maxCents: Long,
    val currency: String,
)
