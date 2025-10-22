package com.example.cropdoctor.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A composable screen that displays the user's diagnosis history.
 *
 * @param navController The NavController for navigating between screens.
 * @param historyViewModel The view model for the diagnosis history.
 * @param historyResultViewModel The view model for the history result.
 * @param onMenuClick A callback to be invoked when the menu icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    historyViewModel: HistoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    historyResultViewModel: HistoryResultViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by historyViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<DiagnosisHistory?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        historyViewModel.fetchHistory()
    }

    if (showDeleteDialog != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog?.let { historyViewModel.deleteHistory(it) }
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Diagnosis History", onMenuClick = onMenuClick, onProfileClick = { navController.navigate(Screen.Profile.route) }) }
    ) { paddingValues ->
        when (val state = uiState) {
            is HistoryUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HistoryUiState.Success -> {
                state.userMessage?.let {
                    LaunchedEffect(it) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        historyViewModel.userMessageShown()
                    }
                }
                if (state.history.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No diagnosis history found.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(paddingValues), contentPadding = PaddingValues(16.dp)) {
                        items(state.history) { item ->
                            HistoryItemCard(
                                item = item,
                                onClick = { historyResultViewModel.viewHistoryItem(navController, item) },
                                onDeleteClick = { showDeleteDialog = item }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
            is HistoryUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: DiagnosisHistory, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUri,
                contentDescription = item.diseaseName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(text = item.diseaseName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = item.plantName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete History", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete History") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
