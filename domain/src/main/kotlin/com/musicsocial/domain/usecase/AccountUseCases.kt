package com.musicsocial.domain.usecase

import com.musicsocial.domain.form.ProfileForm
import com.musicsocial.domain.form.RegisterForm
import com.musicsocial.domain.model.UserProfile
import com.musicsocial.domain.repository.AuthRepository
import com.musicsocial.domain.repository.UserRepository
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.UserValidator
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.domain.validation.ValidationResult
import java.time.Clock

class RegisterUseCase(private val auth: AuthRepository) {

    suspend operator fun invoke(form: RegisterForm): UseCaseResult<String> = runCatchingUseCase {
        val validation = UserValidator.validateRegister(form)
        if (!validation.isValid) return@runCatchingUseCase UseCaseResult.Invalid(validation)

        val email = form.email.trim().lowercase()
        if (auth.isEmailRegistered(email)) {
            return@runCatchingUseCase UseCaseResult.Invalid(
                ValidationResult(mapOf(Field.EMAIL to ValidationError.AlreadyTaken)),
            )
        }
        UseCaseResult.Success(auth.register(email, form.password))
    }
}

/** Crea el perfil en el onboarding o lo actualiza después. */
class SaveProfileUseCase(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(form: ProfileForm): UseCaseResult<UserProfile> = runCatchingUseCase {
        val userId = auth.currentUserId() ?: throw NotLoggedInException()

        val validation = UserValidator.validateProfile(form)
        if (!validation.isValid) return@runCatchingUseCase UseCaseResult.Invalid(validation)

        if (users.isUsernameTaken(form.username.trim().lowercase(), exceptUserId = userId)) {
            return@runCatchingUseCase UseCaseResult.Invalid(
                ValidationResult(mapOf(Field.USERNAME to ValidationError.AlreadyTaken)),
            )
        }

        val base = users.getProfile(userId) ?: UserProfile(
            id = userId,
            email = auth.currentUserEmail() ?: throw NotLoggedInException(),
            username = "",
            artistName = "",
            roles = emptySet(),
            genres = emptySet(),
            createdAt = clock.instant(),
        )
        val profile = form.applyTo(base)
        users.saveProfile(profile)
        UseCaseResult.Success(profile)
    }
}
