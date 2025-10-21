package com.example.cropdoctor.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent

@Composable
fun ProfileAvatar(
    photoUri: Uri?,
    name: String?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp
) {
    SubcomposeAsyncImage(
        model = photoUri,
        contentDescription = "User Avatar",
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
            is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Empty -> {
                InitialsAvatar(name = name, modifier = Modifier.fillMaxSize(), fontSize = fontSize)
            }
            else -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Composable
fun InitialsAvatar(name: String?, modifier: Modifier = Modifier, fontSize: TextUnit) {
    val initials = name?.split(" ")?.filter { it.isNotBlank() }?.take(2)?.map { it.first() }?.joinToString("")?.uppercase() ?: "CD"
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
