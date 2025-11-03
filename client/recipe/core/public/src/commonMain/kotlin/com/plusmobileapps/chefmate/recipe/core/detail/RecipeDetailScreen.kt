@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_add_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_back
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_calories
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_created
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_cancel
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_confirm
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_message
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_title
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_deleting_message
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_deleting_title
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_description
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_details
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_directions
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_edit
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_image
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_ingredients
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_kcal
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_minutes
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_prep_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_rating_label
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_rating_value
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_remove_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_servings
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_source
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_timestamps
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_total_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_updated
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    bloc: RecipeDetailBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.recipe.title) },
                navigationIcon = {
                    IconButton(onClick = bloc::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(Res.string.recipe_detail_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { bloc.onFavoriteToggled() }) {
                        Icon(
                            imageVector =
                                if (state.recipe.isFavorite) {
                                    Icons.Default.Favorite
                                } else {
                                    Icons.Default.FavoriteBorder
                                },
                            contentDescription =
                                if (state.recipe.isFavorite) {
                                    stringResource(Res.string.recipe_detail_remove_favorite)
                                } else {
                                    stringResource(Res.string.recipe_detail_add_favorite)
                                },
                        )
                    }
                    IconButton(onClick = { bloc.onEditClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.recipe_detail_edit),
                        )
                    }
                    IconButton(onClick = { bloc.onDeleteClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.recipe_detail_delete),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            RecipeDetailContent(
                recipe = state.recipe,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            )
        }

        // Delete confirmation dialog
        if (state.showDeleteConfirmationDialog) {
            DeleteConfirmationDialog(
                recipeName = state.recipe.title,
                onConfirm = bloc::onDeleteConfirmed,
                onDismiss = bloc::onDeleteDismissed,
            )
        }

        // Deleting progress dialog
        if (state.isDeleting) {
            DeletingDialog()
        }
    }
}

@Composable
private fun RecipeDetailContent(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Recipe Image
        recipe.imageUrl?.let { imageUrl ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            PhraseModel(
                                Res.string.recipe_detail_image,
                                "url" to FixedString(imageUrl),
                            ).localized(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Star Rating
        recipe.starRating?.let { rating ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.recipe_detail_rating_label),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text =
                        PhraseModel(
                            Res.string.recipe_detail_rating_value,
                            "star_rating" to FixedString(rating.toString()),
                        ).localized(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        // Description
        recipe.description?.let { description ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.recipe_detail_description),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Servings and Times
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.recipe_detail_details),
                    style = MaterialTheme.typography.titleMedium,
                )

                recipe.servings?.let { servings ->
                    DetailRow(
                        label = stringResource(Res.string.recipe_detail_servings),
                        value = "$servings",
                    )
                }

                recipe.prepTime?.let { prepTime ->
                    DetailRow(
                        label = stringResource(Res.string.recipe_detail_prep_time),
                        value =
                            PhraseModel(
                                Res.string.recipe_detail_minutes,
                                "minutes" to FixedString(prepTime.toString()),
                            ).localized(),
                    )
                }

                recipe.cookTime?.let { cookTime ->
                    DetailRow(
                        label = stringResource(Res.string.recipe_detail_cook_time),
                        value =
                            PhraseModel(
                                Res.string.recipe_detail_minutes,
                                "minutes" to FixedString(cookTime.toString()),
                            ).localized(),
                    )
                }

                recipe.totalTime?.let { totalTime ->
                    DetailRow(
                        label = stringResource(Res.string.recipe_detail_total_time),
                        value =
                            PhraseModel(
                                Res.string.recipe_detail_minutes,
                                "minutes" to FixedString(totalTime.toString()),
                            ).localized(),
                    )
                }

                recipe.calories?.let { calories ->
                    DetailRow(
                        label = stringResource(Res.string.recipe_detail_calories),
                        value =
                            PhraseModel(
                                Res.string.recipe_detail_kcal,
                                "calories" to FixedString(calories.toString()),
                            ).localized(),
                    )
                }
            }
        }

        // Source URL
        recipe.sourceUrl?.let { sourceUrl ->
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.recipe_detail_source),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = sourceUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Ingredients
        if (recipe.ingredients.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.recipe_detail_ingredients),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = recipe.ingredients,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Directions
        if (recipe.directions.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.recipe_detail_directions),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = recipe.directions,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        // Timestamps
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.recipe_detail_timestamps),
                    style = MaterialTheme.typography.titleMedium,
                )
                DetailRow(label = stringResource(Res.string.recipe_detail_created), value = recipe.createdAt.toString())
                DetailRow(label = stringResource(Res.string.recipe_detail_updated), value = recipe.updatedAt.toString())
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    recipeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.recipe_detail_delete_title)) },
        text = {
            Text(
                PhraseModel(
                    Res.string.recipe_detail_delete_message,
                    "recipe_name" to FixedString(recipeName),
                ).localized(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.recipe_detail_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.recipe_detail_delete_cancel))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun DeletingDialog(modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal while deleting */ },
        title = { Text(stringResource(Res.string.recipe_detail_deleting_title)) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.recipe_detail_deleting_message))
            }
        },
        confirmButton = { },
        modifier = modifier,
    )
}
