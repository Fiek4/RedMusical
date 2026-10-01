package com.musicsocial.presentation

import com.musicsocial.domain.usecase.StartDestination
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.auth.LoginViewModel
import com.musicsocial.presentation.feed.FeedViewModel
import com.musicsocial.presentation.session.StartViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionViewModelTest : ViewModelTest() {

    @Test
    fun `sin sesion va al login`() = runTest {
        assertEquals(StartDestination.LOGIN, StartViewModel(app.getStartDestination).destination.value)
    }

    @Test
    fun `con sesion pero sin perfil va al onboarding`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        assertEquals(StartDestination.ONBOARDING, StartViewModel(app.getStartDestination).destination.value)
    }

    @Test
    fun `con perfil va al inicio`() = runTest {
        app.signIn("ana")
        assertEquals(StartDestination.HOME, StartViewModel(app.getStartDestination).destination.value)
    }

    @Test
    fun `login correcto`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        app.auth.signOut()

        val vm = LoginViewModel(app.signIn)
        vm.onEmailChange("Fer@Musica.com")
        vm.onPasswordChange("beats2026")
        vm.onSubmit()

        assertTrue(vm.uiState.value.signedIn)
        assertTrue(vm.uiState.value.errors.isEmpty())
    }

    @Test
    fun `login con contrasena incorrecta`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        app.auth.signOut()

        val vm = LoginViewModel(app.signIn)
        vm.onEmailChange("fer@musica.com")
        vm.onPasswordChange("otra1234")
        vm.onSubmit()

        assertFalse(vm.uiState.value.signedIn)
        assertEquals(ValidationError.InvalidCredentials, vm.uiState.value.errors[Field.GENERAL])
    }

    @Test
    fun `cerrar sesion desde el feed`() = runTest {
        app.signIn("ana")
        val feed = FeedViewModel(app.auth, app.users, app.calls, app.clock)
        feed.onSignOut()

        assertTrue(feed.signedOut.value)
        assertNull(app.auth.currentUserId())
    }
}
