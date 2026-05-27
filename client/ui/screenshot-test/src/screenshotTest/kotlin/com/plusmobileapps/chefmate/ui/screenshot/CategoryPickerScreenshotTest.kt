package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.recipe.core.impl.edit.ui.CategoryPickerContent
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Snapshot coverage for the category picker sheet body. ModalBottomSheet doesn't render under
// the screenshot test plugin, so we render `CategoryPickerContent` directly. Steady states
// only — the inline create-row state machine is exercised by viewmodel unit tests.

private val sampleUserCategories =
    listOf(
        Category(id = 100L, name = "Family Favorite"),
        Category(id = 101L, name = "Weeknight"),
        Category(id = 102L, name = "Slow Cooker"),
    )

private val breakfastBuiltinRow =
    Category(id = 1L, name = "Breakfast", builtinId = BuiltinCategory.BREAKFAST.id)

@PreviewTest
@Preview(showBackground = true, heightDp = 800)
@Composable
fun CategoryPickerEmptyLightScreenshot() {
    ChefMateTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            CategoryPickerContent(
                selectedCategories = emptySet(),
                userCategories = emptyList(),
                onAttachBuiltin = {},
                onAttachCategory = {},
                onDetachCategory = {},
                onCreateUserCategory = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryPickerEmptyDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            CategoryPickerContent(
                selectedCategories = emptySet(),
                userCategories = emptyList(),
                onAttachBuiltin = {},
                onAttachCategory = {},
                onDetachCategory = {},
                onCreateUserCategory = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CategoryPickerWithUserCategoriesAndSelectionLightScreenshot() {
    ChefMateTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            CategoryPickerContent(
                selectedCategories =
                    setOf(
                        breakfastBuiltinRow,
                        sampleUserCategories[0],
                    ), // BREAKFAST + Family Favorite
                userCategories = sampleUserCategories,
                onAttachBuiltin = {},
                onAttachCategory = {},
                onDetachCategory = {},
                onCreateUserCategory = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryPickerWithUserCategoriesAndSelectionDarkScreenshot() {
    ChefMateTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            CategoryPickerContent(
                selectedCategories = setOf(breakfastBuiltinRow, sampleUserCategories[0]),
                userCategories = sampleUserCategories,
                onAttachBuiltin = {},
                onAttachCategory = {},
                onDetachCategory = {},
                onCreateUserCategory = {},
            )
        }
    }
}
