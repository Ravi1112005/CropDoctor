package com.example.cropdoctor.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Theme {
    SYSTEM, LIGHT, DARK
}

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM
)

/**
 * ViewModel for the Settings screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /**
     * Sets the theme preference.
     */
    fun setTheme(theme: Theme) {
        _uiState.update { it.copy(theme = theme) }
    }

    /**
     * Clears the application's cache.
     */
    fun clearCache() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            context.cacheDir.deleteRecursively()
        }
    }
}
