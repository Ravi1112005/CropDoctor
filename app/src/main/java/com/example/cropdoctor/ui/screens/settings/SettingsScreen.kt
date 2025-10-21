package com.example.cropdoctor.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeChooserDialog(
            currentTheme = uiState.theme,
            onThemeSelected = { viewModel.setTheme(it) },
            onDismiss = { showThemeDialog = false }
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = Screen.Settings.title, onMenuClick = onMenuClick, onProfileClick = {}) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ThemeSettingItem { showThemeDialog = true }
            Spacer(modifier = Modifier.height(16.dp))
            CacheSettingItem(onClearCache = {
                viewModel.clearCache()
                Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show()
            })
            Spacer(modifier = Modifier.height(16.dp))
            AboutSettingItem()
        }
    }
}

@Composable
fun ThemeSettingItem(onClick: () -> Unit) {
    SettingItemCard(
        icon = Icons.Default.WbSunny,
        title = "Theme",
        onClick = onClick
    )
}

@Composable
fun ThemeChooserDialog(currentTheme: Theme, onThemeSelected: (Theme) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                Theme.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = theme.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun CacheSettingItem(onClearCache: () -> Unit) {
    SettingItemCard(
        icon = Icons.Default.Delete,
        title = "Clear Cache",
        onClick = onClearCache
    )
}

@Composable
fun AboutSettingItem() {
    SettingItemCard(
        icon = Icons.Default.Info,
        title = "About",
        onClick = {}
    )
}

@Composable
fun SettingItemCard(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.SemiBold)
            }
            if (content != null) {
                content()
            }
        }
    }
}
