package com.staffang.gpslideshow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.staffang.gpslideshow.picker.PickerMediaItem
import com.staffang.gpslideshow.settings.SlideshowSettings
import kotlinx.coroutines.delay

@Composable
fun SlideshowScreen(
    items: List<PickerMediaItem>,
    settings: SlideshowSettings,
    onExit: () -> Unit
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (items.isEmpty()) {
            Text(
                text = "Inga medier valda än. Gå tillbaka och välj bilder och videor först.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        } else {
            var index by remember { mutableIntStateOf(0) }
            var finished by remember { mutableStateOf(false) }

            val advance: () -> Unit = {
                val next = index + 1
                when {
                    next < items.size -> index = next
                    settings.loop -> index = 0
                    else -> finished = true
                }
            }

            if (finished) {
                LaunchedEffect(finished) { onExit() }
            }

            val item = items[index]
            if (item.isVideo) {
                VideoPlayer(
                    item = item,
                    videoMaxSeconds = settings.videoMaxSeconds,
                    onDone = advance
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.baseUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                LaunchedEffect(item.id) {
                    delay(settings.imageSeconds * 1000L)
                    advance()
                }
            }

            TextButton(
                onClick = onExit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text("Avsluta", color = Color.White)
            }
        }
    }
}

@Composable
private fun VideoPlayer(
    item: PickerMediaItem,
    videoMaxSeconds: Int,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val advanced = remember(item.id) { mutableStateOf(false) }
    val advance: () -> Unit = {
        if (!advanced.value) {
            advanced.value = true
            onDone()
        }
    }

    val player = remember(item.id) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(item.baseUrl))
            repeatMode = Player.REPEAT_OFF
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    advance()
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(item.id) {
        if (videoMaxSeconds > 0) {
            delay(videoMaxSeconds * 1000L)
            player.pause()
            advance()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                this.player = player
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
