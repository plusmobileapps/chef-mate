package com.plusmobileapps.chefmate.recipe.core.edit

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface EditRecipeBloc : BackClickBloc {
    val state: StateFlow<Model>

    val title: StateFlow<String>

    val description: StateFlow<String>

    val imageUrl: StateFlow<String>

    val ingredients: StateFlow<String>

    val directions: StateFlow<String>

    val sourceUrl: StateFlow<String>

    val servings: StateFlow<String>

    val prepTime: StateFlow<String>

    val cookTime: StateFlow<String>

    val totalTime: StateFlow<String>

    val calories: StateFlow<String>

    val starRating: StateFlow<Int?>

    val categories: StateFlow<Set<Category>>

    /** User-created categories from the local DB. Built-in presets are not included. */
    val availableUserCategories: StateFlow<List<Category>>

    fun onTitleChanged(title: String)

    fun onDescriptionChanged(description: String)

    fun onImageUrlChanged(imageUrl: String)

    fun onIngredientsChanged(ingredients: String)

    fun onDirectionsChanged(directions: String)

    fun onSourceUrlChanged(sourceUrl: String)

    fun onServingsChanged(servings: String)

    fun onPrepTimeChanged(prepTime: String)

    fun onCookTimeChanged(cookTime: String)

    fun onTotalTimeChanged(totalTime: String)

    fun onCaloriesChanged(calories: String)

    fun onStarRatingChanged(starRating: Int?)

    /**
     * Bulk replacement — used by the picker sheet when applying changes (currently unused but kept
     * for parity).
     */
    fun onCategoriesChanged(categories: Set<Category>)

    /** Materializes [builtin] if needed and adds it to the recipe's category set. */
    fun onAttachBuiltin(builtin: BuiltinCategory)

    /** Adds a user-created [category] to the recipe's category set. */
    fun onAttachCategory(category: Category)

    /** Removes [category] from the recipe's category set. */
    fun onDetachCategory(category: Category)

    /**
     * Creates a new user category named [name] and attaches it to the current recipe. The local DB
     * insert is offline-first; the new row appears in [availableUserCategories] as soon as it
     * lands, and remote sync happens in the background.
     */
    fun onCreateUserCategory(name: String)

    fun onDiscardChangesConfirmed()

    fun onDiscardChangesCancelled()

    fun onSaveClicked()

    data class Model(
        val title: TextData,
        val isLoading: Boolean,
        val isSaving: Boolean,
        val showDiscardChangesDialog: Boolean,
    )

    sealed class Output {
        data class Finished(val recipeId: Long) : Output()

        data object Cancelled : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long?,
            extractedRecipe: ExtractedRecipeData?,
            output: Consumer<Output>,
        ): EditRecipeBloc
    }
}
