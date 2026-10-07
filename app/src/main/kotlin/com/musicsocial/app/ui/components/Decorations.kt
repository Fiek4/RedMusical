package com.musicsocial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicsocial.app.R
import com.musicsocial.app.ui.theme.Brand

/** Fondo de todas las pantallas: noche violeta con dos luces de neón difuminadas. */
@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                val radius = size.maxDimension * 0.6f
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Brand.Violet.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(0f, 0f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Brand.Pink.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width, size.height),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(size.width, size.height),
                )
            },
        content = content,
    )
}

/** Logo provisional: audífonos en un círculo con el degradado de marca y dos notas musicales. */
@Composable
fun MusicLogo(size: Dp = 160.dp) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * 0.8f)
                .background(brush = Brand.gradient, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_headphones),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.4f),
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = Brand.Amber,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(size * 0.2f)
                .rotate(15f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_music_note),
            contentDescription = null,
            tint = Brand.Pink,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 4.dp)
                .size(size * 0.15f)
                .rotate(-20f),
        )
    }
}

/** Texto pintado con el degradado de marca, para títulos destacados. */
@Composable
fun GradientText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    Text(text = text, style = style.copy(brush = Brand.gradient), modifier = modifier)
}

/** Colores comunes de los campos de texto: fondo translúcido y borde violeta al enfocar. */
@Composable
fun appTextFieldColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceContainer.copy(alpha = 0.7f),
        unfocusedContainerColor = colors.surfaceContainer.copy(alpha = 0.7f),
        disabledContainerColor = colors.surfaceContainer.copy(alpha = 0.35f),
        errorContainerColor = colors.surfaceContainer.copy(alpha = 0.7f),
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outline,
    )
}

/**
 * Esqueleto de las pantallas de formulario: fondo de marca, snackbar, scroll,
 * ancho máximo para tablets y espacio para el teclado.
 * Con [centered] el contenido queda en el medio de la pantalla (login, registro).
 */
@Composable
fun FormScreen(
    snackbar: SnackbarHostState,
    centered: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    AppBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color.Transparent,
            // Con fondo transparente el color del texto no se deduce solo: sin esto sale negro.
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = if (centered) Alignment.Center else Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content,
                )
            }
        }
    }
}
