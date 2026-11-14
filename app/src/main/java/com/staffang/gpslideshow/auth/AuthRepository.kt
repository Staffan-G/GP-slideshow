package com.staffang.gpslideshow.auth

import android.app.Activity
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.common.api.Scope
import com.staffang.gpslideshow.AppConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository {

    suspend fun getAccessToken(activity: Activity): String =
        suspendCancellableCoroutine { continuation ->
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(AppConfig.PHOTO_PICKER_SCOPE)))
                .build()

            AuthorizationClient.authorize(activity, request)
                .addOnSuccessListener { result ->
                    val token = result.accessToken
                    if (token != null) {
                        continuation.resume(token)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException(
                                "Ingen åtkomsttoken mottogs. Kontrollera att OAuth-klientens " +
                                    "paketnamn och SHA-1 stämmer, och att ditt konto är tillagt som " +
                                    "testanvändare i OAuth consent screen."
                            )
                        )
                    }
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}
