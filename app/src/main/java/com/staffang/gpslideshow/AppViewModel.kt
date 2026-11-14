package com.staffang.gpslideshow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.staffang.gpslideshow.auth.AuthRepository
import com.staffang.gpslideshow.picker.PickerRepository
import com.staffang.gpslideshow.settings.SettingsRepository
import com.staffang.gpslideshow.settings.SlideshowSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen { Home, Settings, Slideshow }

data class UiState(
    val status: String = "",
    val isLoading: Boolean = false,
    val mediaItems: List<PickerMediaItem> = emptyList(),
    val error: String? = null
)

class AppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val pickerRepository: PickerRepository = PickerRepository(),
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _screen = MutableStateFlow(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    val settings: StateFlow<SlideshowSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, SlideshowSettings())

    fun setScreen(screen: Screen) {
        _screen.value = screen
    }

    fun updateSettings(newSettings: SlideshowSettings) {
        viewModelScope.launch { settingsRepository.save(newSettings) }
    }

    fun selectMedia(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true, status = "Loggar in hos Google …")
            try {
                val token = authRepository.getAccessToken(activity)

                _uiState.value = _uiState.value.copy(status = "Skapar val-session …")
                val session = pickerRepository.createSession(token)

                openPicker(activity, session.pickerUri)

                _uiState.value = _uiState.value.copy(
                    status = "Välj bilder och videor i webbläsaren. Väntar på ditt val …"
                )

                val done = pickerRepository.waitForMediaItems(
                    sessionId = session.id,
                    accessToken = token,
                    pollIntervalMs = session.pollIntervalMs
                )
                if (!done) {
                    _uiState.value = UiState(error = "Inget val gjordes inom tidsgränsen.")
                    return@launch
                }

                _uiState.value = _uiState.value.copy(status = "Hämtar medieinformation …")
                val items = pickerRepository.listMediaItems(session.id, token)
                _uiState.value = if (items.isEmpty()) {
                    UiState(error = "Inga medier valdes.")
                } else {
                    UiState(
                        status = "${items.size} medier valda. Klara att starta bildspelet!",
                        mediaItems = items
                    )
                }
            } catch (e: Exception) {
                _uiState.value = UiState(error = e.message ?: "Okänt fel: ${e.javaClass.simpleName}")
            }
        }
    }

    private fun openPicker(activity: Activity, pickerUri: String) {
        val uri = Uri.parse(pickerUri).buildUpon()
            .appendPath("autoclose")
            .build()
        activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                AppViewModel(
                    settingsRepository = SettingsRepository(context.applicationContext)
                )
            }
        }
    }
}
