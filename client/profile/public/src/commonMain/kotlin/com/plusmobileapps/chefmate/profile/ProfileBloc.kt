package com.plusmobileapps.chefmate.profile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * A public profile: its owner's handle, bio and avatar over the recipes they've published.
 *
 * One bloc serves both "my profile" and "someone else's" — they differ only in which actions the
 * header offers, which [Model.Loaded.isOwnProfile] drives. Splitting them would duplicate the
 * entire load/render path to vary two buttons.
 *
 * Tapping a recipe routes out to the existing read-only public preview
 * ([com.plusmobileapps.chefmate.recipe.core.share.PublicRecipeBloc]), which already knows how to
 * fetch a public recipe and save an owned copy.
 */
interface ProfileBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    /**
     * One-shot profile URLs to hand to the platform share sheet, emitted when the owner taps share.
     * Mirrors `RecipeDetailBloc.shareLink` — the screen collects this and passes each URL to
     * `rememberShareLauncher`, rather than routing a share through root navigation.
     */
    val shareLink: Flow<String>

    @Composable
    override fun Content(modifier: Modifier) {
        ProfileScreen(bloc = this, modifier = modifier)
    }

    /** Opens one of the published recipes. */
    fun onRecipeClicked(remoteId: String)

    /** Own profile only: opens the editor to change display name, bio or avatar. */
    fun onManageProfileClicked()

    /** Own profile only: shares the profile's public link. */
    fun onShareClicked()

    /** Own profile, no handle yet: starts the claim flow. */
    fun onCreateProfileClicked()

    /** Retries after a load failure. */
    fun onRetryClicked()

    sealed interface Model {
        data object Loading : Model

        data class Loaded(
            val profile: SocialProfile,
            val recipes: ImmutableList<Recipe>,
            /** True when this is the signed-in user's own profile. */
            val isOwnProfile: Boolean,
        ) : Model

        /**
         * The signed-in user hasn't claimed a handle, so they have no public profile yet. Only
         * reachable for one's own profile — it's the empty state that invites them to create one.
         */
        data object NoProfile : Model

        /** No profile with that handle exists (never created, or the account was deleted). */
        data object NotFound : Model

        /** The fetch failed for a transient reason, e.g. no connectivity. */
        data object Offline : Model
    }

    /** Whose profile to show. */
    @Serializable
    data class Props(
        /** The handle to open, or null for the signed-in user's own profile. */
        val handle: String? = null
    )

    sealed class Output {
        data object Back : Output()

        /** Open a published recipe in the read-only public preview, keyed by its remote id. */
        data class OpenRecipe(val remoteId: String) : Output()

        /** Open the profile editor (also the handle-claim flow when there's no profile yet). */
        data object OpenManageProfile : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): ProfileBloc
    }
}
