package com.plusmobileapps.chefmate.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
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
        Text(text.localized())
    }
}
