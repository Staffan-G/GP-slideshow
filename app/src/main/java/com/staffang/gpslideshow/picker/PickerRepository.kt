package com.staffang.gpslideshow.picker

import com.staffang.gpslideshow.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class PickerRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun createSession(accessToken: String): PickerSession = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/sessions")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("X-Goog-User-Project", AppConfig.GOOGLE_CLOUD_PROJECT_ID)
            .post("{".toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val json = JSONObject(execute(request))
        val pollInterval = json.optJSONObject("pollingConfig")?.optLong("pollInterval") ?: 2000L
        PickerSession(
            id = json.getString("id"),
            pickerUri = json.getString("pickerUri"),
            pollIntervalMs = pollInterval.coerceAtLeast(1000L)
        )
    }

    suspend fun waitForMediaItems(
        sessionId: String,
        accessToken: String,
        pollIntervalMs: Long,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): Boolean = withContext(Dispatchers.IO) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val request = Request.Builder()
                .url("$BASE_URL/sessions/$sessionId")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("X-Goog-User-Project", AppConfig.GOOGLE_CLOUD_PROJECT_ID)
                .get()
                .build()

            val json = JSONObject(execute(request))
            if (json.optBoolean("mediaItemsSet", false)) {
                return@withContext true
            }
            delay(pollIntervalMs)
        }
        false
    }

    suspend fun listMediaItems(sessionId: String, accessToken: String): List<PickerMediaItem> =
        withContext(Dispatchers.IO) {
            val items = mutableListOf<PickerMediaItem>()
            var pageToken: String? = null
            do {
                var url = "$BASE_URL/mediaItems?sessionId=$sessionId&pageSize=50"
                if (pageToken != null) {
                    url += "&pageToken=" + URLEncoder.encode(pageToken, "UTF-8")
                }

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("X-Goog-User-Project", AppConfig.GOOGLE_CLOUD_PROJECT_ID)
                    .get()
                    .build()

                val json = JSONObject(execute(request))
                val mediaItems = json.optJSONArray("mediaItems")
                if (mediaItems != null) {
                    for (i in 0 until mediaItems.length()) {
                        val item = mediaItems.getJSONObject(i)
                        val file = item.getJSONObject("mediaFile")
                        items += PickerMediaItem(
                            id = item.getString("id"),
                            isVideo = item.optString("type") == "VIDEO",
                            baseUrl = file.getString("baseUrl"),
                            mimeType = file.optString("mimeType")
                        )
                    }
                }
                pageToken = if (json.has("nextPageToken")) json.getString("nextPageToken") else null
            } while (pageToken != null)
            items
        }

    private fun execute(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    "Google API-anrop misslyckades (HTTP ${response.code}): $body"
                )
            }
            return body
        }
    }

    companion object {
        private const val BASE_URL = "https://photospicker.googleapis.com/v1"
        private const val DEFAULT_TIMEOUT_MS = 10L * 60 * 1000
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
