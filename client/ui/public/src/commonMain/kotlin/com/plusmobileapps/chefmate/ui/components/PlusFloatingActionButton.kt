package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun PlusFloatingActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    icon: Painter? = null,
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        shape = ChefMateTheme.shapes.extraLarge,
    ) {
        Text(text = text)
        if (icon != null) {
            Spacer(Modifier.width(ChefMateTheme.dimens.paddingNormal))
            Icon(
                painter = icon,
                contentDescription = null,
            )
        }
    }
}