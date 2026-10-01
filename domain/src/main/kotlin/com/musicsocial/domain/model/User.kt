package com.musicsocial.domain.model

import java.time.Instant

/**
 * Perfil público del artista (su "blog"). No incluye el email:
 * ese dato es privado y lo maneja [com.musicsocial.domain.repository.AuthRepository].
 */
data class UserProfile(
    val id: String,
    val username: String,
    val artistName: String,
    val photoUrl: String? = null,
    val bio: String = "",
    val roles: Set<ArtistRole>,
    val genres: Set<Genre>,
    val city: String? = null,
    val country: String? = null,
    val links: List<ExternalLink> = emptyList(),
    val plan: Plan = Plan.FREE,
    val createdAt: Instant,
)

data class ExternalLink(
    val platform: LinkPlatform,
    val url: String,
)

enum class LinkPlatform { SPOTIFY, INSTAGRAM, YOUTUBE, TIKTOK, SOUNDCLOUD, OTHER }

data class Follow(
    val followerId: String,
    val followedId: String,
    val createdAt: Instant,
)
