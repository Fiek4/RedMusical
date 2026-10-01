package com.musicsocial.domain.validation

import com.musicsocial.domain.form.LoginForm
import com.musicsocial.domain.form.ProfileForm
import com.musicsocial.domain.form.RegisterForm

object UserValidator {

    fun validateRegister(form: RegisterForm): ValidationResult = validate {
        check(Field.EMAIL, { Rules.required(form.email) }, { Rules.email(form.email) })
        check(Field.PASSWORD, { Rules.required(form.password) }, { Rules.password(form.password) })
        check(
            Field.CONFIRM_PASSWORD,
            { Rules.required(form.confirmPassword) },
            { if (form.confirmPassword != form.password) ValidationError.PasswordsDoNotMatch else null },
        )
    }

    /** Al iniciar sesión solo revisamos que no esté vacío; la contraseña la valida el servidor. */
    fun validateLogin(form: LoginForm): ValidationResult = validate {
        check(Field.EMAIL, { Rules.required(form.email) }, { Rules.email(form.email) })
        check(Field.PASSWORD, Rules.required(form.password))
    }

    fun validateProfile(form: ProfileForm): ValidationResult = validate {
        check(
            Field.USERNAME,
            { Rules.required(form.username) },
            { Rules.minLength(form.username, Limits.USERNAME_MIN) },
            { Rules.maxLength(form.username, Limits.USERNAME_MAX) },
            { Rules.username(form.username) },
        )
        check(
            Field.ARTIST_NAME,
            { Rules.required(form.artistName) },
            { Rules.minLength(form.artistName, Limits.ARTIST_NAME_MIN) },
            { Rules.maxLength(form.artistName, Limits.ARTIST_NAME_MAX) },
        )
        check(Field.BIO, Rules.maxLength(form.bio, Limits.BIO_MAX))
        check(Field.ROLES, Rules.notEmpty(form.roles))
        check(Field.GENRES, Rules.notEmpty(form.genres))
        check(Field.CITY, Rules.maxLength(form.city, Limits.CITY_MAX))
        check(
            Field.LINKS,
            { Rules.maxItems(form.links, Limits.MAX_LINKS) },
            { form.links.firstNotNullOfOrNull { Rules.url(it.url) } },
        )
    }
}
