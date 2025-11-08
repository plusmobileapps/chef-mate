package com.plusmobileapps.chefmate.recipe.core.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_cancel
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_confirm
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_message
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_calories
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_calories_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_cook_time
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_cook_time_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_description
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_description_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_directions
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_directions_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_image_url
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_image_url_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_ingredients
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_ingredients_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_prep_time
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_prep_time_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_servings
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_servings_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_source_url
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_source_url_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_title_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_total_time
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_total_time_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_save
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()

    if (state.showDiscardChangesDialog) {
        DiscardChangesDialog(
            onConfirm = bloc::onDiscardChangesConfirmed,
            onDismiss = bloc::onDiscardChangesCancelled,
        )
    }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Child(
                title = state.title,
                onBackClick = bloc::onBackClicked,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        floatingActionButton = {
            SaveRecipeFab(
                isSaving = state.isSaving,
                onSaveClicked = bloc::onSaveClicked,
            )
        },
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                LoadingIndicator()
            }
        } else {
            EditRecipeContent(bloc = bloc)
        }
    }
}

@Composable
private fun SaveRecipeFab(
    isSaving: Boolean,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onSaveClicked,
        modifier = modifier,
    ) {
        if (isSaving) {
            PlusLoadingIndicator(
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Text(stringResource(Res.string.edit_recipe_save))
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditRecipeContent(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecipeTitleField(bloc = bloc)
        RecipeDescriptionField(bloc = bloc)
        RecipeStarRatingField(bloc = bloc)
        RecipeImageUrlField(bloc = bloc)
        RecipeSourceUrlField(bloc = bloc)
        RecipeServingsField(bloc = bloc)
        RecipePrepTimeField(bloc = bloc)
        RecipeCookTimeField(bloc = bloc)
        RecipeTotalTimeField(bloc = bloc)
        RecipeCaloriesField(bloc = bloc)
        RecipeIngredientsField(bloc = bloc)
        RecipeDirectionsField(bloc = bloc)
    }
}

@Composable
private fun RecipeTitleField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val title by bloc.title.collectAsState()

    OutlinedTextField(
        value = title,
        onValueChange = bloc::onTitleChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_title)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_title_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeDescriptionField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val description by bloc.description.collectAsState()

    OutlinedTextField(
        value = description,
        onValueChange = bloc::onDescriptionChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_description)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_description_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
    )
}

@Composable
private fun RecipeStarRatingField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val starRating by bloc.starRating.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Rating",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (i in 1..5) {
                val isFilled = starRating != null && i <= starRating!!
                Icon(
                    imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "Star $i",
                    tint =
                        if (isFilled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clickable {
                                bloc.onStarRatingChanged(if (starRating == i) null else i)
                            },
                )
            }
        }
    }
}

@Composable
private fun RecipeImageUrlField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val imageUrl by bloc.imageUrl.collectAsState()

    OutlinedTextField(
        value = imageUrl,
        onValueChange = bloc::onImageUrlChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_image_url)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_image_url_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeSourceUrlField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val sourceUrl by bloc.sourceUrl.collectAsState()

    OutlinedTextField(
        value = sourceUrl,
        onValueChange = bloc::onSourceUrlChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_source_url)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_source_url_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeServingsField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val servings by bloc.servings.collectAsState()

    OutlinedTextField(
        value = servings,
        onValueChange = bloc::onServingsChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_servings)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_servings_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipePrepTimeField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val prepTime by bloc.prepTime.collectAsState()

    OutlinedTextField(
        value = prepTime,
        onValueChange = bloc::onPrepTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_prep_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_prep_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeCookTimeField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val cookTime by bloc.cookTime.collectAsState()

    OutlinedTextField(
        value = cookTime,
        onValueChange = bloc::onCookTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_cook_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_cook_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeTotalTimeField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val totalTime by bloc.totalTime.collectAsState()

    OutlinedTextField(
        value = totalTime,
        onValueChange = bloc::onTotalTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_total_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_total_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeCaloriesField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val calories by bloc.calories.collectAsState()

    OutlinedTextField(
        value = calories,
        onValueChange = bloc::onCaloriesChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_calories)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_calories_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun RecipeIngredientsField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val ingredients by bloc.ingredients.collectAsState()

    OutlinedTextField(
        value = ingredients,
        onValueChange = bloc::onIngredientsChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_ingredients)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_ingredients_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        minLines = 5,
        maxLines = 10,
    )
}

@Composable
private fun RecipeDirectionsField(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val directions by bloc.directions.collectAsState()

    OutlinedTextField(
        value = directions,
        onValueChange = bloc::onDirectionsChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_directions)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_directions_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        minLines = 5,
        maxLines = 10,
    )
}

