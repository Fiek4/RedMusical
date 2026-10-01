package com.musicsocial.domain.form

import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.AudioFile
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.ExternalLink
import com.musicsocial.domain.model.Genre
import java.time.Instant

/*
 * Datos tal como los escribe el usuario en un formulario, antes de validar.
 * Los campos son nullables o texto libre porque el usuario puede dejarlos vacíos;
 * el validador decide si son correctos y el ViewModel convierte a modelo.
 */

data class RegisterForm(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
)

data class LoginForm(
    val email: String = "",
    val password: String = "",
)

data class ProfileForm(
    val username: String = "",
    val artistName: String = "",
    val bio: String = "",
    val roles: Set<ArtistRole> = emptySet(),
    val genres: Set<Genre> = emptySet(),
    val city: String = "",
    val links: List<ExternalLink> = emptyList(),
)

data class TrackForm(
    val title: String = "",
    val genre: Genre? = null,
    val bpm: String = "",
    val audio: AudioFile? = null,
)

data class CollabCallForm(
    val title: String = "",
    val lookingFor: ArtistRole? = null,
    val genre: Genre? = null,
    val description: String = "",
    val bpm: String = "",
    val musicalKey: String = "",
    val referenceAudio: AudioFile? = null,
    val deadline: Instant? = null,
    val dealType: DealType? = null,
    val budgetMin: String = "",
    val budgetMax: String = "",
    val currency: String = "USD",
)

data class ApplicationForm(
    val demo: AudioFile? = null,
    val message: String = "",
)
