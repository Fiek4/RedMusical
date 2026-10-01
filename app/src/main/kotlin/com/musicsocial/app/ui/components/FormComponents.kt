package com.musicsocial.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.musicsocial.app.R
import com.musicsocial.app.ui.message
import com.musicsocial.app.ui.theme.Brand
import com.musicsocial.domain.validation.ValidationError

/**
 * Campo de texto con el error de validación debajo.
 * Si es contraseña, muestra un ojo para verla u ocultarla.
 */
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: ValidationError?,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error.message()) }
        } else {
            null
        },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.medium,
        colors = appTextFieldColors(),
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                        ),
                        contentDescription = stringResource(
                            if (passwordVisible) R.string.action_hide_password else R.string.action_show_password,
                        ),
                    )
                }
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Botón principal con el degradado de marca; muestra un spinner mientras carga. */
@Composable
fun LoadingButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .alpha(if (isLoading) 0.6f else 1f)
                .background(Brand.gradient),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Grupo de chips para elegir varias opciones (roles, géneros). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipGroup(
    options: List<T>,
    selected: Set<T>,
    onToggle: (T) -> Unit,
    label: @Composable (T) -> String,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onToggle(option) },
                label = { Text(label(option)) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}
