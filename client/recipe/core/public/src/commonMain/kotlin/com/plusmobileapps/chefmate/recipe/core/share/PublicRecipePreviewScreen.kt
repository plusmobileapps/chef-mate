@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.share

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.public_recipe_directions
import chefmate.client.recipe.core.public.generated.resources.public_recipe_ingredients
import chefmate.client.recipe.core.public.generated.resources.public_recipe_not_found_body
import chefmate.client.recipe.core.public.generated.resources.public_recipe_not_found_title
import chefmate.client.recipe.core.public.generated.resources.public_recipe_offline_body
import chefmate.client.recipe.core.public.generated.resources.public_recipe_offline_title
import chefmate.client.recipe.core.public.generated.resources.public_recipe_retry
import chefmate.client.recipe.core.public.generated.resources.public_recipe_save
import chefmate.client.recipe.core.public.generated.resources.public_recipe_title
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.resources.stringResource

/** Stable test tags for the public recipe preview screen, shared with its UI-test robot. */
object PublicRecipeTestTags {
    const val SCREEN: String = "public_recipe_screen"
    const val SAVE_BUTTON: String = "public_recipe_save_button"
}

@Composable
fun PublicRecipePreviewScreen(bloc: PublicRecipeBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    Box(modifier = modifier.fillMaxSize().testTag(PublicRecipeTestTags.SCREEN)) {
        PlusHeaderContainer(
            modifier = Modifier.fillMaxSize(),
            data =
                PlusHeaderData.Child(
                    title = Res.string.public_recipe_title.asTextData(),
                    onBackClick = bloc::onBackClicked,
                ),
            scrollEnabled = false,
        ) {
            when (val model = state) {
                PublicRecipeBloc.Model.Loading ->
                    CenteredMessage { PlusLoadingIndicator(modifier = Modifier.size(48.dp)) }
                PublicRecipeBloc.Model.NotFound ->
                    CenteredMessage {
                        MessageBlock(
                            title = stringResource(Res.string.public_recipe_not_found_title),
                            body = stringResource(Res.string.public_recipe_not_found_body),
                        )
                    }
                PublicRecipeBloc.Model.Offline ->
                    CenteredMessage {
                        MessageBlock(
                            title = stringResource(Res.string.public_recipe_offline_title),
                            body = stringResource(Res.string.public_recipe_offline_body),
                        )
                        PlusButton(
                            text = Res.string.public_recipe_retry.asTextData(),
                            variant = PlusButtonVariant.SECONDARY,
                            onClick = bloc::onRetryClicked,
                        )
                    }
                is PublicRecipeBloc.Model.Loaded -> LoadedContent(model, bloc::onSaveClicked)
            }
        }
    }
}

@Composable
private fun LoadedContent(model: PublicRecipeBloc.Model.Loaded, onSave: () -> Unit) {
    val recipe = model.recipe
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
    ) {
        if (recipe.imageUrl != null) {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)),
            )
        }
        Text(text = recipe.title, style = MaterialTheme.typography.headlineSmall)
        recipe.description
            ?.takeIf { it.isNotBlank() }
            ?.let { description ->
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        RecipeSection(
            title = stringResource(Res.string.public_recipe_ingredients),
            body = recipe.ingredients,
        )
        RecipeSection(
            title = stringResource(Res.string.public_recipe_directions),
            body = recipe.directions,
        )
        PlusButton(
            text = Res.string.public_recipe_save.asTextData(),
            isLoading = model.isSaving,
            modifier = Modifier.fillMaxWidth().testTag(PublicRecipeTestTags.SAVE_BUTTON),
            onClick = onSave,
        )
    }
}

@Composable
private fun RecipeSection(title: String, body: String) {
    if (body.isBlank()) return
    Column(verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingExtraSmall)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            spacedBy(ChefMateTheme.dimens.paddingNormal, Alignment.CenterVertically),
    ) {
        content()
    }
}

@Composable
private fun MessageBlock(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Fake bloc in a fixed [model] for @Preview and screenshot tests. */
fun previewPublicRecipeBloc(model: PublicRecipeBloc.Model): PublicRecipeBloc =
    object : PublicRecipeBloc {
        override val state = kotlinx.coroutines.flow.MutableStateFlow(model)

        override fun onSaveClicked() = Unit

        override fun onRetryClicked() = Unit

        override fun onBackClicked() = Unit
    }

val previewPublicRecipeLoadedBloc: PublicRecipeBloc =
    previewPublicRecipeBloc(PublicRecipeBloc.Model.Loaded(recipe = Recipe.Sample))

val previewPublicRecipeNotFoundBloc: PublicRecipeBloc =
    previewPublicRecipeBloc(PublicRecipeBloc.Model.NotFound)
