package com.example.cropdoctor.ui.screens.dashboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.ui.components.ProfileAvatar
import com.example.cropdoctor.ui.theme.CropDoctorTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * A composable screen that displays the user's profile information.
 *
 * @param navController The NavController for navigating between screens.
 * @param viewModel The view model for the profile screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user by viewModel.user.collectAsState()

    var showPhotoDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.uploadProfileImage(it, context) }
        }
    )

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Update Profile Photo") },
            text = { Text("Choose an option to update your profile photo.") },
            confirmButton = {
                TextButton(onClick = { imagePickerLauncher.launch("image/*"); showPhotoDialog = false }) {
                    Text("Upload Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.removeProfileImage(context)
                    showPhotoDialog = false
                }) {
                    Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    if (showNameDialog) {
        EditNameDialog(
            viewModel = viewModel,
            onDismiss = {
                showNameDialog = false
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Profile.route) { inclusive = true }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNameDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { 
                ProfileHeader(
                    user = user,
                    onEditClick = { showPhotoDialog = true } 
                ) 
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { SectionHeader("My Details") }
            item {
                ProfileMenuItem(
                    icon = Icons.Filled.Person,
                    text = "Personal Information",
                    onClick = { showNameDialog = true }
                )
            }
            item {
                ProfileMenuItem(
                    icon = Icons.Filled.Lock,
                    text = "Change Password",
                    onClick = {
                        auth.currentUser?.email?.let { email ->
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Password reset link sent to $email", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to send reset link: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        } ?: Toast.makeText(context, "Could not find user email.", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) }

            item {
                SignOutItem {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EditNameDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    var name by remember(user) { mutableStateOf(user?.displayName ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateProfileName(name, context)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileHeader(user: FirebaseUser?, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            ProfileAvatar(
                photoUri = user?.photoUrl,
                name = user?.displayName,
                modifier = Modifier.size(120.dp)
            )

            IconButton(onClick = onEditClick, modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile Picture")
            }
        }

        Text(text = user?.displayName ?: "Crop Doctor User", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = user?.email ?: "", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = text
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutItem(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
        leadingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out",
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CropDoctorTheme {
        ProfileScreen(rememberNavController())
    }
}
