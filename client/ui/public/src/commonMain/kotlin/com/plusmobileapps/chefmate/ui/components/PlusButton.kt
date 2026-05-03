package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

enum class PlusButtonVariant {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE,
}

@Composable
fun PlusButton(
    text: TextData,
    variant: PlusButtonVariant = PlusButtonVariant.PRIMARY,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = !isLoading,
        colors =
            when (variant) {
                PlusButtonVariant.PRIMARY ->
                    ButtonDefaults.buttonColors(
                        containerColor = ChefMateTheme.colorScheme.primary,
                        contentColor = ChefMateTheme.colorScheme.onPrimary,
                    )
                PlusButtonVariant.SECONDARY ->
                    ButtonDefaults.buttonColors(
                        containerColor = ChefMateTheme.colorScheme.secondaryContainer,
                        contentColor = ChefMateTheme.colorScheme.onSecondaryContainer,
                    )
                PlusButtonVariant.DESTRUCTIVE ->
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    )
            },
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
        } else {
            Text(text.localized())
        }
    }
}
