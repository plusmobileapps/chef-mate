package com.plusmobileapps.chefmate.recipe.core.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RecipeDetailBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    /**
     * One-shot share-link URLs to hand to the platform share sheet. Emitted after the recipe is
     * successfully made public (or immediately if it already was). The screen collects this and
     * passes each URL to `rememberShareLauncher`.
     */
    val shareLink: Flow<String>

    @Composable
    override fun Content(modifier: Modifier) {
        RecipeDetailScreen(bloc = this, modifier = modifier)
    }

    val childSlot: Value<ChildSlot<*, Sheet>>

    val fullImageSlot: Value<ChildSlot<*, FullImage>>

    fun onImageClicked()

    fun onCloseFullImage()

    fun onEditClicked()

    fun onDeleteClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    fun onFavoriteToggled()

    fun onAddToGroceryListClicked()

    fun onCookModeClicked()

    fun onAddToMealPlanClicked()

    /** Dismiss the coach mark with the given [com.plusmobileapps.chefmate.di.CoachMarkId]. */
    fun onCoachMarkDismissed(id: String)

    fun onSourceUrlClicked(url: String)

    /**
     * Share a Chef Mate link to this recipe. Prompts to make it public first if it isn't already.
     */
    fun onShareLinkClicked()

    /** Confirm the "make this recipe public" prompt, then share the link. */
    fun onShareConfirmed()

    /** Dismiss the "make this recipe public" prompt without sharing. */
    fun onShareDismissed()

    /** Make a shared recipe private again so its link stops resolving for others. */
    fun onStopSharingClicked()

    fun onDismissSheet()

    data class Model(
        val isLoading: Boolean,
        val isDeleting: Boolean,
        val showDeleteConfirmationDialog: Boolean,
        /** True while the "anyone with the link can view this" confirmation dialog is shown. */
        val showShareConfirmation: Boolean = false,
        val recipe: Recipe,
        val createdAt: TextData,
        val updatedAt: TextData,
        val formattedPrepTime: TextData? = null,
        val formattedCookTime: TextData? = null,
        val formattedTotalTime: TextData? = null,
        /** Id of the coach mark currently allowed to show on this screen, or null. */
        val activeCoachMark: String? = null,
    )

    sealed class Output {
        data object Finished : Output()

        data class EditRecipe(val recipeId: Long) : Output()

        data class OpenUrl(val url: String) : Output()

        data object OpenGroceryList : Output()

        data class OpenMealPlanner(val recipeId: Long) : Output()

        data class OpenCookMode(val recipeId: Long) : Output()
    }

    sealed class Sheet {

        abstract val bloc: ComposeScreen

        data class AddToGroceryList(override val bloc: AddRecipeToGroceryListBloc) : Sheet()
    }

    sealed class FullImage {
        data class Active(val imageUrl: String, val recipeId: Long, val title: String) : FullImage()
    }

    interface Factory {
        fun create(context: BlocContext, recipeId: Long, output: Consumer<Output>): RecipeDetailBloc
    }
}
