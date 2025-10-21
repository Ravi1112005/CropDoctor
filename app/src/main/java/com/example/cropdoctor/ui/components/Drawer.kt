package com.example.cropdoctor.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctor.navigation.Screen
import com.example.cropdoctor.navigation.drawerItems
import com.example.cropdoctor.ui.theme.CropDoctorTheme
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(user: FirebaseUser?, onItemClick: (Screen) -> Unit) {
    ModalDrawerSheet {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(Screen.Profile) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    photoUri = user?.photoUrl,
                    name = user?.displayName,
                    modifier = Modifier.size(64.dp),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = user?.displayName ?: "Ravi Sharma", fontWeight = FontWeight.Bold)
                    Text(text = user?.email ?: "", fontSize = 12.sp)
                }
            }
            Divider()

            // Menu Items
            drawerItems.forEach { screen ->
                NavigationDrawerItem(
                    icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                    label = { Text(screen.title) },
                    selected = false,
                    onClick = { onItemClick(screen) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawerContentPreview() {
    CropDoctorTheme {
        DrawerContent(user = null, onItemClick = {})
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DrawerContentDarkPreview() {
    CropDoctorTheme {
        DrawerContent(user = null, onItemClick = {})
    }
}
