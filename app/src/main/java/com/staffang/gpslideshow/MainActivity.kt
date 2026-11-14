package com.staffang.gpslideshow

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.staffang.gpslideshow.ui.HomeScreen
import com.staffang.gpslideshow.ui.SettingsScreen
import com.staffang.gpslideshow.ui.SlideshowScreen
import com.staffang.gpslideshow.ui.theme.GPSlideshowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels { AppViewModel.factory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSlideshowTheme {
                AppNav(viewModel)
            }
        }
    }
}

@Composable
private fun AppNav(viewModel: AppViewModel) {
    val screen by viewModel.screen.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activity = LocalContext.current as Activity

    when (screen) {
        Screen.Home -> HomeScreen(
            uiState = uiState,
            onSelectMedia = { viewModel.selectMedia(activity) },
            onStartSlideshow = { viewModel.setScreen(Screen.Slideshow) },
            onOpenSettings = { viewModel.setScreen(Screen.Settings) }
        )

        Screen.Settings -> SettingsScreen(
            settings = settings,
            onChange = { viewModel.updateSettings(it) },
            onBack = { viewModel.setScreen(Screen.Home) }
        )

        Screen.Slideshow -> SlideshowScreen(
            items = uiState.mediaItems,
            settings = settings,
            onExit = { viewModel.setScreen(Screen.Home) }
        )
    }
}
