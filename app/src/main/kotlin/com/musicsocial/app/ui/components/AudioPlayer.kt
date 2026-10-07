package com.musicsocial.app.ui.components

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.musicsocial.app.R

private enum class PlayerState { IDLE, LOADING, PLAYING, PAUSED, ERROR }

/**
 * Botón para escuchar un audio: por streaming (la URL pública de Supabase) o un archivo local (modo demo).
 * Se carga recién al tocar play y se libera al salir de la pantalla.
 */
@Composable
fun AudioPlayerButton(url: String, durationSeconds: Int) {
    val context = LocalContext.current
    var state by remember(url) { mutableStateOf(PlayerState.IDLE) }
    val player = remember(url) { MediaPlayer() }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    fun start() {
        state = PlayerState.LOADING
        runCatching {
            player.reset()
            player.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
            )
            player.setDataSource(context, Uri.parse(url))
            player.setOnPreparedListener { it.start(); state = PlayerState.PLAYING }
            player.setOnCompletionListener { state = PlayerState.IDLE }
            player.setOnErrorListener { _, _, _ -> state = PlayerState.ERROR; true }
            player.prepareAsync()
        }.onFailure { state = PlayerState.ERROR }
    }

    OutlinedButton(
        onClick = {
            when (state) {
                PlayerState.IDLE, PlayerState.ERROR -> start()
                PlayerState.PLAYING -> { player.pause(); state = PlayerState.PAUSED }
                PlayerState.PAUSED -> { player.start(); state = PlayerState.PLAYING }
                PlayerState.LOADING -> Unit
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (state == PlayerState.LOADING) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            }
            Text(
                when (state) {
                    PlayerState.PLAYING -> stringResource(R.string.player_pause)
                    PlayerState.ERROR -> stringResource(R.string.player_error)
                    else -> stringResource(R.string.player_play, durationSeconds / 60, durationSeconds % 60)
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
