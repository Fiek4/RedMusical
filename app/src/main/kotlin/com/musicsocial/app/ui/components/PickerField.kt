package com.musicsocial.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.musicsocial.app.R
import com.musicsocial.app.ui.message
import com.musicsocial.domain.validation.ValidationError
import java.text.Normalizer

/**
 * Campo de solo lectura que abre una lista con buscador para elegir una opción.
 * Si [allowCustom] es true y lo buscado no está en la lista, ofrece usar el texto tal cual.
 */
@Composable
fun <T> PickerField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: ValidationError? = null,
    hint: String? = null,
    allowCustom: ((String) -> Unit)? = null,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val supporting = error?.message() ?: hint?.takeIf { !enabled }

    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            isError = error != null,
            supportingText = if (supporting != null) {
                { Text(supporting) }
            } else {
                null
            },
            trailingIcon = { Icon(painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = appTextFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        // El TextField se come los toques; esta capa transparente abre la lista.
        if (enabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable { open = true },
            )
        }
    }

    if (open) {
        PickerDialog(
            title = label,
            options = options,
            optionLabel = optionLabel,
            onSelect = {
                onSelect(it)
                open = false
            },
            onCustom = allowCustom?.let { custom ->
                { text: String ->
                    custom(text)
                    open = false
                }
            },
            onDismiss = { open = false },
        )
    }
}

@Composable
private fun <T> PickerDialog(
    title: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onCustom: ((String) -> Unit)?,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(query, options) {
        val q = query.normalized()
        if (q.isEmpty()) options else options.filter { optionLabel(it).normalized().contains(q) }
    }
    val custom = query.trim()
    val showCustom = onCustom != null && custom.isNotEmpty() &&
        filtered.none { optionLabel(it).normalized() == custom.normalized() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.picker_search)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = appTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    if (showCustom) {
                        item {
                            PickerRow(stringResource(R.string.picker_use_custom, custom)) { onCustom?.invoke(custom) }
                            HorizontalDivider()
                        }
                    }
                    items(filtered) { option ->
                        PickerRow(optionLabel(option)) { onSelect(option) }
                    }
                    if (filtered.isEmpty() && !showCustom) {
                        item {
                            Text(
                                stringResource(R.string.picker_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun PickerRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

/** Minúsculas y sin tildes, para que "vina" encuentre "Viña del Mar". */
private fun String.normalized(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")
