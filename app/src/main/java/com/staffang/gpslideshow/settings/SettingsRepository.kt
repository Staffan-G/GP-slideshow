package com.staffang.gpslideshow.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "slideshow_settings")

data class SlideshowSettings(
    val imageSeconds: Int = 5,
    val videoMaxSeconds: Int = 0,
    val loop: Boolean = true
) {
    val playWholeVideo: Boolean get() = videoMaxSeconds <= 0
}

class SettingsRepository(private val context: Context) {

    private val imageSecondsKey = intPreferencesKey("image_seconds")
    private val videoMaxSecondsKey = intPreferencesKey("video_max_seconds")
    private val loopKey = booleanPreferencesKey("loop")

    val settings: Flow<SlideshowSettings> = context.dataStore.data.map { prefs ->
        SlideshowSettings(
            imageSeconds = prefs[imageSecondsKey] ?: DEFAULT_IMAGE_SECONDS,
            videoMaxSeconds = prefs[videoMaxSecondsKey] ?: DEFAULT_VIDEO_MAX_SECONDS,
            loop = prefs[loopKey] ?: true
        )
    }

    suspend fun save(settings: SlideshowSettings) {
        context.dataStore.edit { prefs ->
            prefs[imageSecondsKey] = settings.imageSeconds
            prefs[videoMaxSecondsKey] = settings.videoMaxSeconds
            prefs[loopKey] = settings.loop
        }
    }

    companion object {
        const val DEFAULT_IMAGE_SECONDS = 5
        const val DEFAULT_VIDEO_MAX_SECONDS = 0
    }
}
