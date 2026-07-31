package com.plusmobileapps.chefmate.profile.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.profile.ProfileBloc
import com.plusmobileapps.chefmate.profile.ProfileBloc.Model
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

private fun profileBloc(model: Model): ProfileBloc =
    object : ProfileBloc {
        override val state = MutableStateFlow(model)

        override val shareLink = emptyFlow<String>()

        override fun onRecipeClicked(remoteId: String) = Unit

        override fun onManageProfileClicked() = Unit

        override fun onShareClicked() = Unit

        override fun onCreateProfileClicked() = Unit

        override fun onRetryClicked() = Unit

        override fun onBackClicked() = Unit
    }

private val sampleProfile =
    SocialProfile(
        id = "chef-1",
        handle = "juliachild",
        displayName = "Julia Child",
        bio = "French cooking, demystified. Butter is not optional.",
        avatarUrl = null,
        publishedRecipeCount = 3,
    )

private fun sampleRecipe(id: Long, title: String, description: String) =
    Recipe.Sample.copy(
        id = id,
        remoteId = "remote-$id",
        title = title,
        description = description,
        imageUrl = null,
    )

private val sampleRecipes =
    listOf(
            sampleRecipe(1, "Beef Bourguignon", "The one that takes all afternoon."),
            sampleRecipe(2, "Coq au Vin", "Chicken braised in red wine."),
            sampleRecipe(3, "Tarte Tatin", "Upside-down caramelised apple tart."),
        )
        .toImmutableList()

/** Someone else's profile, seen by a visitor who followed a /@handle link. */
val previewProfileBloc: ProfileBloc =
    profileBloc(
        Model.Loaded(profile = sampleProfile, recipes = sampleRecipes, isOwnProfile = false)
    )

/** Your own profile, which adds the Edit profile action and the share affordance. */
val previewOwnProfileBloc: ProfileBloc =
    profileBloc(Model.Loaded(profile = sampleProfile, recipes = sampleRecipes, isOwnProfile = true))

/** Your own profile before you've published anything. */
val previewOwnProfileEmptyBloc: ProfileBloc =
    profileBloc(
        Model.Loaded(
            profile = sampleProfile.copy(publishedRecipeCount = 0),
            recipes = persistentListOf(),
            isOwnProfile = true,
        )
    )

/** A visitor looking at a profile that hasn't published anything. */
val previewProfileEmptyBloc: ProfileBloc =
    profileBloc(
        Model.Loaded(
            profile = sampleProfile.copy(publishedRecipeCount = 0),
            recipes = persistentListOf(),
            isOwnProfile = false,
        )
    )

/** Signed in, but no handle claimed yet — the invitation to create a profile. */
val previewNoProfileBloc: ProfileBloc = profileBloc(Model.NoProfile)

val previewProfileLoadingBloc: ProfileBloc = profileBloc(Model.Loading)

val previewProfileNotFoundBloc: ProfileBloc = profileBloc(Model.NotFound)

val previewProfileOfflineBloc: ProfileBloc = profileBloc(Model.Offline)

@Preview
@Composable
internal fun ProfilePreview() {
    ChefMateTheme { previewProfileBloc.Content(Modifier) }
}

@Preview
@Composable
internal fun OwnProfilePreview() {
    ChefMateTheme { previewOwnProfileBloc.Content(Modifier) }
}

@Preview
@Composable
internal fun NoProfilePreview() {
    ChefMateTheme { previewNoProfileBloc.Content(Modifier) }
}
