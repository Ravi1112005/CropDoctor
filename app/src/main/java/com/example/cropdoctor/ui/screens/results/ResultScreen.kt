package com.example.cropdoctor.ui.screens.results

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.decodeBitmap
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.ui.components.AppTopBar
import com.example.cropdoctor.ui.components.createPdf
import com.example.cropdoctor.ui.components.shimmerBackground
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisUiState
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisViewModel
import com.example.cropdoctor.ui.screens.history.HistoryResultViewModel
import com.example.cropdoctor.ui.theme.CropDoctorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit,
    imageUri: Uri? = null, // For new scans
    viewModel: DiagnosisViewModel? = null, // For new scans
    historyResultViewModel: HistoryResultViewModel? = null, // For viewing history
) {
    val context = LocalContext.current

    if (imageUri != null && viewModel != null) {
        // --- Live Analysis Mode ---
        val uiState by viewModel.uiState.collectAsState()
        var historySaved by remember(imageUri) { mutableStateOf(false) }

        LaunchedEffect(imageUri) {
            viewModel.analyzeImage(imageUri, context.contentResolver)
        }

        LaunchedEffect(uiState) {
            val state = uiState
            if (state is DiagnosisUiState.Success && state.diagnosisResults.isNotEmpty() && !historySaved) {
                viewModel.saveDiagnosisToHistory(state.diagnosisResults.first(), context)
                historySaved = true
            }
        }

        DisposableEffect(Unit) {
            onDispose(viewModel::resetState)
        }

        Scaffold(
            topBar = { AppTopBar(onMenuClick = onMenuClick, onProfileClick = { navController.navigate(Screen.Profile.route) }) },
            bottomBar = {
                if (uiState is DiagnosisUiState.Success) {
                    val results = (uiState as DiagnosisUiState.Success).diagnosisResults
                    if (results.isNotEmpty()) {
                        BottomBar(onNewScan = onNavigateBack, onShareClick = { shareDiagnosis(context, results.first()) })
                    }
                }
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is DiagnosisUiState.Loading, is DiagnosisUiState.Idle -> ResultShimmerScreen(Modifier.padding(paddingValues))
                is DiagnosisUiState.Success -> {
                    if (state.diagnosisResults.isNotEmpty()) {
                        ResultSuccessScreen(state.diagnosisResults, Modifier.padding(paddingValues))
                    } else {
                        ErrorScreen("Could not find a matching disease.", onNavigateBack, Modifier.padding(paddingValues))
                    }
                }
                is DiagnosisUiState.Error -> ErrorScreen(state.message, onNavigateBack, Modifier.padding(paddingValues))
            }
        }
    } else if (historyResultViewModel?.historyItem != null) {
        // --- History View Mode ---
        val result = historyResultViewModel.historyItem!!
        Scaffold(
            topBar = { AppTopBar(onMenuClick = onMenuClick, onProfileClick = { navController.navigate(Screen.Profile.route) }) },
            bottomBar = { BottomBar(onNewScan = onNavigateBack, onShareClick = { shareDiagnosis(context, result) }) }
        ) { paddingValues ->
            ResultSuccessScreen(results = listOf(result), modifier = Modifier.padding(paddingValues))
        }
    } else {
        // --- Error: Invalid state ---
        ErrorScreen(message = "Could not load diagnosis data.", onRetry = onNavigateBack)
    }
}

private fun shareDiagnosis(context: Context, result: DiagnosisResult) {
    try {
        // --- CHANGE IS HERE: Load the bitmap from the Uri first ---
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, result.imageUri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, result.imageUri)
        }.copy(Bitmap.Config.ARGB_8888, false) // Ensure it's mutable if needed, and ARGB_8888

        // --- CHANGE IS HERE: Pass the loaded bitmap to createPdf ---
        val pdfFile = createPdf(context, result, bitmap)
        val pdfUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Diagnosis Report"))

    } catch(e: Exception) {
        // Handle cases where the bitmap can't be loaded
        e.printStackTrace()
        // Optionally show a Toast message to the user
    }
}

@Composable
private fun ResultShimmerScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .shimmerBackground())
        }
        Spacer(Modifier.height(16.dp))
        repeat(3) {
            Card(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .shimmerBackground())
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Analysis Failed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(message, textAlign = TextAlign.Center, color = colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun ResultSuccessScreen(
    results: List<DiagnosisResult>,
    modifier: Modifier = Modifier,
) {
    val topResult = results.first()
    val otherResults = if (results.size > 1) results.subList(1, results.size) else emptyList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        DiagnosisCard(topResult)
        Spacer(Modifier.height(16.dp))

        val aboutItems = listOf(
            "✔" to "Symptoms: ${topResult.description}",
            "✔" to "Disease Type: ${topResult.diseaseType}"
        )
        InfoCard(title = "About the Disease", items = aboutItems)
        Spacer(Modifier.height(16.dp))

        val treatmentItems = topResult.treatment.mapIndexed { index, treatment ->
            (index + 1).toString() to treatment
        }
        InfoCard(title = "Recommended Treatment", items = treatmentItems)
        Spacer(Modifier.height(16.dp))

        val preventionItems = topResult.prevention.map { "✔" to it }
        InfoCard(title = "Prevention", items = preventionItems)
        Spacer(Modifier.height(16.dp))

        if (otherResults.isNotEmpty()) {
            OtherResultsCard(otherResults)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DiagnosisCard(result: DiagnosisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = result.imageUri,
                contentDescription = "Analyzed leaf",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${result.disease} Detected!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colorScheme.onSurface)
                Text(text = "${(result.confidence * 100).toInt()}% Confidence", color = colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            items.forEach { (prefix, text) ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = prefix, modifier = Modifier.padding(end = 8.dp), color = colorScheme.onSurface)
                    Text(text = text, color = colorScheme.onSurface)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun OtherResultsCard(results: List<DiagnosisResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Other Possibilities",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            results.forEach { result ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${result.plantName} - ${result.disease}",
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${(result.confidence * 100).toInt()}%",
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BottomBar(onNewScan: () -> Unit, onShareClick: () -> Unit) {
    BottomAppBar(
        actions = {
            OutlinedButton(
                onClick = onNewScan,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DocumentScanner, contentDescription = "New Scan", modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("New Scan")
            }
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = onShareClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Report", modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Share Report")
            }
        }
    )
}