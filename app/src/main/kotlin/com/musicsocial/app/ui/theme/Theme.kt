package com.musicsocial.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Colores de marca: noche violeta con acentos neón (violeta → rosa → ámbar). */
object Brand {
    val Night = Color(0xFF0E0B1A)
    val Violet = Color(0xFF8B5CF6)
    val Pink = Color(0xFFEC4899)
    val Amber = Color(0xFFF59E0B)

    /** Degradado principal: botones, logo y títulos destacados. */
    val gradient: Brush get() = Brush.linearGradient(listOf(Violet, Pink, Amber))
}

// La app usa siempre el tema oscuro: es la estética de las apps de música y los neones lucen más.
private val Colors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1A0B3D),
    primaryContainer = Color(0xFF3B2470),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF3D0A24),
    secondaryContainer = Color(0xFF5C1A3D),
    onSecondaryContainer = Color(0xFFFFD8E8),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF3A2600),
    tertiaryContainer = Color(0xFF4D3500),
    onTertiaryContainer = Color(0xFFFFE7A8),
    background = Brand.Night,
    onBackground = Color(0xFFF5F3FF),
    surface = Color(0xFF15102A),
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF241C3D),
    onSurfaceVariant = Color(0xFFB9B0D6),
    surfaceContainerLowest = Color(0xFF0B0816),
    surfaceContainerLow = Color(0xFF130E25),
    surfaceContainer = Color(0xFF1A1430),
    surfaceContainerHigh = Color(0xFF221A3B),
    surfaceContainerHighest = Color(0xFF2B2247),
    outline = Color(0xFF4B3F6B),
    outlineVariant = Color(0xFF332A4F),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

private val AppTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun MusicSocialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content,
    )
}
