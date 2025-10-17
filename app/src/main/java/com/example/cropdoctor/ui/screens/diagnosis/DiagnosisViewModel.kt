package com.example.cropdoctor.ui.screens.diagnosis

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropdoctor.data.FakePlantClassifier
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
    // We hold the full list, the UI can decide to show one or more.
    data class Success(val diagnosisResults: List<DiagnosisResult>) : DiagnosisUiState
    data class Error(val message: String) : DiagnosisUiState
}

class DiagnosisViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // In a real app, this would be injected using a dependency injection framework like Hilt.
    private val plantClassifier: PlantClassifier = FakePlantClassifier()

    // A simple cache to hold the latest results.
    private var latestResults: Pair<Uri, List<DiagnosisResult>>? = null


    /**
     * Starts the analysis of a given image URI.
     */
    fun analyzeImage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.value = DiagnosisUiState.Loading
            try {
                val bitmap = uriToBitmap(uri, contentResolver)
                if (bitmap == null) {
                    _uiState.value = DiagnosisUiState.Error("Could not process the selected image.")
                    return@launch
                }

                // Run classification. The fake classifier is synchronous, but a real one might not be.
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

    /**
     * A simple method to retrieve results if you navigate away and back.
     * In a more complex app, you might persist this state differently.
     */
    fun getResultForUri(uri: Uri): List<DiagnosisResult>? {
        return if (latestResults?.first == uri) {
            latestResults?.second
        } else {
            null
        }
    }


    /**
     * Resets the UI state back to Idle.
     */
    fun resetState() {
        _uiState.value = DiagnosisUiState.Idle
    }

    /**
     * Converts a URI to a Bitmap.
     */
    private fun uriToBitmap(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}