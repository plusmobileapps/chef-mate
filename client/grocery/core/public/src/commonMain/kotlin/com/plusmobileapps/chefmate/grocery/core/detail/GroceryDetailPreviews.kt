package com.plusmobileapps.chefmate.grocery.core.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun groceryDetailBloc(item: GroceryItem): GroceryDetailBloc =
    object : GroceryDetailBloc {
        override val models =
            MutableStateFlow<GroceryDetailBloc.Model>(GroceryDetailBloc.Model.Loaded(item))

        override fun onGroceryNameChanged(name: String) = Unit

        override fun onGroceryQuantityChanged(quantity: String) = Unit

        override fun onGroceryCheckedChanged(isChecked: Boolean) = Unit

        override fun onAisleChanged(category: GroceryCategory) = Unit

        override fun onSaveClicked() = Unit

        override fun onBackClicked() = Unit
    }

private val sampleItem =
    GroceryItem(
        id = 1L,
        name = "1 gal Whole milk",
        displayName = "Whole milk",
        quantity = "1 gal",
        category = GroceryCategory.DAIRY,
        isChecked = false,
    )

/**
 * Loaded detail sheet — exercises quantity + name fields, aisle dropdown, unchecked purchase row.
 */
val previewGroceryDetailBlocLoaded: GroceryDetailBloc = groceryDetailBloc(sampleItem)

/** Loaded + purchased — exercises the checked variant of the purchase row. */
val previewGroceryDetailBlocPurchased: GroceryDetailBloc =
    groceryDetailBloc(sampleItem.copy(isChecked = true))

/** Item linked to a recipe — exercises the "From: …" source line. */
val previewGroceryDetailBlocFromRecipe: GroceryDetailBloc =
    groceryDetailBloc(sampleItem.copy(recipeName = "Pancakes"))

/** Loading state — exercises the spinner. */
val previewGroceryDetailBlocLoading: GroceryDetailBloc =
    object : GroceryDetailBloc, BackClickBloc {
        override val models =
            MutableStateFlow<GroceryDetailBloc.Model>(GroceryDetailBloc.Model.Loading)

        override fun onGroceryNameChanged(name: String) = Unit

        override fun onGroceryQuantityChanged(quantity: String) = Unit

        override fun onGroceryCheckedChanged(isChecked: Boolean) = Unit

        override fun onAisleChanged(category: GroceryCategory) = Unit

        override fun onSaveClicked() = Unit

        override fun onBackClicked() = Unit
    }

@Preview(showBackground = true, heightDp = 500)
@Composable
internal fun GroceryDetailSheetLoadedPreview() {
    ChefMateTheme { GroceryDetailSheetContent(bloc = previewGroceryDetailBlocLoaded) }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
internal fun GroceryDetailSheetPurchasedPreview() {
    ChefMateTheme { GroceryDetailSheetContent(bloc = previewGroceryDetailBlocPurchased) }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
internal fun GroceryDetailSheetFromRecipePreview() {
    ChefMateTheme { GroceryDetailSheetContent(bloc = previewGroceryDetailBlocFromRecipe) }
}

@Preview(showBackground = true, heightDp = 300)
@Composable
internal fun GroceryDetailSheetLoadingPreview() {
    ChefMateTheme { GroceryDetailSheetContent(bloc = previewGroceryDetailBlocLoading) }
}
