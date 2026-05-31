package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.plusmobileapps.chefmate.ui.LocalSecondaryAnimatedVisibilityScope
import com.plusmobileapps.chefmate.ui.sharedElementBy

@Composable
fun RecipeImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    secondarySharedElementKey: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val secondarySharedModifier =
        Modifier.sharedElementBy(
            key = secondarySharedElementKey,
            animatedVisibilityScope = LocalSecondaryAnimatedVisibilityScope.current,
        )
    val clickModifier =
        if (imageUrl != null && onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier =
                modifier
                    .then(secondarySharedModifier)
                    .clip(MaterialTheme.shapes.medium)
                    .then(clickModifier),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier =
                modifier
                    .then(secondarySharedModifier)
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
