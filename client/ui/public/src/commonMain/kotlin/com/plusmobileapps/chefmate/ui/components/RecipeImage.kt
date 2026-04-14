package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun RecipeImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
