package com.example.cropdoctor.ui.components

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cropdoctor.BuildConfig
import com.example.cropdoctor.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun GoogleSignInButton(
    onSuccess: (idToken: String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val oneTapClient = Identity.getSignInClient(context)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val googleIdToken = credential.googleIdToken
                    if (googleIdToken != null) {
                        onSuccess(googleIdToken)
                    } else {
                        onError("Google ID token was null.")
                    }
                } catch (e: ApiException) {
                    Log.w(ContentValues.TAG, "Google sign in failed", e)
                    onError("Google sign in failed. Please try again.")
                }
            } else {
                 onError("Google Sign-In was cancelled or failed.")
            }
        }
    )

    OutlinedButton(
        onClick = {
             scope.launch {
                try {
                    val signInRequest = BeginSignInRequest.builder()
                        .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                                .setFilterByAuthorizedAccounts(false) // This shows account chooser
                                .build()
                        )
                        .setAutoSelectEnabled(false)
                        .build()

                    val result = oneTapClient.beginSignIn(signInRequest).await()
                    launcher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())

                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Google sign-in launch failed", e)
                    onError(e.localizedMessage ?: "An error occurred during sign-in.")
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = "Google Icon",
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Continue with Google")
    }
}
