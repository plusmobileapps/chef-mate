package com.plusmobileapps.chefmate.recipe.core.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_back
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
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    bloc: EditRecipeBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            EditRecipeTopBar(
                onBackClicked = bloc::onBackClicked,
            )
        },
        floatingActionButton = {
            SaveRecipeFab(
                isSaving = state.isSaving,
                onSaveClicked = bloc::onSaveClicked,
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                EditRecipeContent(bloc = bloc)
            }
        }

        if (state.showDiscardChangesDialog) {
            DiscardChangesDialog(
                onConfirm = bloc::onDiscardChangesConfirmed,
                onDismiss = bloc::onDiscardChangesCancelled,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRecipeTopBar(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.edit_recipe_title)) },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.edit_recipe_back),
                )
            }
        },
        modifier = modifier,
    )
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
            CircularProgressIndicator(
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecipeTitleField(bloc = bloc)
        RecipeDescriptionField(bloc = bloc)
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
