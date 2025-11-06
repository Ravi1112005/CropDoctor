package com.example.cropdoctor.ui.screens.diagnosis

import android.content.ContentResolver
import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropdoctor.data.LlmPlantClassifier
import com.example.cropdoctor.domain.DiagnosisResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID

sealed class DiagnosisUiState {
    object Idle : DiagnosisUiState()
    object Loading : DiagnosisUiState()
    data class Success(val diagnosisResults: List<DiagnosisResult>) : DiagnosisUiState()
    data class Error(val message: String) : DiagnosisUiState()
}

class DiagnosisViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DiagnosisUiState>(DiagnosisUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val classifier = LlmPlantClassifier()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun analyzeImage(uri: Uri, contentResolver: ContentResolver) {
        _uiState.value = DiagnosisUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }

                val results = classifier.classify(bitmap, 0).map {
                    // Replace the empty Uri from the classifier with the actual image Uri
                    it.copy(imageUri = uri)
                }
                _uiState.value = DiagnosisUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = DiagnosisUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveDiagnosisToHistory(result: DiagnosisResult, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@launch

            try {
                // 1. Copy image to internal storage to get a permanent URI
                val dir = File(context.filesDir, "history_images")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val destinationFile = File(dir, "${UUID.randomUUID()}.jpg")

                context.contentResolver.openInputStream(result.imageUri)?.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val permanentUri = Uri.fromFile(destinationFile)

                // 2. Create history entry with the new, permanent URI
                val historyEntry = hashMapOf(
                    "userId" to userId,
                    "diseaseName" to result.disease,
                    "plantName" to result.plantName,
                    "confidence" to result.confidence,
                    "timestamp" to Date(),
                    "imageUri" to permanentUri.toString(), // Save the permanent URI
                    "description" to result.description,
                    "treatment" to result.treatment,
                    "prevention" to result.prevention
                )

                // 3. Save to Firestore
                db.collection("diagnosis_history").add(historyEntry)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _uiState.value = DiagnosisUiState.Idle
    }
}
