package com.example.cropdoctor.ui.screens.dashboard

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow(auth.currentUser)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = auth.currentUser ?: return@launch

            try {
                val dir = File(context.filesDir, "profile_images")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val destinationFile = File(dir, "${user.uid}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val localUri = Uri.fromFile(destinationFile)
                val profileUpdates = userProfileChangeRequest {
                    photoUri = localUri
                }
                user.updateProfile(profileUpdates).await()
                _user.value = auth.currentUser // Refresh user state
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeProfileImage(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = auth.currentUser ?: return@launch
            try {
                user.photoUrl?.path?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                    }
                }

                val profileUpdates = userProfileChangeRequest {
                    photoUri = null
                }
                user.updateProfile(profileUpdates).await()
                _user.value = auth.currentUser // Refresh user state
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to remove image: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfileName(name: String, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = auth.currentUser ?: return@launch
            try {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates).await()
                _user.value = auth.currentUser // Refresh user state
                Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to update name: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
