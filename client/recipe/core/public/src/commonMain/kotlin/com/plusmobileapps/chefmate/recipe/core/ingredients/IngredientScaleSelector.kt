package com.plusmobileapps.chefmate.recipe.core.ingredients

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_scale_ingredients
import com.plusmobileapps.chefmate.recipe.data.IngredientScaler
import org.jetbrains.compose.resources.stringResource

/**
 * A compact dropdown button showing the current ingredient scale (e.g. `2×`). Tapping it opens a
 * menu of [IngredientScaler.DEFAULT_FACTORS] with the active one check-marked.
 *
 * Shared by every surface that scales a recipe's ingredients — the recipe detail screen and the
 * add-to-grocery-list sheet — so they offer the same factors and read the same way. [buttonTestTag]
 * is applied to the button itself so each screen's robot can find its own control.
 */
@Composable
internal fun IngredientScaleSelector(
    scale: Double,
    onScaleChange: (Double) -> Unit,
    buttonTestTag: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }
        TextButton(onClick = { expanded = true }, modifier = Modifier.testTag(buttonTestTag)) {
            Text(IngredientScaler.label(scale))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = stringResource(Res.string.recipe_scale_ingredients),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            IngredientScaler.DEFAULT_FACTORS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(IngredientScaler.label(option)) },
                    trailingIcon = {
                        if (option == scale) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        onScaleChange(option)
                    },
                )
            }
        }
    }
}
