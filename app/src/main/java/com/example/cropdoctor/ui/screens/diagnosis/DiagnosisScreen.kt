package com.example.cropdoctor.ui.screens.diagnosis

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.cropdoctor.ui.components.CameraView

@Composable
fun DiagnosisScreen(
    viewModel: DiagnosisViewModel,
    onNavigateToResult: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to use the camera.", Toast.LENGTH_LONG).show()
        }
    }

    // Request permission when the composable is first launched.
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // React to state changes from the ViewModel
    when (val state = uiState) {
        is DiagnosisUiState.Success -> {
            // Analysis is complete, trigger navigation
            val resultUri = state.diagnosisResults.firstOrNull()?.imageUri
            if (resultUri != null) {
                onNavigateToResult(resultUri)
                viewModel.resetState() // Reset state after navigation
            }
        }
        is DiagnosisUiState.Error -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.resetState() // Allow user to try again
        }
        else -> Unit // Idle or Loading
    }


    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraView(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onImageCaptured = { uri ->
                    // Start analysis when a new image is captured.
                    viewModel.analyzeImage(uri, context.contentResolver)
                },
                onGalleryClicked = {
                    // TODO: Implement gallery picker
                    Toast.makeText(context, "Gallery feature coming soon!", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Show a message if permission is not granted.
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Camera permission needed to scan for diseases.")
            }
        }

        // Show a loading indicator when the model is processing.
        if (uiState is DiagnosisUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}