package com.example.cropdoctor.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.cropdoctor.BuildConfig
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.domain.PlantClassifier
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * An implementation that uses the Gemini API to analyze a plant image.
 */
class LlmPlantClassifier : PlantClassifier {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    override suspend fun classify(bitmap: Bitmap, rotationDegrees: Int): List<DiagnosisResult> {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                You are an expert in plant pathology. Analyze the provided image of a plant leaf and identify the most likely disease.

                Your response MUST be a single, clean JSON object and nothing else. Do not include any introductory text, closing text, or markdown formatting like ```json.

                The JSON object must have the following structure:
                {
                  "plantName": "<The common name of the plant>",
                  "scientificName": "<The scientific name of the plant>",
                  "disease": "<The name of the disease found, or 'Healthy' if no disease is detected>",
                  "confidence": <A float value between 0.0 and 1.0 representing your confidence>,
                  "description": "<A detailed, paragraph-long description of the disease symptoms>",
                  "diseaseType": "<The type of disease, e.g., Fungal, Bacterial, Viral, or N/A if healthy>",
                  "treatment": ["<A list of detailed treatment steps>"],
                  "prevention": ["<A list of detailed prevention steps>"]
                }

                If the image does not clearly show a plant leaf or the quality is too poor to make a diagnosis, return a JSON object with the disease set to "Unknown" and a low confidence score.
                """

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                val response = generativeModel.generateContent(inputContent)

                val responseJson = response.text ?: ""
                val jsonObject = JSONObject(responseJson)

                val result = DiagnosisResult(
                    imageUri = Uri.EMPTY, // Will be replaced by the ViewModel
                    plantName = jsonObject.getString("plantName"),
                    scientificName = jsonObject.getString("scientificName"),
                    disease = jsonObject.getString("disease"),
                    confidence = jsonObject.getDouble("confidence").toFloat(),
                    description = jsonObject.getString("description"),
                    diseaseType = jsonObject.getString("diseaseType"),
                    treatment = jsonObject.getJSONArray("treatment").let { 0.until(it.length()).map(it::getString) },
                    prevention = jsonObject.getJSONArray("prevention").let { 0.until(it.length()).map(it::getString) }
                )

                if (result.confidence >= 0.5f) {
                    listOf(result)
                } else {
                    emptyList()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}