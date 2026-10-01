package com.musicsocial.presentation

import com.musicsocial.domain.validation.Field
import com.musicsocial.domain.validation.ValidationError
import com.musicsocial.presentation.auth.RegisterViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterViewModelTest : ViewModelTest() {

    // lazy: el ViewModel se crea después de reemplazar Dispatchers.Main en @BeforeTest.
    private val vm by lazy { RegisterViewModel(app.register) }

    @Test
    fun `no muestra errores antes de enviar`() {
        vm.onEmailChange("mal")
        assertTrue(vm.uiState.value.errors.isEmpty())
    }

    @Test
    fun `al enviar vacio muestra errores y luego valida mientras escribe`() = runTest {
        vm.onSubmit()
        assertEquals(ValidationError.Required, vm.uiState.value.errors[Field.EMAIL])

        vm.onEmailChange("fer@musica.com")
        assertNull(vm.uiState.value.errors[Field.EMAIL])
        assertEquals(ValidationError.Required, vm.uiState.value.errors[Field.PASSWORD])
    }

    @Test
    fun `registro exitoso`() = runTest {
        vm.onEmailChange("fer@musica.com")
        vm.onPasswordChange("beats2026")
        vm.onConfirmPasswordChange("beats2026")
        vm.onSubmit()

        assertNotNull(vm.uiState.value.registeredUserId)
        assertEquals(false, vm.uiState.value.isLoading)
    }

    @Test
    fun `email ya registrado`() = runTest {
        app.auth.register("fer@musica.com", "beats2026")
        vm.onEmailChange("Fer@Musica.com")
        vm.onPasswordChange("beats2026")
        vm.onConfirmPasswordChange("beats2026")
        vm.onSubmit()

        assertEquals(ValidationError.AlreadyTaken, vm.uiState.value.errors[Field.EMAIL])
        assertNull(vm.uiState.value.registeredUserId)
    }
}
