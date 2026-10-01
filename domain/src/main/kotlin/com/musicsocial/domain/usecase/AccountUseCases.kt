package com.musicsocial.domain.usecase

import com.musicsocial.domain.form.LoginForm
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

        try {
            UseCaseResult.Success(auth.register(form.email.trim().lowercase(), form.password))
        } catch (e: EmailAlreadyRegisteredException) {
            UseCaseResult.Invalid(ValidationResult(mapOf(Field.EMAIL to ValidationError.AlreadyTaken)))
        }
    }
}

class SignInUseCase(private val auth: AuthRepository) {

    suspend operator fun invoke(form: LoginForm): UseCaseResult<String> = runCatchingUseCase {
        val validation = UserValidator.validateLogin(form)
        if (!validation.isValid) return@runCatchingUseCase UseCaseResult.Invalid(validation)
        try {
            UseCaseResult.Success(auth.signIn(form.email.trim().lowercase(), form.password))
        } catch (e: InvalidCredentialsException) {
            UseCaseResult.Invalid(ValidationResult(mapOf(Field.GENERAL to ValidationError.InvalidCredentials)))
        }
    }
}

/** A dónde debe ir la app al abrirse. */
enum class StartDestination { LOGIN, ONBOARDING, HOME }

/** Decide la primera pantalla: sin sesión → login; sin perfil → onboarding; si no → inicio. */
class GetStartDestinationUseCase(
    private val auth: AuthRepository,
    private val users: UserRepository,
) {
    suspend operator fun invoke(): StartDestination {
        val userId = auth.restoreSession() ?: return StartDestination.LOGIN
        return try {
            if (users.getProfile(userId) == null) StartDestination.ONBOARDING else StartDestination.HOME
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión: mejor mostrar el inicio que bloquear la app.
            StartDestination.HOME
        }
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
