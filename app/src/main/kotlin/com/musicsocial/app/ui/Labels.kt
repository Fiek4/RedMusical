package com.musicsocial.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.musicsocial.app.R
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.DealType
import com.musicsocial.domain.model.Genre

/** Textos para mostrar los enums del dominio en pantalla. */
@Composable
fun ArtistRole.label(): String = stringResource(
    when (this) {
        ArtistRole.PRODUCER -> R.string.role_producer
        ArtistRole.SINGER -> R.string.role_singer
        ArtistRole.RAPPER -> R.string.role_rapper
        ArtistRole.INSTRUMENTALIST -> R.string.role_instrumentalist
        ArtistRole.MIX_ENGINEER -> R.string.role_mix_engineer
        ArtistRole.SONGWRITER -> R.string.role_songwriter
    },
)

@Composable
fun Genre.label(): String = stringResource(
    when (this) {
        Genre.TRAP -> R.string.genre_trap
        Genre.REGGAETON -> R.string.genre_reggaeton
        Genre.HIP_HOP -> R.string.genre_hip_hop
        Genre.DRILL -> R.string.genre_drill
        Genre.RNB -> R.string.genre_rnb
        Genre.POP -> R.string.genre_pop
        Genre.ROCK -> R.string.genre_rock
        Genre.ELECTRONIC -> R.string.genre_electronic
        Genre.LATIN -> R.string.genre_latin
        Genre.OTHER -> R.string.genre_other
    },
)

@Composable
fun DealType.label(): String = stringResource(
    when (this) {
        DealType.FREE_COLLAB -> R.string.deal_free_collab
        DealType.PAID -> R.string.deal_paid
        DealType.EXCHANGE -> R.string.deal_exchange
    },
)
