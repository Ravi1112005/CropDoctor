package com.example.cropdoctor.ui.screens.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.navigation.Screen

class HistoryResultViewModel : ViewModel() {
    var historyItem by mutableStateOf<DiagnosisResult?>(null)
        private set

    fun setHistoryData(result: DiagnosisResult) {
        historyItem = result
    }

    fun viewHistoryItem(navController: NavController, item: DiagnosisHistory) {
        val result = DiagnosisResult(
            plantName = item.plantName,
            scientificName = item.scientificName,
            disease = item.diseaseName,
            confidence = item.confidence,
            description = item.description,
            treatment = item.treatment,
            prevention = item.prevention,
            imageUri = item.imageUri.toUri(),
            diseaseType = item.diseaseType
        )
        setHistoryData(result)
        navController.navigate(Screen.HistoryResult.route)
    }
}
