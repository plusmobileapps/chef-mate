package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun PlusFloatingActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    icon: Painter? = null,
) {
    ExtendedFloatingActionButton(
        // Ride up so a shown snackbar never covers the FAB.
        modifier = modifier.padding(bottom = LocalSnackbarInset.current),
        onClick = onClick,
        shape = ChefMateTheme.shapes.extraLarge,
    ) {
        Text(text = text)
        if (icon != null) {
            Spacer(Modifier.width(ChefMateTheme.dimens.paddingNormal))
            Icon(painter = icon, contentDescription = null)
        }
    }
}

@Composable
fun PlusFloatingActionButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    ExtendedFloatingActionButton(
        // Ride up so a shown snackbar never covers the FAB.
        modifier = modifier.padding(bottom = LocalSnackbarInset.current),
        onClick = onClick,
        shape = ChefMateTheme.shapes.extraLarge,
    ) {
        text?.let { Text(text = text) }
        if (icon != null) {
            Spacer(Modifier.width(ChefMateTheme.dimens.paddingNormal))
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}
