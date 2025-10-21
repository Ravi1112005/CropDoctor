package com.example.cropdoctor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctor.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String? = null,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    CenterAlignedTopAppBar(
        title = {
            if (title != null) {
                Text(title)
            } else {
                Image(
                    painter = painterResource(
                        id = if (isSystemInDarkTheme()) R.drawable.ic_cropdoctor_logo else R.drawable.ic_cropdoctor_logo_green
                    ),
                    contentDescription = "CropDoctor Logo",
                    modifier = Modifier.height(42.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                ProfileAvatar(
                    photoUri = user?.photoUrl,
                    name = user?.displayName,
                    modifier = Modifier.size(40.dp),
                    fontSize = 16.sp
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}
