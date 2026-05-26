package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.core.impl.detail.ui.RecipeCategoriesRow
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Snapshot coverage for the chip row rendered on the recipe detail screen. Built-in presets
// are resolved through pickerLabelRes(); user-created rows fall back to category.name.

private val mixedCategories =
    setOf(
        Category(id = 1L, name = "Dinner", builtinId = BuiltinCategory.DINNER.id),
        Category(id = 100L, name = "Weeknight"),
    )

private val manyCategories =
    setOf(
        Category(id = 1L, name = "Breakfast", builtinId = BuiltinCategory.BREAKFAST.id),
        Category(id = 2L, name = "Lunch", builtinId = BuiltinCategory.LUNCH.id),
        Category(id = 3L, name = "Dinner", builtinId = BuiltinCategory.DINNER.id),
        Category(id = 100L, name = "Family Favorite"),
        Category(id = 101L, name = "Weeknight"),
        Category(id = 102L, name = "Slow Cooker"),
    )

@PreviewTest
@Preview(showBackground = true)
@Composable
fun RecipeCategoriesRowMixedLightScreenshot() {
    ChefMateTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RecipeCategoriesRow(
                categories = mixedCategories,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecipeCategoriesRowMixedDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RecipeCategoriesRow(
                categories = mixedCategories,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 360)
@Composable
fun RecipeCategoriesRowManyWrappingScreenshot() {
    ChefMateTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            RecipeCategoriesRow(
                categories = manyCategories,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
