package com.musicsocial.domain.validation

import com.musicsocial.domain.form.ProfileForm
import com.musicsocial.domain.form.RegisterForm
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.ExternalLink
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.model.LinkPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserValidatorTest {

    @Test
    fun `registro valido`() {
        val result = UserValidator.validateRegister(RegisterForm("fer@musica.com", "beats2026", "beats2026"))
        assertTrue(result.isValid)
    }

    @Test
    fun `registro vacio marca todos los campos como requeridos`() {
        val result = UserValidator.validateRegister(RegisterForm())
        assertEquals(ValidationError.Required, result.errorFor(Field.EMAIL))
        assertEquals(ValidationError.Required, result.errorFor(Field.PASSWORD))
        assertEquals(ValidationError.Required, result.errorFor(Field.CONFIRM_PASSWORD))
    }

    @Test
    fun `contrasenas distintas`() {
        val result = UserValidator.validateRegister(RegisterForm("fer@musica.com", "beats2026", "beats2027"))
        assertEquals(ValidationError.PasswordsDoNotMatch, result.errorFor(Field.CONFIRM_PASSWORD))
    }

    @Test
    fun `perfil valido`() {
        val form = ProfileForm(
            username = "fer.beats",
            artistName = "Fer Beats",
            roles = setOf(ArtistRole.PRODUCER),
            genres = setOf(Genre.TRAP),
            links = listOf(ExternalLink(LinkPlatform.INSTAGRAM, "https://instagram.com/ferbeats")),
        )
        assertTrue(UserValidator.validateProfile(form).isValid)
    }

    @Test
    fun `perfil con errores`() {
        val form = ProfileForm(
            username = "Fer Beats!",
            artistName = "F",
            links = listOf(ExternalLink(LinkPlatform.OTHER, "no-es-url")),
        )
        val result = UserValidator.validateProfile(form)
        assertEquals(ValidationError.InvalidFormat, result.errorFor(Field.USERNAME))
        assertEquals(ValidationError.TooShort(Limits.ARTIST_NAME_MIN), result.errorFor(Field.ARTIST_NAME))
        assertEquals(ValidationError.Required, result.errorFor(Field.ROLES))
        assertEquals(ValidationError.Required, result.errorFor(Field.GENRES))
        assertEquals(ValidationError.InvalidFormat, result.errorFor(Field.LINKS))
    }
}
