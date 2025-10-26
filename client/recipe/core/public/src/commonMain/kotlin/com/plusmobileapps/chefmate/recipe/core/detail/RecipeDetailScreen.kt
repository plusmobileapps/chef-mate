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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.recipe.data.Recipe

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
                                    "Remove from favorites"
                                } else {
                                    "Add to favorites"
                                },
                        )
                    }
                    IconButton(onClick = { bloc.onEditClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit recipe",
                        )
                    }
                    IconButton(onClick = { bloc.onDeleteClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recipe",
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
                        text = "Image: $imageUrl",
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = "Rating:",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "$rating/5 ⭐",
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
                        text = "Description",
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
                    text = "Details",
                    style = MaterialTheme.typography.titleMedium,
                )

                recipe.servings?.let { servings ->
                    DetailRow(label = "Servings", value = "$servings")
                }

                recipe.prepTime?.let { prepTime ->
                    DetailRow(label = "Prep Time", value = "$prepTime minutes")
                }

                recipe.cookTime?.let { cookTime ->
                    DetailRow(label = "Cook Time", value = "$cookTime minutes")
                }

                recipe.totalTime?.let { totalTime ->
                    DetailRow(label = "Total Time", value = "$totalTime minutes")
                }

                recipe.calories?.let { calories ->
                    DetailRow(label = "Calories", value = "$calories kcal")
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
                        text = "Source",
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

        // Timestamps
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Timestamps",
                    style = MaterialTheme.typography.titleMedium,
                )
                DetailRow(label = "Created", value = recipe.createdAt.toString())
                DetailRow(label = "Updated", value = recipe.updatedAt.toString())
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
