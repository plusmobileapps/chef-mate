package com.plusmobileapps.chefmate.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.profile_avatar_content_description
import chefmate.client.profile.public.generated.resources.profile_create
import chefmate.client.profile.public.generated.resources.profile_empty_other
import chefmate.client.profile.public.generated.resources.profile_empty_own
import chefmate.client.profile.public.generated.resources.profile_manage
import chefmate.client.profile.public.generated.resources.profile_no_profile_message
import chefmate.client.profile.public.generated.resources.profile_not_found
import chefmate.client.profile.public.generated.resources.profile_offline
import chefmate.client.profile.public.generated.resources.profile_recipe_count
import chefmate.client.profile.public.generated.resources.profile_recipe_count_one
import chefmate.client.profile.public.generated.resources.profile_retry
import chefmate.client.profile.public.generated.resources.profile_share_content_description
import chefmate.client.profile.public.generated.resources.profile_title
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusAvatar
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberShareLauncher

@Composable
fun ProfileScreen(bloc: ProfileBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    val loaded = state as? ProfileBloc.Model.Loaded
    val shareLauncher = rememberShareLauncher()

    LaunchedEffect(bloc) { bloc.shareLink.collect { url -> shareLauncher(url) } }

    PlusHeaderContainer(
        modifier = modifier.testTag(ProfileTestTags.SCREEN),
        // The content owns its scrolling: the published list is a LazyColumn, and nesting one
        // inside the container's own verticalScroll measures it with infinite height and throws.
        // Same reason RecipeListScreen opts out.
        scrollEnabled = false,
        data =
            PlusHeaderData.Child(
                title =
                    loaded?.profile?.let { FixedString("@${it.handle}") }
                        ?: Res.string.profile_title.asTextData(),
                onBackClick = bloc::onBackClicked,
                // Only your own profile gets a share affordance in the header; sharing someone
                // else's is what the system share sheet on the link is for.
                trailingAccessory =
                    loaded
                        ?.takeIf { it.isOwnProfile }
                        ?.let {
                            PlusHeaderData.TrailingAccessory.Icon(
                                icon = Icons.Default.Share,
                                contentDesc =
                                    Res.string.profile_share_content_description.asTextData(),
                                onClick = bloc::onShareClicked,
                            )
                        },
            ),
        content = {
            when (val model = state) {
                ProfileBloc.Model.Loading -> CenteredMessage { PlusLoadingIndicator() }
                ProfileBloc.Model.NoProfile ->
                    NoProfileState(onCreateClick = bloc::onCreateProfileClicked)
                ProfileBloc.Model.NotFound ->
                    CenteredMessage(testTag = ProfileTestTags.NOT_FOUND) {
                        Text(
                            Res.string.profile_not_found.asTextData().localized(),
                            style = ChefMateTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                ProfileBloc.Model.Offline ->
                    CenteredMessage(testTag = ProfileTestTags.OFFLINE) {
                        Text(
                            Res.string.profile_offline.asTextData().localized(),
                            style = ChefMateTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        PlusButton(
                            text = Res.string.profile_retry.asTextData(),
                            variant = PlusButtonVariant.SECONDARY,
                            modifier = Modifier.testTag(ProfileTestTags.RETRY),
                            onClick = bloc::onRetryClicked,
                        )
                    }
                is ProfileBloc.Model.Loaded ->
                    LoadedProfile(
                        model = model,
                        onRecipeClick = bloc::onRecipeClicked,
                        onManageClick = bloc::onManageProfileClicked,
                    )
            }
        },
    )
}

@Composable
private fun LoadedProfile(
    model: ProfileBloc.Model.Loaded,
    onRecipeClick: (String) -> Unit,
    onManageClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(ProfileTestTags.RECIPE_LIST),
        contentPadding = PaddingValues(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
    ) {
        item {
            ProfileHeader(
                profile = model.profile,
                isOwnProfile = model.isOwnProfile,
                onManageClick = onManageClick,
            )
        }

        if (model.recipes.isEmpty()) {
            item {
                Text(
                    if (model.isOwnProfile) {
                            Res.string.profile_empty_own.asTextData()
                        } else {
                            Res.string.profile_empty_other.asTextData()
                        }
                        .localized(),
                    style = ChefMateTheme.typography.bodyMedium,
                    color = ChefMateTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(ChefMateTheme.dimens.paddingNormal)
                            .testTag(ProfileTestTags.EMPTY),
                )
            }
        }

        items(model.recipes, key = { it.remoteId ?: it.title }) { recipe ->
            PublishedRecipeRow(
                recipe = recipe,
                // A published recipe always has a remote id — it's what the listing is keyed by —
                // but guard rather than force-unwrap so a malformed row can't crash the screen.
                onClick = { recipe.remoteId?.let(onRecipeClick) },
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: SocialProfile,
    isOwnProfile: Boolean,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        PlusAvatar(
            imageUrl = profile.avatarUrl,
            contentDescription =
                Res.string.profile_avatar_content_description.asTextData().localized(),
            fallbackText = profile.displayName.take(1).uppercase(),
            size = AVATAR_SIZE,
            modifier = Modifier.testTag(ProfileTestTags.AVATAR),
        )

        Text(
            profile.displayName,
            style = ChefMateTheme.typography.headlineSmall,
            modifier = Modifier.testTag(ProfileTestTags.DISPLAY_NAME),
        )

        Text(
            "@${profile.handle}",
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(ProfileTestTags.HANDLE),
        )

        if (profile.bio.isNotBlank()) {
            Text(
                profile.bio,
                style = ChefMateTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().testTag(ProfileTestTags.BIO),
            )
        }

        Text(
            recipeCountText(profile.publishedRecipeCount).localized(),
            style = ChefMateTheme.typography.labelLarge,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(ProfileTestTags.RECIPE_COUNT),
        )

        if (isOwnProfile) {
            PlusButton(
                text = Res.string.profile_manage.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                modifier = Modifier.testTag(ProfileTestTags.MANAGE),
                onClick = onManageClick,
            )
        }
    }
}

private fun recipeCountText(count: Int) =
    if (count == 1) {
        Res.string.profile_recipe_count_one.asTextData()
    } else {
        PhraseModel(Res.string.profile_recipe_count, "count" to FixedString(count.toString()))
    }

@Composable
private fun PublishedRecipeRow(recipe: Recipe, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag(ProfileTestTags.RECIPE_ITEM),
        horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = null,
            modifier = Modifier.size(THUMBNAIL_SIZE),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
        ) {
            Text(recipe.title, style = ChefMateTheme.typography.titleMedium)
            recipe.description
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        it,
                        style = ChefMateTheme.typography.bodySmall,
                        color = ChefMateTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
        }
    }
}

/** The signed-in user has no public profile yet — invite them to claim a handle. */
@Composable
private fun NoProfileState(onCreateClick: () -> Unit) {
    CenteredMessage(testTag = ProfileTestTags.EMPTY) {
        Text(
            Res.string.profile_no_profile_message.asTextData().localized(),
            style = ChefMateTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        PlusButton(
            text = Res.string.profile_create.asTextData(),
            modifier = Modifier.testTag(ProfileTestTags.CREATE_PROFILE),
            onClick = onCreateClick,
        )
    }
}

@Composable
private fun CenteredMessage(
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(ChefMateTheme.dimens.paddingLarge)
                .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal, Alignment.CenterVertically),
        content = content,
    )
}

private val AVATAR_SIZE = 96.dp
private val THUMBNAIL_SIZE = 72.dp
