package com.musicsocial.presentation

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.UserProfile
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
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)
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
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)

        vm.onUsernameChange("luis")
        vm.onArtistNameChange("Fer")
        vm.onRoleToggle(ArtistRole.PRODUCER)
        vm.onGenreToggle(Genre.TRAP)
        vm.onSave()

        assertEquals(ValidationError.AlreadyTaken, vm.uiState.value.errors[Field.USERNAME])
    }

    @Test
    fun `region depende del pais y ciudad de la region`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)
        assertEquals(listOf("CL", "PE"), vm.uiState.value.countries.map { it.code })
        assertEquals(emptyList(), vm.uiState.value.regions)

        vm.onCountrySelected("CL")
        assertEquals("Chile", vm.uiState.value.countryName)
        assertEquals(listOf("Región Metropolitana de Santiago", "Valparaíso"), vm.uiState.value.regions)
        assertEquals(emptyList(), vm.uiState.value.cities)

        vm.onRegionSelected("Valparaíso")
        assertEquals(listOf("Valparaíso", "Viña del Mar"), vm.uiState.value.cities)

        vm.onCityChange("Viña del Mar")
        assertEquals("Viña del Mar", vm.uiState.value.form.city)
    }

    @Test
    fun `cambiar de pais borra region y ciudad`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)
        vm.onCountrySelected("CL")
        vm.onRegionSelected("Valparaíso")
        vm.onCityChange("Valparaíso")

        vm.onCountrySelected("PE")

        val state = vm.uiState.value
        assertEquals("PE", state.form.country)
        assertEquals("", state.region)
        assertEquals("", state.form.city)
        assertEquals(listOf("Lima"), state.regions)
        assertEquals(emptyList(), state.cities)
    }

    @Test
    fun `guarda pais y ciudad y al volver recupera la region`() = runTest {
        val userId = app.auth.register("fer@musica.com", "beats2026")
        val vm = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)
        vm.onUsernameChange("fer.beats")
        vm.onArtistNameChange("Fer Beats")
        vm.onRoleToggle(ArtistRole.PRODUCER)
        vm.onGenreToggle(Genre.TRAP)
        vm.onCountrySelected("PE")
        vm.onRegionSelected("Lima")
        vm.onCityChange("Miraflores")
        vm.onSave()

        val saved: UserProfile = app.users.getProfile(userId)!!
        assertEquals("PE", saved.country)
        assertEquals("Miraflores", saved.city)

        val again = EditProfileViewModel(app.auth, app.users, app.saveProfile, app.locations)
        assertEquals("Lima", again.uiState.value.region)
        assertEquals(listOf("Lima", "Miraflores"), again.uiState.value.cities)
    }
}
