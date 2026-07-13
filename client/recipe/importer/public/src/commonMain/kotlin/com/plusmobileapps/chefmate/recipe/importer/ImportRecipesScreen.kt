package com.plusmobileapps.chefmate.recipe.importer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.importer.`public`.generated.resources.Res
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_books_label
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_books_required
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_choose_file
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_deselect_all
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_done_button
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_done_headline
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_empty_body
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_empty_format_hint
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_empty_headline
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_import_button
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_parsing
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_review_headline
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_select_all
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_start_over
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_title
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberZipPickerLauncher

@Composable
fun ImportRecipesScreen(bloc: ImportRecipesBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val pickArchive = rememberZipPickerLauncher { picked ->
        if (picked != null) bloc.onArchiveSelected(picked.bytes, picked.fileName)
    }

    PlusHeaderContainer(
        modifier = modifier.testTag(ImportRecipesTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.import_recipes_title.asTextData(),
                onBackClick = bloc::onBack,
            ),
        content = {
            when (val phase = state.phase) {
                Phase.Empty -> EmptyContent(onChooseFile = pickArchive)
                Phase.Parsing ->
                    StatusContent(
                        message = Res.string.import_recipes_parsing.asTextData().localized()
                    )
                is Phase.Review -> {
                    val books by bloc.recipeBooks.collectAsState()
                    val selectedBookIds by bloc.selectedBookIds.collectAsState()
                    ReviewContent(
                        phase = phase,
                        books = books,
                        selectedBookIds = selectedBookIds,
                        onToggle = bloc::onRecipeToggled,
                        onToggleSelectAll = bloc::onToggleSelectAll,
                        onToggleBook = bloc::onToggleBook,
                        onImport = bloc::onImportClicked,
                    )
                }
                is Phase.Done ->
                    DoneContent(
                        importedCount = phase.importedCount,
                        onDone = bloc::onBack,
                        onStartOver = bloc::onStartOver,
                    )
                is Phase.Error ->
                    ErrorContent(message = phase.message.localized(), onChooseFile = pickArchive)
            }
        },
    )
}

@Composable
private fun EmptyContent(onChooseFile: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            Res.string.import_recipes_empty_headline.asTextData().localized(),
            style = ChefMateTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        Text(
            Res.string.import_recipes_empty_body.asTextData().localized(),
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        Text(
            Res.string.import_recipes_empty_format_hint.asTextData().localized(),
            style = ChefMateTheme.typography.bodySmall,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Button(
            onClick = onChooseFile,
            modifier = Modifier.testTag(ImportRecipesTestTags.CHOOSE_FILE_BUTTON),
        ) {
            Text(Res.string.import_recipes_choose_file.asTextData().localized())
        }
    }
}

@Composable
private fun StatusContent(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlusLoadingIndicator()
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Text(message, style = ChefMateTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReviewContent(
    phase: Phase.Review,
    books: List<RecipeBook>,
    selectedBookIds: Set<Long>,
    onToggle: (String) -> Unit,
    onToggleSelectAll: () -> Unit,
    onToggleBook: (Long) -> Unit,
    onImport: () -> Unit,
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
            Res.string.import_recipes_review_headline.asTextData().localized(),
            style = ChefMateTheme.typography.titleMedium,
        )
        Text(
            text =
                if (allSelected) {
                    Res.string.import_recipes_deselect_all.asTextData().localized()
                } else {
                    Res.string.import_recipes_select_all.asTextData().localized()
                },
            style = ChefMateTheme.typography.labelLarge,
            color = ChefMateTheme.colorScheme.primary,
            modifier =
                Modifier.clickable(enabled = !phase.isImporting) { onToggleSelectAll() }
                    .padding(ChefMateTheme.dimens.paddingSmall),
        )
    }

    phase.recipes.forEach { item ->
        HorizontalDivider()
        ReviewRow(item = item, enabled = !phase.isImporting, onToggle = { onToggle(item.id) })
    }
    HorizontalDivider()

    if (books.isNotEmpty()) {
        BookSelector(
            books = books,
            selectedBookIds = selectedBookIds,
            enabled = !phase.isImporting,
            onToggleBook = onToggleBook,
        )
    }

    // A book is required so imported recipes always have an explicit home; the default book is
    // pre-selected, but the user can swap it out as long as one remains chosen.
    val bookRequired = books.isNotEmpty() && selectedBookIds.isEmpty()

    Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
    Button(
        onClick = onImport,
        enabled = selectedCount > 0 && !phase.isImporting && !bookRequired,
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .testTag(ImportRecipesTestTags.IMPORT_BUTTON),
    ) {
        if (phase.isImporting) {
            PlusLoadingIndicator(modifier = Modifier.size(20.dp))
        } else {
            Text(
                PhraseModel(
                        resource = Res.string.import_recipes_import_button,
                        "count" to FixedString(selectedCount.toString()),
                    )
                    .localized()
            )
        }
    }
    Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookSelector(
    books: List<RecipeBook>,
    selectedBookIds: Set<Long>,
    enabled: Boolean,
    onToggleBook: (Long) -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .padding(top = ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        Text(
            Res.string.import_recipes_books_label.asTextData().localized(),
            style = ChefMateTheme.typography.titleMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
        ) {
            books.forEach { book ->
                FilterChip(
                    selected = book.id in selectedBookIds,
                    onClick = { onToggleBook(book.id) },
                    enabled = enabled,
                    label = { Text(book.name) },
                )
            }
        }
        if (selectedBookIds.isEmpty()) {
            Text(
                Res.string.import_recipes_books_required.asTextData().localized(),
                style = ChefMateTheme.typography.bodySmall,
                color = ChefMateTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ReviewRow(item: ImportRecipesBloc.ImportItem, enabled: Boolean, onToggle: () -> Unit) {
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
        if (item.hasImage) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = ChefMateTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DoneContent(importedCount: Int, onDone: () -> Unit, onStartOver: () -> Unit) {
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
                    resource = Res.string.import_recipes_done_headline,
                    "count" to FixedString(importedCount.toString()),
                )
                .localized(),
            style = ChefMateTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingNormal))
        Button(onClick = onDone, modifier = Modifier.testTag(ImportRecipesTestTags.DONE_BUTTON)) {
            Text(Res.string.import_recipes_done_button.asTextData().localized())
        }
        Spacer(Modifier.height(ChefMateTheme.dimens.paddingSmall))
        OutlinedButton(onClick = onStartOver) {
            Text(Res.string.import_recipes_start_over.asTextData().localized())
        }
    }
}

@Composable
private fun ErrorContent(message: String, onChooseFile: () -> Unit) {
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
        Button(
            onClick = onChooseFile,
            modifier = Modifier.testTag(ImportRecipesTestTags.CHOOSE_FILE_BUTTON),
        ) {
            Text(Res.string.import_recipes_choose_file.asTextData().localized())
        }
    }
}
