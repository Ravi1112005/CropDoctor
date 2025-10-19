package com.example.cropdoctor.ui.screens.diagnosis

import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropdoctor.data.LlmPlantClassifier
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.domain.PlantClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the different states the Diagnosis screen can be in.
 */
sealed interface DiagnosisUiState {
    object Idle : DiagnosisUiState
    object Loading : DiagnosisUiState
    data class Success(val diagnosisResults: List<DiagnosisResult>) : DiagnosisUiState
    data class Error(val message: String) : DiagnosisUiState
}

class DiagnosisViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Switch to the new LlmPlantClassifier
    private val plantClassifier: PlantClassifier = LlmPlantClassifier()

    private var latestResults: Pair<Uri, List<DiagnosisResult>>? = null

    fun analyzeImage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.value = DiagnosisUiState.Loading
            try {
                val bitmap = uriToBitmap(uri, contentResolver)
                if (bitmap == null) {
                    _uiState.value = DiagnosisUiState.Error("Could not process the selected image.")
                    return@launch
                }

                // The classifier is now a suspend function, so it will run asynchronously.
                val results = plantClassifier.classify(bitmap, 0)
                    .map { it.copy(imageUri = uri) } // Ensure the result has the correct URI

                latestResults = uri to results
                _uiState.value = DiagnosisUiState.Success(results)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DiagnosisUiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun getResultForUri(uri: Uri): List<DiagnosisResult>? {
        return if (latestResults?.first == uri) {
            latestResults?.second
        } else {
            null
        }
    }

    fun resetState() {
        _uiState.value = DiagnosisUiState.Idle
    }

    private fun uriToBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        return try {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
