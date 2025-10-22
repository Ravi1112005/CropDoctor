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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cropdoctor.R
import com.example.cropdoctor.ui.components.FormField
import com.example.cropdoctor.ui.components.GoogleSignInButton
import com.google.firebase.auth.GoogleAuthProvider

/**
 * A composable screen for user registration.
 * @param onLoginClick A callback to be invoked when the "Login" button is clicked.
 * @param onRegisterSuccess A callback to be invoked when the user successfully registers.
 * @param authViewModel The view model for authentication.
 */
@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> onRegisterSuccess()
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                authViewModel.dismissError() // Reset state
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
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
                    Text(text = "Login", color = colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { /* Current Screen */ },
                    shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
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

            if (authState is AuthUiState.InProgress) {
                Text((authState as AuthUiState.InProgress).message)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.signUp(name, email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Text(text = "Sign Up")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text("OR", modifier = Modifier.padding(horizontal = 8.dp), color = colorScheme.onSurfaceVariant)
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
