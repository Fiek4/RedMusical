package com.musicsocial.domain.model

import java.time.Instant

/**
 * Colaboración terminada. Solo cuenta como "completada" cuando
 * todos los participantes la confirman.
 */
data class Collaboration(
    val id: String,
    val callId: String,
    val participantIds: Set<String>,
    val confirmedBy: Set<String> = emptySet(),
    val trackTitle: String? = null,
    val completedAt: Instant? = null,
) {
    val isCompleted: Boolean
        get() = participantIds.isNotEmpty() && confirmedBy.containsAll(participantIds)
}
