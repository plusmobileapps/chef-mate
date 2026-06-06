package com.plusmobileapps.chefmate.recipe.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CategoryItem
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CreateState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.DialogState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.Model
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

private fun bloc(model: Model): RecipeCategoriesBloc =
    object : RecipeCategoriesBloc {
        override val backHandler: BackHandler = BackDispatcher()
        override val state = MutableStateFlow(model)

        override fun onBackClicked() = Unit

        override fun onCategoryClicked(item: CategoryItem) = Unit

        override fun onCategoryLongClicked(item: CategoryItem) = Unit

        override fun onSelectModeClicked() = Unit

        override fun onCancelSelection() = Unit

        override fun onCreateClicked() = Unit

        override fun onCreateCancelled() = Unit

        override fun onCreateTextChanged(text: String) = Unit

        override fun onCreateSubmitted() = Unit

        override fun onRenameRequested(item: CategoryItem) = Unit

        override fun onRenameSubmitted(id: Long, newName: String) = Unit

        override fun onRenameDismissed() = Unit

        override fun onDeleteRequested(item: CategoryItem) = Unit

        override fun onDeleteConfirmed() = Unit

        override fun onDeleteDismissed() = Unit

        override fun onBulkDeleteRequested() = Unit

        override fun onBulkDeleteConfirmed() = Unit

        override fun onBulkDeleteDismissed() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            // Stub; screenshot tests render the impl screen directly via the public bloc above.
        }
    }

private val sampleItems: ImmutableList<CategoryItem> =
    persistentListOf(
        CategoryItem(id = 1L, name = "Family Favorites", builtinId = null, recipeCount = 12),
        CategoryItem(id = 2L, name = "Weeknight", builtinId = null, recipeCount = 3),
        CategoryItem(
            id = 5L,
            name = "Breakfast",
            builtinId = BuiltinCategory.BREAKFAST.id,
            recipeCount = 4,
        ),
        CategoryItem(
            id = 0L,
            name = "Lunch",
            builtinId = BuiltinCategory.LUNCH.id,
            recipeCount = 0,
        ),
        CategoryItem(
            id = 0L,
            name = "Dinner",
            builtinId = BuiltinCategory.DINNER.id,
            recipeCount = 0,
        ),
        CategoryItem(
            id = 0L,
            name = "Dessert",
            builtinId = BuiltinCategory.DESSERT.id,
            recipeCount = 0,
        ),
    )

val previewRecipeCategoriesBloc: RecipeCategoriesBloc =
    bloc(Model(categories = sampleItems, isLoading = false))

val previewRecipeCategoriesBlocSelectionMode: RecipeCategoriesBloc =
    bloc(
        Model(
            categories = sampleItems,
            isLoading = false,
            selectionMode = true,
            selectedIds = setOf(1L, 2L),
        )
    )

/**
 * Selection mode just entered via the header button — no rows picked yet. Confirms the header
 * renders "Select categories" and hides the trash icon until the user picks at least one row.
 */
val previewRecipeCategoriesBlocSelectionEmpty: RecipeCategoriesBloc =
    bloc(
        Model(
            categories = sampleItems,
            isLoading = false,
            selectionMode = true,
            selectedIds = emptySet(),
        )
    )

val previewRecipeCategoriesBlocCreating: RecipeCategoriesBloc =
    bloc(
        Model(
            categories = sampleItems,
            isLoading = false,
            createState = CreateState.Editing(text = "Brunch"),
        )
    )

val previewRecipeCategoriesBlocEmptyUser: RecipeCategoriesBloc =
    bloc(
        Model(
            categories =
                BuiltinCategory.entries
                    .map { builtin ->
                        CategoryItem(
                            id = 0L,
                            name = builtin.id.replaceFirstChar { it.uppercase() },
                            builtinId = builtin.id,
                            recipeCount = 0,
                        )
                    }
                    .toImmutableList(),
            isLoading = false,
        )
    )

val previewRecipeCategoriesBlocBulkDeleteDialog: RecipeCategoriesBloc =
    bloc(
        Model(
            categories = sampleItems,
            isLoading = false,
            selectionMode = true,
            selectedIds = setOf(1L, 2L),
            dialog = DialogState.BulkDelete(count = 2),
        )
    )
