package com.example.cropdoctor.ui.screens.history

import androidx.lifecycle.ViewModel
import com.example.cropdoctor.domain.DiagnosisResult
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

class HistoryResultViewModel : ViewModel() {
    var historyItem: DiagnosisResult? = null

    fun setHistoryData(data: DiagnosisResult) {
        historyItem = data
    }

    // Helper functions to encode/decode the data for navigation
    fun encodeResult(result: DiagnosisResult): String {
        val json = Gson().toJson(result)
        return URLEncoder.encode(json, "UTF-8")
    }

    fun decodeResult(json: String): DiagnosisResult {
        val decodedJson = URLDecoder.decode(json, "UTF-8")
        return Gson().fromJson(decodedJson, DiagnosisResult::class.java)
    }
}
