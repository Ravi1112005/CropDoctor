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

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val history: List<DiagnosisHistory>, val userMessage: String? = null) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

class HistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser == null) {
            _uiState.value = HistoryUiState.Success(emptyList())
        } else {
            fetchHistory()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val snapshot = db.collection("diagnosis_history")
                        .whereEqualTo("userId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val historyList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DiagnosisHistory::class.java)?.copy(id = doc.id)
                    }
                    _uiState.value = HistoryUiState.Success(historyList)
                } else {
                    _uiState.value = HistoryUiState.Success(emptyList())
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error("Failed to fetch history: ${e.message}")
            }
        }
    }

    fun deleteHistory(item: DiagnosisHistory) {
        viewModelScope.launch {
            try {
                db.collection("diagnosis_history").document(item.id).delete().await()
                fetchHistory() // Refresh the list after deletion
            } catch (e: Exception) {
                val currentState = _uiState.value
                if (currentState is HistoryUiState.Success) {
                    _uiState.value = currentState.copy(userMessage = "Failed to delete item: ${e.message}")
                }
            }
        }
    }

    fun userMessageShown() {
        val currentState = _uiState.value
        if (currentState is HistoryUiState.Success) {
            _uiState.value = currentState.copy(userMessage = null)
        }
    }
}
