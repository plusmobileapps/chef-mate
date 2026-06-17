@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.cook.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.cook.public.generated.resources.Res
import chefmate.client.cook.public.generated.resources.whats_cooking_cancel_select
import chefmate.client.cook.public.generated.resources.whats_cooking_delete_selected
import chefmate.client.cook.public.generated.resources.whats_cooking_empty
import chefmate.client.cook.public.generated.resources.whats_cooking_select
import chefmate.client.cook.public.generated.resources.whats_cooking_title
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import org.jetbrains.compose.resources.stringResource

/**
 * @param onClose if non-null, renders the close icon and invokes this on tap. Pass null when
 *   embedding inline (e.g. in the persistent peek sheet on mobile) where dismissal is handled by
 *   the parent.
 * @param onRecipeSelected invoked after a non-select-mode recipe tap so the parent surface (mobile
 *   peek sheet, tablet modal) can dismiss itself in addition to the bloc switching the active
 *   recipe.
 */
@Composable
fun WhatsCookingScreen(
    bloc: WhatsCookingBloc,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = bloc::onCloseClicked,
    onRecipeSelected: () -> Unit = {},
) {
    val state by bloc.state.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            windowInsets = WindowInsets(0.dp),
            title = { Text(stringResource(Res.string.whats_cooking_title)) },
            navigationIcon = {
                if (onClose != null) {
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
                }
            },
            actions = {
                AnimatedVisibility(
                    visible = state.isSelectMode && state.hasSelection,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    IconButton(onClick = bloc::onDeleteSelectedClicked) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription =
                                stringResource(Res.string.whats_cooking_delete_selected),
                        )
                    }
                }
                IconButton(onClick = bloc::onSelectModeToggled) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription =
                            stringResource(
                                if (state.isSelectMode) {
                                    Res.string.whats_cooking_cancel_select
                                } else {
                                    Res.string.whats_cooking_select
                                }
                            ),
                        tint =
                            if (state.isSelectMode) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                androidx.compose.material3.LocalContentColor.current
                            },
                    )
                }
            },
        )
        if (state.recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.whats_cooking_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(state.recipes, key = { it.recipeId }) { item ->
                    WhatsCookingRow(
                        item = item,
                        isSelectMode = state.isSelectMode,
                        isSelected = item.recipeId in state.selectedRecipeIds,
                        onClick = {
                            if (state.isSelectMode) {
                                bloc.onSelectionToggled(item.recipeId)
                            } else {
                                bloc.onRecipeClicked(item.recipeId)
                                onRecipeSelected()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WhatsCookingRow(
    item: WhatsCookingBloc.Model.Item,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(
                    visible = isSelectMode,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Row {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() },
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }
                RecipeImage(
                    imageUrl = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(40.dp),
                )
            }
        },
        headlineContent = { Text(item.title) },
    )
}
