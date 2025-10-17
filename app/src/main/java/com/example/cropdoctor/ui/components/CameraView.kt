package com.example.cropdoctor.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.cropdoctor.R
import java.io.File
import java.util.concurrent.Executor

typealias OnImageCaptured = (Uri) -> Unit

@Composable
fun CameraView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onImageCaptured: OnImageCaptured,
    onGalleryClicked: () -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Live camera preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Error binding camera: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }, cameraExecutor)
                previewView
            }
        )

        // UI Overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_cropdoctor_logo),
                    contentDescription = "CropDoctor Logo",
                    modifier = Modifier.height(36.dp)
                )
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button
                IconButton(onClick = onGalleryClicked) {
                    Icon(painter = painterResource(id = R.drawable.ic_gallery), contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                // Capture Button
                IconButton(
                    onClick = { takePhoto(context, imageCapture, cameraExecutor, onImageCaptured) },
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, Color(0xFFB9E4C3), CircleShape) // Light green border
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_camera_filled), contentDescription = "Take Photo", tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp)) // Dark green icon
                }

                // Spacer to balance the layout
                Box(modifier = Modifier.size(40.dp))
            }
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: OnImageCaptured
) {
    val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }
        }
    )
}
