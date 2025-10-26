package com.example.cropdoctor.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cropdoctor.R
import com.example.cropdoctor.ui.components.FormField
import com.example.cropdoctor.ui.components.GoogleSignInButton
import com.google.firebase.auth.GoogleAuthProvider

/**
 * A composable screen for user login.
 *
 * @param onRegisterClick A callback to be invoked when the "Register" button is clicked.
 * @param onLoginSuccess A callback to be invoked when the user successfully logs in.
 * @param authViewModel The view model for authentication.
 */
@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

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
                        if (resetEmail.isNotBlank()) {
                            authViewModel.sendPasswordReset(resetEmail)
                            showForgotPasswordDialog = false
                            Toast.makeText(context, "Password reset link sent to $resetEmail", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
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

            if (authState is AuthUiState.InProgress) {
                Text((authState as AuthUiState.InProgress).message)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email and password are required.", Toast.LENGTH_SHORT).show()
                    } else {
                        authViewModel.signIn(email, password)
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
                GoogleSignInButton(
                    onSuccess = { idToken -> authViewModel.signInWithGoogle(GoogleAuthProvider.getCredential(idToken, null)) },
                    onError = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}