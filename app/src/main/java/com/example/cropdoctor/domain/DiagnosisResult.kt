package com.example.cropdoctor.domain

import android.net.Uri

/**
 * A data class to hold the results of a plant health diagnosis.
 *
 * @param imageUri The URI of the image that was analyzed.
 * @param plantName The common name of the detected plant (e.g., "Tomato").
 * @param scientificName The scientific name of the plant.
 * @param disease The name of the disease identified.
 * @param confidence A value from 0.0 to 1.0 indicating the model's confidence.
 * @param description A short summary of the disease symptoms.
 * @param diseaseType The type of the disease (e.g., Fungal-like organism).
 * @param treatment Suggestions for treating the identified condition.
 * @param prevention Suggestions for preventing the disease in the future.
 */
data class DiagnosisResult(
    val imageUri: Uri,
    val plantName: String,
    val scientificName: String,
    val disease: String,
    val confidence: Float,
    val description: String,
    val diseaseType: String,
    val treatment: List<String>,
    val prevention: List<String>
)
