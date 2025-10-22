package com.example.cropdoctor.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    object SignedOut : AuthUiState
    data class InProgress(val message: String) : AuthUiState
    data class Success(val isNewUser: Boolean) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.SignedOut)
    val uiState = _uiState.asStateFlow()

    init {
        if (auth.currentUser != null) {
            _uiState.value = AuthUiState.Success(false)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.InProgress("Signing in...")
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = AuthUiState.Success(false)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign-in failed.")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.InProgress("Creating account...")
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user!!
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates).await()
                _uiState.value = AuthUiState.Success(true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign-up failed.")
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.InProgress("Signing in with Google...")
            try {
                val result = auth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Success(result.additionalUserInfo?.isNewUser == true)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google sign-in failed.")
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                // You might want to add a state to indicate success
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Password reset failed.")
            }
        }
    }

    fun dismissError() {
        _uiState.value = AuthUiState.SignedOut
    }
}
