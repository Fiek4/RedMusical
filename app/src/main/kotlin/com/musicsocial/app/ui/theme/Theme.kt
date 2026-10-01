package com.musicsocial.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Purple = Color(0xFF7C3AED)
private val Pink = Color(0xFFEC4899)

private val LightColors = lightColorScheme(primary = Purple, secondary = Pink)
private val DarkColors = darkColorScheme(primary = Color(0xFFA78BFA), secondary = Color(0xFFF472B6))

@Composable
fun MusicSocialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
