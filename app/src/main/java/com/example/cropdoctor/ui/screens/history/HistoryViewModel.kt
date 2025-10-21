package com.example.cropdoctor.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

// This now includes all fields from the Firestore document
data class DiagnosisHistory(
    val id: String = "", // Add document ID
    val userId: String = "",
    val diseaseName: String = "",
    val plantName: String = "",
    val scientificName: String = "",
    val confidence: Float = 0f,
    val timestamp: Date = Date(),
    val imageUri: String = "",
    val description: String = "",
    val treatment: List<String> = emptyList(),
    val prevention: List<String> = emptyList(),
    val diseaseType: String = ""
)

class HistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _history = MutableStateFlow<List<DiagnosisHistory>>(emptyList())
    val history = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = auth.currentUser?.uid ?: return@launch

            try {
                val snapshot = db.collection("diagnosis_history")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                _history.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(DiagnosisHistory::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHistory(item: DiagnosisHistory) {
        viewModelScope.launch {
            try {
                db.collection("diagnosis_history").document(item.id).delete().await()
                fetchHistory() // Refresh the list after deletion
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
