package com.musicsocial.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.musicsocial.app.ui.components.AppBackground
import com.musicsocial.app.ui.components.GradientText
import com.musicsocial.app.ui.theme.Brand
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
import androidx.compose.material3.ExtendedFloatingActionButton
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
    onCreateCall: () -> Unit,
    onOpenCall: (String) -> Unit,
    viewModel: FeedViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val signedOut by viewModel.signedOut.collectAsStateWithLifecycle()
    LaunchedEffect(signedOut) { if (signedOut) onSignedOut() }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        GradientText(stringResource(R.string.feed_title), style = MaterialTheme.typography.headlineSmall)
                    },
                    actions = {
                        TextButton(onClick = viewModel::onSignOut) { Text(stringResource(R.string.action_sign_out)) }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onCreateCall,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Text(stringResource(R.string.action_create_call)) }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                // Espacio abajo para que el botón flotante no tape la última tarjeta.
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
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
                    else -> items(state.calls, key = { it.id }) { call -> CallCard(call, onClick = { onOpenCall(call.id) }) }
                }
            }
        }
    }
}

private val deadlineFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
private fun CallCard(call: CollabCall, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        // Franja con el degradado de marca arriba de cada tarjeta.
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Brand.gradient),
        )
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(call.lookingFor.label(), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                Pill(call.genre.label(), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Text(call.title, style = MaterialTheme.typography.titleLarge)
            Text(
                call.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${call.dealType.label()} · " +
                    stringResource(R.string.feed_deadline, deadlineFormat.format(call.deadline.atZone(ZoneId.systemDefault()))),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun Pill(text: String, container: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = Modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
