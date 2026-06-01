package com.plusmobileapps.chefmate.recipe.exporter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.exporter.`public`.generated.resources.Res
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_deselect_all
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_done_button
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_done_headline
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_empty_body
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_empty_headline
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_export_button
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_generating
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_loading
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_review_headline
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_select_all
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_start_over
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_title
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Phase
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberZipSaveLauncher

@Composable
fun ExportRecipesScreen(bloc: ExportRecipesBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val saveZip = rememberZipSaveLauncher { saved -> bloc.onSaveCompleted(saved) }
    LaunchedEffect(state.pendingSave?.token) {
        val pending = state.pendingSave ?: return@LaunchedEffect
        saveZip(pending.fileName, pending.archive)
    }

    val reviewPhase = state.phase as? Phase.Review
    val selectedCount = reviewPhase?.recipes?.count { it.selected } ?: 0
    // FAB is anchored to the screen container so it animates independently of the recipe list as
    // the user toggles selections. Visibility is driven by "review phase + at least one selected"
    // so the action morphs in only once the export becomes meaningful.
    val showFab = reviewPhase != null && selectedCount > 0

    PlusHeaderContainer(
        modifier = modifier.testTag(ExportRecipesTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.export_recipes_title.asTextData(),
                onBackClick = bloc::onBack,
            ),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                ExportFab(
                    selectedCount = selectedCount,
                    isExporting = reviewPhase?.isExporting == true,
                    onClick = bloc::onExportClicked,
                )
            }
        },
        content = {
            when (val phase = state.phase) {
                Phase.Loading ->
                    StatusContent(
                        message = Res.string.export_recipes_loading.asTextData().localized()
                    )
                Phase.Empty -> EmptyContent()
                is Phase.Review ->
                    ReviewContent(
                        phase = phase,
                        onToggle = bloc::onRecipeToggled,
                        onToggleSelectAll = bloc::onToggleSelectAll,
                    )
                is Phase.Done ->
                    DoneContent(
                        exportedCount = phase.exportedCount,
                        onDone = bloc::onBack,
                        onStartOver = bloc::onStartOver,
                    )
                is Phase.Error ->
                    ErrorContent(
                        message = phase.message.localized(),
                        onTryAgain = bloc::onStartOver,
                    )
            }
        },
    )
}

@Composable
private fun ExportFab(selectedCount: Int, isExporting: Boolean, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = { if (!isExporting) onClick() },
        modifier = Modifier.testTag(ExportRecipesTestTags.EXPORT_BUTTON),
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = ChefMateTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.size(ChefMateTheme.dimens.paddingSmall))
            Text(Res.string.export_recipes_generating.asTextData().localized())
        } else {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.size(ChefMateTheme.dimens.paddingSmall))
            Text(
                PhraseModel(
                        resource = Res.string.export_recipes_export_button,
                        "count" to FixedString(selectedCount.toString()),
                    )
                    .localized()
            )
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            Res.string.export_recipes_empty_headline.asTextData().localized(),
            style = ChefMateTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        Text(
            Res.string.export_recipes_empty_body.asTextData().localized(),
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatusContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Text(message, style = ChefMateTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReviewContent(
    phase: Phase.Review,
    onToggle: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
) {
    val selectedCount = phase.recipes.count { it.selected }
    val allSelected = phase.recipes.isNotEmpty() && selectedCount == phase.recipes.size

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .padding(top = ChefMateTheme.dimens.paddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            Res.string.export_recipes_review_headline.asTextData().localized(),
            style = ChefMateTheme.typography.titleMedium,
        )
        Text(
            text =
                if (allSelected) {
                    Res.string.export_recipes_deselect_all.asTextData().localized()
                } else {
                    Res.string.export_recipes_select_all.asTextData().localized()
                },
            style = ChefMateTheme.typography.labelLarge,
            color = ChefMateTheme.colorScheme.primary,
            modifier =
                Modifier.clickable(enabled = !phase.isExporting) { onToggleSelectAll() }
                    .padding(ChefMateTheme.dimens.paddingSmall),
        )
    }

    phase.recipes.forEach { item ->
        HorizontalDivider()
        ReviewRow(item = item, enabled = !phase.isExporting, onToggle = { onToggle(item.id) })
    }
    HorizontalDivider()
}

@Composable
private fun ReviewRow(item: ExportRecipesBloc.ExportItem, enabled: Boolean, onToggle: () -> Unit) {
    val description = item.title
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(enabled = enabled) { onToggle() }
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                )
                .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.selected, onCheckedChange = { onToggle() }, enabled = enabled)
        Spacer(Modifier.size(ChefMateTheme.dimens.paddingSmall))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = ChefMateTheme.typography.titleMedium)
            Text(
                item.subtitle.localized(),
                style = ChefMateTheme.typography.bodySmall,
                color = ChefMateTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (item.imageUrl != null) {
            Spacer(Modifier.size(ChefMateTheme.dimens.paddingSmall))
            RecipeImage(
                imageUrl = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

@Composable
private fun DoneContent(exportedCount: Int, onDone: () -> Unit, onStartOver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.PlaylistAddCheck,
            contentDescription = null,
            tint = ChefMateTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Text(
            PhraseModel(
                    resource = Res.string.export_recipes_done_headline,
                    "count" to FixedString(exportedCount.toString()),
                )
                .localized(),
            style = ChefMateTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Button(onClick = onDone, modifier = Modifier.testTag(ExportRecipesTestTags.DONE_BUTTON)) {
            Text(Res.string.export_recipes_done_button.asTextData().localized())
        }
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        OutlinedButton(onClick = onStartOver) {
            Text(Res.string.export_recipes_start_over.asTextData().localized())
        }
    }
}

@Composable
private fun ErrorContent(message: String, onTryAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            message,
            style = ChefMateTheme.typography.bodyLarge,
            color = ChefMateTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Button(onClick = onTryAgain) {
            Text(Res.string.export_recipes_start_over.asTextData().localized())
        }
    }
}
