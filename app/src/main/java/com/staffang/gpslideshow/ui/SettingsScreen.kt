package com.staffang.gpslideshow.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.staffang.gpslideshow.settings.SlideshowSettings
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    settings: SlideshowSettings,
    onChange: (SlideshowSettings) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Inställningar",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Visningstid per bild: ${settings.imageSeconds} s",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = settings.imageSeconds.toFloat(),
            onValueChange = { onChange(settings.copy(imageSeconds = it.roundToInt().coerceAtLeast(1))) },
            valueRange = 1f..120f
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (settings.playWholeVideo) {
                "Max videolängd: spela hela videon"
            } else {
                "Max videolängd: ${settings.videoMaxSeconds} s"
            },
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = settings.videoMaxSeconds.toFloat(),
            onValueChange = { onChange(settings.copy(videoMaxSeconds = it.roundToInt())) },
            valueRange = 0f..300f
        )
        Text(
            text = "Dra till 0 för att spela hela videor.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upprepa bildspelet",
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = settings.loop,
                onCheckedChange = { onChange(settings.copy(loop = it)) }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Klar")
        }
    }
}
