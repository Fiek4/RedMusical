package com.musicsocial.domain.model

import java.time.Instant

/** Chat 1 a 1 que se abre cuando se acepta una postulación. */
data class Conversation(
    val id: String,
    val applicationId: String,
    val participantIds: Set<String>,
    val createdAt: Instant,
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: MessageContent,
    val sentAt: Instant,
)

sealed interface MessageContent {
    data class Text(val text: String) : MessageContent
    data class Voice(val audio: AudioFile) : MessageContent
    data class File(val audio: AudioFile, val fileName: String) : MessageContent
}
