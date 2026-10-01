package com.musicsocial.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.ui.components.ChipGroup
import com.musicsocial.app.ui.components.FormField
import com.musicsocial.app.ui.components.LoadingButton
import com.musicsocial.app.ui.components.PickerField
import com.musicsocial.app.ui.label
import com.musicsocial.app.ui.message
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.Genre
import com.musicsocial.domain.validation.Field
import com.musicsocial.presentation.profile.EditProfileViewModel
import org.koin.androidx.compose.koinViewModel

/** Onboarding (perfil nuevo) y edición de perfil. */
@Composable
fun ProfileScreen(
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorText = state.error?.message()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbar.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(if (state.isNewProfile) R.string.profile_title_new else R.string.profile_title_edit),
                style = MaterialTheme.typography.headlineMedium,
            )

            FormField(
                value = state.form.artistName,
                onValueChange = viewModel::onArtistNameChange,
                label = stringResource(R.string.field_artist_name),
                error = state.errors[Field.ARTIST_NAME],
            )
            FormField(
                value = state.form.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.field_username),
                error = state.errors[Field.USERNAME],
            )
            PickerField(
                label = stringResource(R.string.field_country),
                value = state.countryName,
                options = state.countries,
                optionLabel = { it.name },
                onSelect = { viewModel.onCountrySelected(it.code) },
            )
            PickerField(
                label = stringResource(R.string.field_region),
                value = state.region,
                options = state.regions,
                optionLabel = { it },
                onSelect = viewModel::onRegionSelected,
                enabled = state.form.country.isNotEmpty(),
                hint = stringResource(R.string.hint_choose_country_first),
            )
            // Si su ciudad no está en la lista, puede escribirla en el buscador y usarla igual.
            PickerField(
                label = stringResource(R.string.field_city),
                value = state.form.city,
                options = state.cities,
                optionLabel = { it },
                onSelect = viewModel::onCityChange,
                enabled = state.region.isNotEmpty(),
                error = state.errors[Field.CITY],
                hint = stringResource(R.string.hint_choose_region_first),
                allowCustom = viewModel::onCityChange,
            )
            FormField(
                value = state.form.bio,
                onValueChange = viewModel::onBioChange,
                label = stringResource(R.string.field_bio),
                error = state.errors[Field.BIO],
                singleLine = false,
            )

            Text(stringResource(R.string.profile_roles), style = MaterialTheme.typography.titleMedium)
            ChipGroup(ArtistRole.entries, state.form.roles, viewModel::onRoleToggle) { it.label() }
            state.errors[Field.ROLES]?.let { Text(it.message(), color = MaterialTheme.colorScheme.error) }

            Text(stringResource(R.string.profile_genres), style = MaterialTheme.typography.titleMedium)
            ChipGroup(Genre.entries, state.form.genres, viewModel::onGenreToggle) { it.label() }
            state.errors[Field.GENRES]?.let { Text(it.message(), color = MaterialTheme.colorScheme.error) }

            LoadingButton(stringResource(R.string.action_save), state.isSaving, viewModel::onSave)
        }
    }
}
