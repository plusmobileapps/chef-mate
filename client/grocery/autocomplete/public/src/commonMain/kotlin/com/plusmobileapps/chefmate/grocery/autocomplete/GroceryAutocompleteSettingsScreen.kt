package com.plusmobileapps.chefmate.grocery.autocomplete

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.autocomplete.public.generated.resources.Res
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_create_a11y
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_create_cancel_a11y
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_create_confirm_a11y
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_create_placeholder
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_delete
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_delete_cancel
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_delete_confirm
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_delete_message
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_delete_title
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_empty_user
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_more_a11y
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_section_defaults
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_section_user
import chefmate.client.grocery.autocomplete.public.generated.resources.grocery_autocomplete_title
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.CreateState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.DialogState
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteBloc.Item
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainerDefaults
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroceryAutocompleteSettingsScreen(
    bloc: GroceryAutocompleteBloc,
    modifier: Modifier = Modifier,
) {
    val model by bloc.state.collectAsState()
    GroceryAutocompleteContent(model = model, handlers = blocHandlers(bloc), modifier = modifier)
}

internal data class GroceryAutocompleteHandlers(
    val onBackClicked: () -> Unit,
    val onCreateClicked: () -> Unit,
    val onCreateCancelled: () -> Unit,
    val onCreateTextChanged: (String) -> Unit,
    val onCreateSubmitted: () -> Unit,
    val onDeleteRequested: (Item) -> Unit,
    val onDeleteConfirmed: () -> Unit,
    val onDeleteDismissed: () -> Unit,
)

private fun blocHandlers(bloc: GroceryAutocompleteBloc): GroceryAutocompleteHandlers =
    GroceryAutocompleteHandlers(
        onBackClicked = bloc::onBackClicked,
        onCreateClicked = bloc::onCreateClicked,
        onCreateCancelled = bloc::onCreateCancelled,
        onCreateTextChanged = bloc::onCreateTextChanged,
        onCreateSubmitted = bloc::onCreateSubmitted,
        onDeleteRequested = bloc::onDeleteRequested,
        onDeleteConfirmed = bloc::onDeleteConfirmed,
        onDeleteDismissed = bloc::onDeleteDismissed,
    )

@Composable
internal fun GroceryAutocompleteContent(
    model: GroceryAutocompleteBloc.Model,
    handlers: GroceryAutocompleteHandlers,
    modifier: Modifier = Modifier,
) {
    PlusHeaderContainer(
        modifier = modifier.testTag(GroceryAutocompleteTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.grocery_autocomplete_title.asTextData(),
                onBackClick = handlers.onBackClicked,
            ),
        // Disable the container's outer scroll so the inner LazyColumn owns scrolling (Compose
        // throws on a scrollable nested under infinite-height constraints).
        scrollEnabled = false,
        maxContentWidth = Dp.Unspecified,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            CreateFieldRow(
                createState = model.createState,
                onTextChanged = handlers.onCreateTextChanged,
                onSubmit = handlers.onCreateSubmitted,
                onCancel = handlers.onCreateCancelled,
            )
            ItemList(
                userItems = model.userItems,
                defaults = model.defaults,
                createOpen = model.createState is CreateState.Editing,
                onCreateClicked = handlers.onCreateClicked,
                onDeleteRequested = handlers.onDeleteRequested,
            )
        },
    )

    when (val dialog = model.dialog) {
        DialogState.None -> Unit
        is DialogState.Delete ->
            PlusDialog(
                title = ResourceString(Res.string.grocery_autocomplete_delete_title),
                message =
                    PhraseModel(
                        Res.string.grocery_autocomplete_delete_message,
                        "item" to FixedString(dialog.target.name),
                    ),
                confirmButtonText = ResourceString(Res.string.grocery_autocomplete_delete_confirm),
                dismissButtonText = ResourceString(Res.string.grocery_autocomplete_delete_cancel),
                onConfirmClick = handlers.onDeleteConfirmed,
                onDismissRequest = handlers.onDeleteDismissed,
            )
    }
}

@Composable
private fun CreateFieldRow(
    createState: CreateState,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    AnimatedContent(
        targetState = createState,
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth).fillMaxWidth(),
        contentKey = { it is CreateState.Editing },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        if (state is CreateState.Editing) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            val text = state.text
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            start = ChefMateTheme.dimens.paddingNormal,
                            end = ChefMateTheme.dimens.paddingSmall,
                        )
                        .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(stringResource(Res.string.grocery_autocomplete_create_placeholder))
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier =
                        Modifier.weight(1f)
                            .focusRequester(focusRequester)
                            .testTag(GroceryAutocompleteTestTags.CREATE_FIELD),
                )
                IconButton(onClick = { if (text.isNotBlank()) onSubmit() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription =
                            stringResource(Res.string.grocery_autocomplete_create_confirm_a11y),
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription =
                            stringResource(Res.string.grocery_autocomplete_create_cancel_a11y),
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemList(
    userItems: List<Item>,
    defaults: List<String>,
    createOpen: Boolean,
    onCreateClicked: () -> Unit,
    onDeleteRequested: (Item) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        stickyHeader(key = "header-user") {
            SectionHeader(
                title = stringResource(Res.string.grocery_autocomplete_section_user),
                trailing = {
                    if (!createOpen) {
                        IconButton(
                            onClick = onCreateClicked,
                            modifier = Modifier.testTag(GroceryAutocompleteTestTags.ADD_BUTTON),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription =
                                    stringResource(Res.string.grocery_autocomplete_create_a11y),
                            )
                        }
                    }
                },
            )
        }
        if (userItems.isEmpty()) {
            item(key = "user-empty") { EmptyUserSectionHint() }
        } else {
            items(userItems, key = { "user-${it.id}" }) { item ->
                UserItemRow(item = item, onDeleteClicked = { onDeleteRequested(item) })
            }
        }
        stickyHeader(key = "header-defaults") {
            SectionHeader(
                title = stringResource(Res.string.grocery_autocomplete_section_defaults),
                trailing = {},
            )
        }
        items(defaults, key = { "default-$it" }) { name -> DefaultItemRow(name = name) }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: @Composable () -> Unit) {
    Surface(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth).fillMaxWidth(),
        color = ChefMateTheme.colorScheme.background,
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(start = ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ChefMateTheme.typography.titleSmall,
                color = ChefMateTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
    }
}

@Composable
private fun EmptyUserSectionHint() {
    Box(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                )
    ) {
        Text(
            text = stringResource(Res.string.grocery_autocomplete_empty_user),
            style = MaterialTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UserItemRow(item: Item, onDeleteClicked: () -> Unit) {
    Row(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(start = ChefMateTheme.dimens.paddingNormal)
                .testTag(GroceryAutocompleteTestTags.USER_ROW),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        UserItemOverflowMenu(itemName = item.name, onDeleteClicked = onDeleteClicked)
    }
}

@Composable
private fun UserItemOverflowMenu(itemName: String, onDeleteClicked: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription =
                    PhraseModel(
                            Res.string.grocery_autocomplete_more_a11y,
                            "item" to FixedString(itemName),
                        )
                        .localized(),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.grocery_autocomplete_delete)) },
                onClick = {
                    expanded = false
                    onDeleteClicked()
                },
            )
        }
    }
}

@Composable
private fun DefaultItemRow(name: String) {
    Row(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .testTag(GroceryAutocompleteTestTags.DEFAULT_ROW),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}
