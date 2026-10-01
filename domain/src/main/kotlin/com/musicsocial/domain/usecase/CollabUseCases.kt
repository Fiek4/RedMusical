package com.musicsocial.domain.usecase

import com.musicsocial.domain.form.ApplicationForm
import com.musicsocial.domain.form.CollabCallForm
import com.musicsocial.domain.model.ApplicationStatus
import com.musicsocial.domain.model.CollabApplication
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Conversation
import com.musicsocial.domain.repository.ApplicationRepository
import com.musicsocial.domain.repository.AudioUploader
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.ChatRepository
import com.musicsocial.domain.repository.CollabCallRepository
import com.musicsocial.domain.repository.IdGenerator
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.validation.ApplicationValidator
import com.musicsocial.domain.validation.CollabCallValidator
import java.time.Clock

class CreateCallUseCase(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val calls: CollabCallRepository,
    private val uploader: AudioUploader,
    private val ids: IdGenerator,
    private val clock: Clock,
) {

    suspend operator fun invoke(form: CollabCallForm): UseCaseResult<CollabCall> = runCatchingUseCase {
        val userId = auth.currentUserId() ?: throw NotLoggedInException()
        val user = users.getProfile(userId) ?: throw NotFoundException("Perfil")
        val now = clock.instant()

        val validation = CollabCallValidator.validate(
            form = form,
            plan = user.plan,
            activeCallCount = calls.countActiveCalls(userId, now),
            now = now,
        )
        if (!validation.isValid) return@runCatchingUseCase UseCaseResult.Invalid(validation)

        // Subimos el audio solo cuando todo lo demás es válido.
        val uploaded = uploader.upload(form.referenceAudio!!)
        val call = form.copy(referenceAudio = uploaded).toModel(ids.newId(), userId, now)
        calls.createCall(call)
        UseCaseResult.Success(call)
    }
}

class ApplyToCallUseCase(
    private val auth: AuthRepository,
    private val calls: CollabCallRepository,
    private val applications: ApplicationRepository,
    private val uploader: AudioUploader,
    private val ids: IdGenerator,
    private val clock: Clock,
) {

    suspend operator fun invoke(callId: String, form: ApplicationForm): UseCaseResult<CollabApplication> =
        runCatchingUseCase {
            val userId = auth.currentUserId() ?: throw NotLoggedInException()
            val call = calls.getCall(callId) ?: throw NotFoundException("Convocatoria")
            val now = clock.instant()

            val validation = ApplicationValidator.validate(
                form = form,
                call = call,
                applicantId = userId,
                alreadyApplied = applications.hasApplied(callId, userId),
                now = now,
            )
            if (!validation.isValid) return@runCatchingUseCase UseCaseResult.Invalid(validation)

            val application = CollabApplication(
                id = ids.newId(),
                callId = callId,
                applicantId = userId,
                demo = uploader.upload(form.demo!!),
                message = form.message.trim(),
                createdAt = now,
            )
            applications.apply(application)
            UseCaseResult.Success(application)
        }
}

/** El autor de la convocatoria acepta (abre chat) o descarta una postulación. */
class ReviewApplicationUseCase(
    private val auth: AuthRepository,
    private val calls: CollabCallRepository,
    private val applications: ApplicationRepository,
    private val chats: ChatRepository,
    private val ids: IdGenerator,
    private val clock: Clock,
) {

    suspend fun accept(applicationId: String): UseCaseResult<Conversation> = runCatchingUseCase {
        val application = loadOwnPendingApplication(applicationId)
        applications.updateStatus(applicationId, ApplicationStatus.ACCEPTED)

        val conversation = chats.findByApplication(applicationId) ?: Conversation(
            id = ids.newId(),
            applicationId = applicationId,
            participantIds = setOf(auth.currentUserId()!!, application.applicantId),
            createdAt = clock.instant(),
        ).also { chats.createConversation(it) }

        UseCaseResult.Success(conversation)
    }

    suspend fun reject(applicationId: String): UseCaseResult<Unit> = runCatchingUseCase {
        loadOwnPendingApplication(applicationId)
        applications.updateStatus(applicationId, ApplicationStatus.REJECTED)
        UseCaseResult.Success(Unit)
    }

    private suspend fun loadOwnPendingApplication(applicationId: String): CollabApplication {
        val userId = auth.currentUserId() ?: throw NotLoggedInException()
        val application = applications.getApplication(applicationId) ?: throw NotFoundException("Postulación")
        val call = calls.getCall(application.callId) ?: throw NotFoundException("Convocatoria")
        if (call.authorId != userId) throw NotAllowedException("Solo el autor puede revisar postulaciones")
        if (application.status != ApplicationStatus.PENDING) throw NotAllowedException("La postulación ya fue revisada")
        return application
    }
}
