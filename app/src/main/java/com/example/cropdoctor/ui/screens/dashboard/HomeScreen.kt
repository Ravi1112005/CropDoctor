package com.example.cropdoctor.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cropdoctor.R
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.ui.components.AppTopBar
import com.example.cropdoctor.ui.screens.history.DiagnosisHistory
import com.example.cropdoctor.ui.screens.history.HistoryResultViewModel
import com.example.cropdoctor.ui.screens.history.HistoryUiState
import com.example.cropdoctor.ui.screens.history.HistoryViewModel
import com.example.cropdoctor.ui.theme.Green
import com.example.cropdoctor.ui.theme.LimeGreen
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A composable screen that serves as the main dashboard of the app.
 *
 * @param navController The NavController for navigating between screens.
 * @param onMenuClick A callback to be invoked when the menu icon is clicked.
 * @param historyViewModel The view model for the diagnosis history.
 * @param historyResultViewModel The view model for the history result.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onMenuClick: () -> Unit,
    historyViewModel: HistoryViewModel = viewModel(),
    historyResultViewModel: HistoryResultViewModel
) {
    Scaffold(
        topBar = {
            AppTopBar(
                onMenuClick = onMenuClick,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }
    ) { paddingValues ->
        HomeScreenContent(
            paddingValues = paddingValues,
            navController = navController,
            historyViewModel = historyViewModel,
            historyResultViewModel = historyResultViewModel
        )
    }
}

@Composable
fun HomeScreenContent(
    paddingValues: PaddingValues,
    navController: NavController,
    historyViewModel: HistoryViewModel,
    historyResultViewModel: HistoryResultViewModel
) {
    val historyUiState by historyViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        historyViewModel.fetchHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(75.dp))
        PrimaryActionCard(navController = navController)
        Spacer(modifier = Modifier.height(75.dp))

        if (historyUiState is HistoryUiState.Success && (historyUiState as HistoryUiState.Success).history.isNotEmpty()) {
            RecentScansSection(navController, historyViewModel, historyResultViewModel)
        }
    }
}

@Composable
fun PrimaryActionCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { navController.navigate(Screen.ScanCrop.route) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(LimeGreen, Green),
                        end = Offset(0f, Float.POSITIVE_INFINITY),
                        start = Offset(0f, 0f)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Identify Disease\nin Seconds",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 75.dp)
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera_filled),
                            contentDescription = "Camera Icon",
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF388E3C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentScansSection(
    navController: NavController,
    historyViewModel: HistoryViewModel,
    historyResultViewModel: HistoryResultViewModel
) {
    val historyUiState by historyViewModel.uiState.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recent Scans",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(onClick = { navController.navigate(Screen.History.route) }) {
            Text("View All")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    when (val state = historyUiState) {
        is HistoryUiState.Loading -> CircularProgressIndicator()
        is HistoryUiState.Success -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.history.take(3)) { item ->
                    RecentScanItem(item) { historyResultViewModel.viewHistoryItem(navController, item) }
                }
            }
        }
        is HistoryUiState.Error -> Text(state.message)
    }
}

@Composable
fun RecentScanItem(item: DiagnosisHistory, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUri,
                contentDescription = item.diseaseName,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.diseaseName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.plantName} - ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
