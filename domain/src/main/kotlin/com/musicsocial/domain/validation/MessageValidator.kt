package com.musicsocial.domain.validation

import com.musicsocial.domain.model.Conversation
import com.musicsocial.domain.model.MessageContent

object MessageValidator {

    fun validate(content: MessageContent, conversation: Conversation, senderId: String): ValidationResult = validate {
        check(Field.GENERAL, if (senderId !in conversation.participantIds) ValidationError.NotAParticipant else null)
        when (content) {
            is MessageContent.Text -> check(
                Field.MESSAGE,
                { Rules.required(content.text) },
                { Rules.maxLength(content.text, Limits.CHAT_MESSAGE_MAX) },
            )
            is MessageContent.Voice -> check(Field.AUDIO, Rules.audio(content.audio, Limits.VOICE_NOTE_MAX_SECONDS))
            is MessageContent.File -> check(Field.AUDIO, Rules.audio(content.audio, Limits.TRACK_MAX_SECONDS))
        }
    }
}
