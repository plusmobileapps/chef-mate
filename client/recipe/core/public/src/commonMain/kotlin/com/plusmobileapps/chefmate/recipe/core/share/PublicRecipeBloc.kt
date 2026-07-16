package com.plusmobileapps.chefmate.recipe.core.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only preview of a recipe opened from a shared link (`https://…/recipe/<remoteId>`) by a
 * recipient who doesn't own or collaborate on it. It fetches the public recipe by its remote id and
 * offers to save an owned copy. Recipients who already have the recipe locally are routed straight
 * to the normal [com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc] instead.
 */
interface PublicRecipeBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        PublicRecipePreviewScreen(bloc = this, modifier = modifier)
    }

    /** Saves an owned copy of the previewed recipe into the user's own recipes. */
    fun onSaveClicked()

    /** Retries the fetch after a load failure. */
    fun onRetryClicked()

    /** Opens the recipe's source [url] in the in-app browser. */
    fun onSourceUrlClicked(url: String)

    sealed interface Model {
        data object Loading : Model

        data class Loaded(
            val recipe: Recipe,
            val isSaving: Boolean = false,
            /**
             * Prep/cook/total times pre-formatted for display (e.g. "1 hr 30 min"), null when
             * unset.
             */
            val formattedPrepTime: TextData? = null,
            val formattedCookTime: TextData? = null,
            val formattedTotalTime: TextData? = null,
        ) : Model

        /** The recipe isn't public, was deleted, or the id is invalid. */
        data object NotFound : Model

        /** The fetch failed for a transient reason (e.g. no connectivity). */
        data object Offline : Model
    }

    sealed class Output {
        data object Finished : Output()

        /**
         * Open the recipe in the normal detail screen, carrying its local id. Emitted both when the
         * recipient already had the recipe locally (resolved by remote id) and after saving a copy.
         */
        data class OpenRecipe(val recipeId: Long) : Output()

        /** Open the recipe's source [url] in the in-app browser. */
        data class OpenUrl(val url: String) : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            remoteId: String,
            output: Consumer<Output>,
        ): PublicRecipeBloc
    }
}
