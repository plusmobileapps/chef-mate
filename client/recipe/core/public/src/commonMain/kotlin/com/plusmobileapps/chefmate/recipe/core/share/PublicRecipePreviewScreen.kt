@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_calories
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_details
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_kcal
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_prep_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_servings
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_source
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_total_time
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainerDefaults
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
    val loaded = state as? PublicRecipeBloc.Model.Loaded
    Box(modifier = modifier.fillMaxSize().testTag(PublicRecipeTestTags.SCREEN)) {
        PlusHeaderContainer(
            modifier = Modifier.fillMaxSize(),
            data =
                PlusHeaderData.Child(
                    title = Res.string.public_recipe_title.asTextData(),
                    onBackClick = bloc::onBackClicked,
                ),
            scrollEnabled = false,
            // Float the "Save" CTA at the bottom over the scrolling content so it stays visible on
            // open, instead of hiding at the very end of a long recipe.
            floatingToolbar =
                loaded?.let { model ->
                    { SaveRecipeToolbar(isSaving = model.isSaving, onSave = bloc::onSaveClicked) }
                },
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
                is PublicRecipeBloc.Model.Loaded ->
                    LoadedContent(
                        model = model,
                        onSourceUrlClicked = bloc::onSourceUrlClicked,
                    )
            }
        }
    }
}

@Composable
private fun LoadedContent(
    model: PublicRecipeBloc.Model.Loaded,
    onSourceUrlClicked: (String) -> Unit,
) {
    val recipe = model.recipe
    // Clearance so the last line of the recipe can scroll clear above the floating Save toolbar
    // (its height + the bottom system-bar inset it already accounts for).
    val bottomClearance =
        SaveToolbarHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
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
        RecipeDetailsCard(
            recipe = recipe,
            formattedPrepTime = model.formattedPrepTime,
            formattedCookTime = model.formattedCookTime,
            formattedTotalTime = model.formattedTotalTime,
            onSourceUrlClicked = onSourceUrlClicked,
        )
        RecipeSection(
            title = stringResource(Res.string.public_recipe_ingredients),
            body = recipe.ingredients,
        )
        RecipeSection(
            title = stringResource(Res.string.public_recipe_directions),
            body = recipe.directions,
        )
        Spacer(modifier = Modifier.height(bottomClearance))
    }
}

/** Approximate height reserved below the scrolling content for the floating Save toolbar. */
private val SaveToolbarHeight: Dp = 88.dp

/** Bottom-anchored floating Save CTA, spanning the content width with a small side gutter. */
@Composable
private fun SaveRecipeToolbar(isSaving: Boolean, onSave: () -> Unit) {
    PlusButton(
        text = Res.string.public_recipe_save.asTextData(),
        isLoading = isSaving,
        modifier =
            Modifier.fillMaxWidth()
                .widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .testTag(PublicRecipeTestTags.SAVE_BUTTON),
        onClick = onSave,
    )
}

/**
 * Key recipe metadata for the read-only shared preview: servings, prep/cook/total time, calories,
 * and a tappable source link that opens in the in-app browser. Renders nothing when the recipe
 * carries none of these fields.
 */
@Composable
private fun RecipeDetailsCard(
    recipe: Recipe,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    onSourceUrlClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasDetails =
        recipe.servings != null ||
            formattedPrepTime != null ||
            formattedCookTime != null ||
            formattedTotalTime != null ||
            recipe.calories != null ||
            recipe.sourceUrl != null
    if (!hasDetails) return
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_details),
                style = MaterialTheme.typography.titleMedium,
            )
            recipe.servings?.let { servings ->
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    label = stringResource(Res.string.recipe_detail_servings),
                    value = "$servings",
                )
            }
            formattedPrepTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value = it.localized(),
                )
            }
            formattedCookTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value = it.localized(),
                )
            }
            formattedTotalTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value = it.localized(),
                )
            }
            recipe.calories?.let { calories ->
                DetailRow(
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(Res.string.recipe_detail_calories),
                    value =
                        PhraseModel(
                                Res.string.recipe_detail_kcal,
                                "calories" to FixedString(calories.toString()),
                            )
                            .localized(),
                )
            }
            recipe.sourceUrl?.let { sourceUrl ->
                Column(verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingExtraSmall)) {
                    Text(
                        text = stringResource(Res.string.recipe_detail_source),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = sourceUrl,
                        modifier =
                            Modifier.fillMaxWidth().clickable { onSourceUrlClicked(sourceUrl) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
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

        override fun onSourceUrlClicked(url: String) = Unit

        override fun onBackClicked() = Unit
    }

val previewPublicRecipeLoadedBloc: PublicRecipeBloc =
    previewPublicRecipeBloc(
        PublicRecipeBloc.Model.Loaded(
            recipe = Recipe.Sample.copy(sourceUrl = "https://example.com/pasta-carbonara"),
            formattedPrepTime = FixedString("10 min"),
            formattedCookTime = FixedString("15 min"),
            formattedTotalTime = FixedString("25 min"),
        )
    )

val previewPublicRecipeNotFoundBloc: PublicRecipeBloc =
    previewPublicRecipeBloc(PublicRecipeBloc.Model.NotFound)
