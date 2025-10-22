package com.example.cropdoctor.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Theme {
    SYSTEM, LIGHT, DARK
}

data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val signedOut: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setTheme(theme: Theme) {
        _uiState.update { it.copy(theme = theme) }
    }

    fun clearCache() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            context.cacheDir.deleteRecursively()
            FirebaseAuth.getInstance().signOut()
            _uiState.update { it.copy(signedOut = true) }
        }
    }
    fun onSignedOut() {
        _uiState.update { it.copy(signedOut = false) }
    }

}
