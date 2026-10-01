package com.musicsocial.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicsocial.app.R
import com.musicsocial.app.ui.components.ChipGroup
import com.musicsocial.app.ui.label
import com.musicsocial.domain.model.ArtistRole
import com.musicsocial.domain.model.CollabCall
import com.musicsocial.domain.model.Genre
import com.musicsocial.presentation.feed.FeedViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Inicio: convocatorias abiertas, filtradas por rol y género. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onSignedOut: () -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val signedOut by viewModel.signedOut.collectAsStateWithLifecycle()
    LaunchedEffect(signedOut) { if (signedOut) onSignedOut() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feed_title)) },
                actions = {
                    TextButton(onClick = viewModel::onSignOut) { Text(stringResource(R.string.action_sign_out)) }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ChipGroup(ArtistRole.entries, state.filter.roles, viewModel::onRoleToggle) { it.label() }
            }
            item {
                ChipGroup(Genre.entries, state.filter.genres, viewModel::onGenreToggle) { it.label() }
            }

            when {
                state.isLoading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.calls.isEmpty() -> item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.feed_empty), modifier = Modifier.padding(top = 32.dp))
                        TextButton(onClick = viewModel::onClearFilters) { Text(stringResource(R.string.feed_clear_filters)) }
                    }
                }
                else -> items(state.calls, key = { it.id }) { call -> CallCard(call) }
            }
        }
    }
}

private val deadlineFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
private fun CallCard(call: CollabCall) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(call.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "${stringResource(R.string.feed_looking_for, call.lookingFor.label())} · ${call.genre.label()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(call.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            Text(
                "${call.dealType.label()} · " +
                    stringResource(R.string.feed_deadline, deadlineFormat.format(call.deadline.atZone(ZoneId.systemDefault()))),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
