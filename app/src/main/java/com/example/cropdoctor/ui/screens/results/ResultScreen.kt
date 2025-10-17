package com.example.cropdoctor.ui.screens.results

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cropdoctor.R
import com.example.cropdoctor.domain.DiagnosisResult
import com.example.cropdoctor.ui.components.shimmerBackground
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisUiState
import com.example.cropdoctor.ui.screens.diagnosis.DiagnosisViewModel
import com.example.cropdoctor.ui.theme.LightGreen

@Composable
fun ResultScreen(
    imageUri: Uri,
    viewModel: DiagnosisViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(imageUri) {
        viewModel.analyzeImage(imageUri, context.contentResolver)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    when (val state = uiState) {
        is DiagnosisUiState.Loading,
        is DiagnosisUiState.Idle -> {
            ResultShimmerScreen()
        }
        is DiagnosisUiState.Success -> {
            ResultSuccessScreen(result = state.diagnosisResults.first(), onNavigateBack = onNavigateBack)
        }
        is DiagnosisUiState.Error -> {
            ErrorScreen(message = state.message, onRetry = onNavigateBack)
        }
    }
}

@Composable
private fun ResultShimmerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGreen)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(48.dp)) // Placeholder for TopAppBar

        // Shimmer for DiagnosisCard
        Card(modifier = Modifier.fillMaxWidth().height(112.dp)) {
            Box(modifier = Modifier.fillMaxSize().shimmerBackground())
        }
        Spacer(Modifier.height(16.dp))

        // Shimmer for InfoCards
        repeat(3) {
            Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                Box(modifier = Modifier.fillMaxSize().shimmerBackground())
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Analysis Failed", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(message, textAlign = TextAlign.Center)
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
    result: DiagnosisResult,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(LightGreen),
        bottomBar = {
            BottomBar(onNewScan = onNavigateBack)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGreen)
                .padding(it)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopAppBar()
            Spacer(Modifier.height(16.dp))
            DiagnosisCard(result)
            Spacer(Modifier.height(16.dp))

            val aboutItems = listOf(
                "✔" to "Symptoms: ${result.description}",
                "✔" to "Disease Type: ${result.diseaseType}"
            )
            InfoCard(title = "About the Disease", items = aboutItems)
            Spacer(Modifier.height(16.dp))

            val treatmentItems = result.treatment.mapIndexed { index, treatment ->
                (index + 1).toString() to treatment
            }
            InfoCard(title = "Recommended Treatment", items = treatmentItems)
            Spacer(Modifier.height(16.dp))

            val preventionItems = result.prevention.map { "✔" to it }
            InfoCard(title = "Prevention", items = preventionItems)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_menu), contentDescription = "Menu")
        Image(
            painter = painterResource(id = R.drawable.ic_cropdoctor_logo),
            contentDescription = "CropDoctor Logo",
            modifier = Modifier.height(32.dp)
        )
        Icon(painter = painterResource(id = R.drawable.ic_notification), contentDescription = "Notifications")
    }
}

@Composable
private fun DiagnosisCard(result: DiagnosisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
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
                Text(text = "${result.disease} Detected!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "${(result.confidence * 100).toInt()}% Confidence", color = Color.Gray)
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, items: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            items.forEach { (prefix, text) ->
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = prefix, modifier = Modifier.padding(end = 8.dp))
                    Text(text = text)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun BottomBar(onNewScan: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onNewScan, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(painter = painterResource(id = R.drawable.ic_camera_filled), contentDescription = "New Scan")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("New Scan")
            }
            Button(onClick = { /* TODO: Share */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()) {
                Icon(painter = painterResource(id = R.drawable.ic_share), contentDescription = "Share Report", tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Share Report", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
