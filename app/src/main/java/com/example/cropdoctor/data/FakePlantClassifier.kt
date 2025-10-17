package com.example.cropdoctor.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.domain.PlantClassifier

/**
 * A fake implementation of the [PlantClassifier] for testing and UI development.
 * It returns a static, predefined diagnosis result regardless of the input.
 */
class FakePlantClassifier : PlantClassifier {

    override fun classify(bitmap: Bitmap, rotationDegrees: Int): List<DiagnosisResult> {
        // This fake classifier returns a hardcoded result for demonstration that matches the new UI design.
        return listOf(
            DiagnosisResult(
                imageUri = Uri.EMPTY, // The ViewModel will replace this with the actual URI
                plantName = "Tomato",
                scientificName = "Solanum lycopersicum",
                disease = "Late Blight",
                confidence = 0.94f,
                description = "Dark, water-soaked spots on leaves, rapid browning.",
                diseaseType = "Oomycete (Fungal-like organism)",
                treatment = listOf(
                    "Prune affected leaves & destroy.",
                    "Apply organic copper fungicide weekly.",
                    "Improve air circulation.",
                    "Avoid overhead watering."
                ),
                prevention = listOf(
                    "Use disease-resistant varieties.",
                    "Practice crop rotation."
                )
            )
            // You could add other potential fake results here if needed
        )
    }
}
