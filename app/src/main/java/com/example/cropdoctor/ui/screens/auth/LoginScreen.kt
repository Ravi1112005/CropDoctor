package com.example.cropdoctor.ui.screens.auth

import android.content.ContentValues
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var showForgotPasswordDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            onLoginSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            error = task.exception?.message
                        }
                    }
            } catch (e: ApiException) {
                Log.w(ContentValues.TAG, "Google sign in failed", e)
                error = "Google sign in failed"
            }
        }
    )

    if (showForgotPasswordDialog) {
        var resetEmail by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Forgot Password") },
            text = {
                Column {
                    Text("Enter your email to reset your password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(resetEmail)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    showForgotPasswordDialog = false
                                } else {
                                    error = task.exception?.message
                                }
                            }
                    }
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                Button(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


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
                Button(
                    onClick = { /* Current Screen */ },
                    shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Login")
                }
                OutlinedButton(
                    onClick = onRegisterClick,
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Sign Up", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            FormField(label = "Email", textState = email, onTextField = { email = it })
            FormField(
                label = "Password",
                textState = password,
                onTextField = { password = it },
                isPasswordField = true,
                passwordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible }
            )
            Spacer(modifier = Modifier.height(32.dp))

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Email and password are required."
                        return@Button
                    }
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginSuccess()
                            } else {
                                error = task.exception?.message
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Login")
            }
            TextButton(onClick = { showForgotPasswordDialog = true }) {
                Text(text = "Forgot Password?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR",modifier = Modifier.padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
fun LoginScreenPreview() {
    Surface {
        LoginScreen(onRegisterClick = {}, onLoginSuccess = {})
    }
}
