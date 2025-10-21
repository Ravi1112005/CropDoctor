package com.example.cropdoctor.ui.screens.auth

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cropdoctor.BuildConfig
import com.example.cropdoctor.R
import com.example.cropdoctor.ui.components.FormField
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            onRegisterSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onRegisterSuccess()
                        } else {
                            error = task.exception?.message
                        }
                    }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                error = "Google sign in failed"
            }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            val logo = if (isSystemInDarkTheme()) {
                R.drawable.ic_cropdoctor_logo
            } else {
                R.drawable.ic_cropdoctor_logo_green
            }

            Image(
                painter = painterResource(id = logo),
                contentDescription = "CropDoctor Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Login", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { /* Current Screen */ },
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Sign Up")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            FormField(label = "Name", textState = name, onTextField = { name = it })
            FormField(label = "Email", textState = email, onTextField = { email = it })
            FormField(
                label = "Password",
                textState = password,
                onTextField = { password = it },
                isPasswordField = true,
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible }
            )
            FormField(
                label = "Confirm Password",
                textState = confirmPassword,
                onTextField = { confirmPassword = it },
                isPasswordField = true,
                passwordVisible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible }
            )
            Spacer(modifier = Modifier.height(32.dp))

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        error = "All fields are required."
                        return@Button
                    }
                    if (password != confirmPassword) {
                        error = "Passwords do not match."
                        return@Button
                    }

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = name
                                }
                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        onRegisterSuccess()
                                    }
                                }

                            } else {
                                error = task.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Sign Up")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(painterResource(id = R.drawable.google_icon), contentDescription = "Google Icon", modifier = Modifier.size(25.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Continue with Google")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegisterScreenPreview() {
    Surface {
        RegisterScreen(onLoginClick = {}, onRegisterSuccess = {})
    }
}
