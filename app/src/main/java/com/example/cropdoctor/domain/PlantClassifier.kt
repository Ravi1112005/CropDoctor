package com.example.cropdoctor.domain

import android.graphics.Bitmap

/**
 * An interface that defines the contract for a plant disease classifier.
 * This allows for different ML models or frameworks to be used interchangeably.
 */
interface PlantClassifier {

    /**
     * Analyzes a given image of a plant leaf and returns a list of possible diagnoses.
     *
     * @param bitmap The input image of a plant leaf as a [Bitmap].
     * @param rotationDegrees The rotation of the image that needs to be corrected before inference.
     * @return A list of [DiagnosisResult] objects, sorted by confidence in descending order.
     */
    suspend fun classify(bitmap: Bitmap, rotationDegrees: Int): List<DiagnosisResult>
}