@Composable
private fun DiscardChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_recipe_discard_title)) },
        text = { Text(stringResource(Res.string.edit_recipe_discard_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.edit_recipe_discard_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.edit_recipe_discard_cancel))
            }
        },
        modifier = modifier,
    )
}

private val previewBloc = object : EditRecipeBloc {
    override val state: StateFlow<EditRecipeBloc.Model> = MutableStateFlow(
        EditRecipeBloc.Model(
            title = FixedString("Edit Recipe"),
            isLoading = false,
            isSaving = false,
            showDiscardChangesDialog = false,
        )
    )
    override val title: StateFlow<String> = MutableStateFlow("Spaghetti Carbonara")
    override val description: StateFlow<String> = MutableStateFlow("A classic Italian pasta dish with eggs, cheese, pancetta, and black pepper")
    override val imageUrl: StateFlow<String> = MutableStateFlow("https://example.com/carbonara.jpg")
    override val ingredients: StateFlow<String> = MutableStateFlow(
        """400g spaghetti
200g pancetta
4 large eggs
100g Pecorino Romano cheese
Black pepper to taste
Salt for pasta water"""
    )
    override val directions: StateFlow<String> = MutableStateFlow(
        """1. Bring a large pot of salted water to boil
2. Cook spaghetti until al dente
3. While pasta cooks, fry pancetta until crispy
4. Beat eggs and mix with grated cheese
5. Drain pasta, reserving some pasta water
6. Combine hot pasta with pancetta
7. Remove from heat and add egg mixture
8. Toss quickly, adding pasta water if needed
9. Season with black pepper and serve"""
    )
    override val sourceUrl: StateFlow<String> = MutableStateFlow("https://example.com/recipe/carbonara")
    override val servings: StateFlow<String> = MutableStateFlow("4")
    override val prepTime: StateFlow<String> = MutableStateFlow("10 minutes")
    override val cookTime: StateFlow<String> = MutableStateFlow("15 minutes")
    override val totalTime: StateFlow<String> = MutableStateFlow("25 minutes")
    override val calories: StateFlow<String> = MutableStateFlow("550")
    override val starRating: StateFlow<Int?> = MutableStateFlow(4)

    override fun onTitleChanged(title: String) {}
    override fun onDescriptionChanged(description: String) {}
    override fun onImageUrlChanged(imageUrl: String) {}
    override fun onIngredientsChanged(ingredients: String) {}
    override fun onDirectionsChanged(directions: String) {}
    override fun onSourceUrlChanged(sourceUrl: String) {}
    override fun onServingsChanged(servings: String) {}
    override fun onPrepTimeChanged(prepTime: String) {}
    override fun onCookTimeChanged(cookTime: String) {}
    override fun onTotalTimeChanged(totalTime: String) {}
    override fun onCaloriesChanged(calories: String) {}
    override fun onStarRatingChanged(starRating: Int?) {}
    override fun onDiscardChangesConfirmed() {}
    override fun onDiscardChangesCancelled() {}
    override fun onSaveClicked() {}
    override fun onBackClicked() {}
}

@Preview
@Composable
private fun EditRecipeScreenLightPreview() {
    ChefMateTheme(darkTheme = false) {
        EditRecipeScreen(bloc = previewBloc)
    }
}

@Preview
@Composable
private fun EditRecipeScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        EditRecipeScreen(bloc = previewBloc)
    }
}