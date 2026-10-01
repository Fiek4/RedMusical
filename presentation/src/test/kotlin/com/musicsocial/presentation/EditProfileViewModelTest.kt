package com.musicsocial.presentation

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.profile.EditProfileViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditProfileViewModelTest : ViewModelTest() {

    @Test
    fun `onboarding crea el perfil`() = runTest {
        val userId = app.auth.register("fer@musica.com", "beats2026")
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile)
        assertTrue(vm.uiState.value.isNewProfile)

        vm.onUsernameChange("fer.beats")
        vm.onArtistNameChange("Fer Beats")
        vm.onRoleToggle(ArtistRole.PRODUCER)
        vm.onGenreToggle(Genre.TRAP)
        vm.onSave()

        assertTrue(vm.uiState.value.saved)
        val saved = app.users.getProfile(userId)!!
        assertEquals("Fer Beats", saved.artistName)
    }

    @Test
    fun `nombre de usuario ocupado`() = runTest {
        app.signIn("luis")
        app.auth.register("fer@musica.com", "beats2026")
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile)

        vm.onUsernameChange("luis")
        vm.onArtistNameChange("Fer")
        vm.onRoleToggle(ArtistRole.PRODUCER)
        vm.onGenreToggle(Genre.TRAP)
        vm.onSave()

        assertEquals(ValidationError.AlreadyTaken, vm.uiState.value.errors[Field.USERNAME])
    }
}
