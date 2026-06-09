package com.plusmobileapps.chefmate.recipe.core.impl.edit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_cancel
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_confirm
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_message
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_discard_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_books
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_books_none
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_calories
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_calories_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_add
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_none
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_remove_a11y
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
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_resize_handle_a11y
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_servings
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_servings_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_source_url
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_source_url_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_title_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_total_time
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_total_time_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_save
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_upload_photo
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_upload_photo_dismiss
import coil3.compose.AsyncImage
import com.plusmobileapps.chefmate.recipe.categories.pickerLabelRes
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeTestTags
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.cropImageToSquare
import com.plusmobileapps.chefmate.util.decodeImageBitmap
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    if (state.showDiscardChangesDialog) {
        DiscardChangesDialog(
            onConfirm = bloc::onDiscardChangesConfirmed,
            onDismiss = bloc::onDiscardChangesCancelled,
        )
    }

    state.uploadError?.let { error ->
        UploadErrorDialog(message = error.localized(), onDismiss = bloc::onUploadErrorDismissed)
    }

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize().imePadding().testTag(EditRecipeTestTags.SCREEN),
        data = PlusHeaderData.Child(title = state.title, onBackClick = bloc::onBackClicked),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
        floatingActionButton = {
            SaveRecipeFab(isSaving = state.isSaving, onSaveClicked = bloc::onSaveClicked)
        },
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
    ExtendedFloatingActionButton(onClick = onSaveClicked, modifier = modifier) {
        if (isSaving) {
            PlusLoadingIndicator(
                modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall)
            )
        }
        Text(stringResource(Res.string.edit_recipe_save))
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditRecipeContent(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
    ) {
        RecipeTitleField(bloc = bloc)
        RecipeDescriptionField(bloc = bloc)
        RecipeBooksField(bloc = bloc)
        RecipeCategoryField(bloc = bloc)
        RecipeStarRatingField(bloc = bloc)
        RecipePhotoUploader(bloc = bloc)
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
private fun RecipeTitleField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
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
private fun RecipeDescriptionField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val description by bloc.description.collectAsState()

    OutlinedTextField(
        value = description,
        onValueChange = bloc::onDescriptionChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_description)) },
        placeholder = {
            Text(stringResource(Res.string.edit_recipe_field_description_placeholder))
        },
        modifier = modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeCategoryField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val categories by bloc.categories.collectAsState()
    val userCategories by bloc.availableUserCategories.collectAsState()
    var showSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        Text(
            text = stringResource(Res.string.edit_recipe_field_category),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (categories.isEmpty()) {
            Text(
                text = stringResource(Res.string.edit_recipe_field_category_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
            ) {
                categories.forEach { category ->
                    val label = category.displayLabel()
                    InputChip(
                        selected = true,
                        onClick = { bloc.onDetachCategory(category) },
                        label = { Text(label) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription =
                                    stringResource(
                                        Res.string.edit_recipe_field_category_remove_a11y,
                                        label,
                                    ),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }

        TextButton(onClick = { showSheet = true }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(Res.string.edit_recipe_field_category_add),
                modifier = Modifier.padding(start = ChefMateTheme.dimens.paddingExtraSmall),
            )
        }
    }

    if (showSheet) {
        CategoryPickerSheet(
            selectedCategories = categories,
            userCategories = userCategories,
            onAttachBuiltin = bloc::onAttachBuiltin,
            onAttachCategory = bloc::onAttachCategory,
            onDetachCategory = bloc::onDetachCategory,
            onCreateUserCategory = bloc::onCreateUserCategory,
            onRenameCategory = bloc::onRenameCategory,
            onDeleteCategory = bloc::onDeleteCategory,
            onDismiss = { showSheet = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeBooksField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val books by bloc.recipeBooks.collectAsState()
    val selected by bloc.selectedBookIds.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        Text(
            text = stringResource(Res.string.edit_recipe_field_books),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (books.isEmpty()) {
            Text(
                text = stringResource(Res.string.edit_recipe_field_books_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
            ) {
                books.forEach { book ->
                    FilterChip(
                        selected = book.id in selected,
                        onClick = { bloc.onToggleBook(book.id) },
                        label = { Text(book.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun com.plusmobileapps.chefmate.recipe.data.Category.displayLabel(): String {
    val builtin = BuiltinCategory.fromId(builtinId)
    return if (builtin != null) stringResource(builtin.pickerLabelRes()) else name
}

@Composable
private fun RecipeStarRatingField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val starRating by bloc.starRating.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        Text(
            text = "Rating",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
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
                        Modifier.size(40.dp).clickable {
                            bloc.onStarRatingChanged(if (starRating == i) null else i)
                        },
                )
            }
        }
    }
}

private data class PendingCrop(val bytes: ByteArray, val bitmap: ImageBitmap)

@Composable
private fun RecipePhotoUploader(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val imageUrl by bloc.imageUrl.collectAsState()
    val pendingBytes by bloc.pendingPhotoBytes.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingCrop by remember { mutableStateOf<PendingCrop?>(null) }
    var isCropping by remember { mutableStateOf(false) }
    val pickPhoto = rememberImagePickerLauncher { picked ->
        if (picked != null) {
            scope.launch {
                val bitmap =
                    runCatching {
                            withContext(Dispatchers.Default) { decodeImageBitmap(picked.bytes) }
                        }
                        .getOrNull()
                if (bitmap != null) {
                    pendingCrop = PendingCrop(bytes = picked.bytes, bitmap = bitmap)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        val previewModel: Any? = pendingBytes ?: imageUrl.takeIf { it.isNotBlank() }
        if (previewModel != null) {
            AsyncImage(
                model = previewModel,
                contentDescription = null,
                modifier =
                    Modifier.fillMaxWidth().aspectRatio(1f).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
        }
        OutlinedButton(onClick = pickPhoto, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall),
            )
            Text(stringResource(Res.string.edit_recipe_upload_photo))
        }
    }

    pendingCrop?.let { crop ->
        CropPhotoOverlay(
            bitmap = crop.bitmap,
            isProcessing = isCropping,
            onCancel = { pendingCrop = null },
            onConfirm = { srcX, srcY, srcSize ->
                isCropping = true
                scope.launch {
                    try {
                        val cropped =
                            withContext(Dispatchers.Default) {
                                cropImageToSquare(
                                    bytes = crop.bytes,
                                    srcX = srcX,
                                    srcY = srcY,
                                    srcSize = srcSize,
                                )
                            }
                        bloc.onPhotoPicked(cropped, "jpg")
                    } finally {
                        isCropping = false
                        pendingCrop = null
                    }
                }
            },
        )
    }
}

@Composable
private fun UploadErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.edit_recipe_upload_photo_dismiss))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun RecipeImageUrlField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
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
private fun RecipeSourceUrlField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
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
private fun RecipeServingsField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val servings by bloc.servings.collectAsState()

    OutlinedTextField(
        value = servings,
        onValueChange = bloc::onServingsChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_servings)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_servings_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun RecipePrepTimeField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val prepTime by bloc.prepTime.collectAsState()

    OutlinedTextField(
        value = prepTime,
        onValueChange = bloc::onPrepTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_prep_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_prep_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun RecipeCookTimeField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val cookTime by bloc.cookTime.collectAsState()

    OutlinedTextField(
        value = cookTime,
        onValueChange = bloc::onCookTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_cook_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_cook_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun RecipeTotalTimeField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val totalTime by bloc.totalTime.collectAsState()

    OutlinedTextField(
        value = totalTime,
        onValueChange = bloc::onTotalTimeChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_total_time)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_total_time_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun RecipeCaloriesField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val calories by bloc.calories.collectAsState()

    OutlinedTextField(
        value = calories,
        onValueChange = bloc::onCaloriesChanged,
        label = { Text(stringResource(Res.string.edit_recipe_field_calories)) },
        placeholder = { Text(stringResource(Res.string.edit_recipe_field_calories_placeholder)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
}

@Composable
private fun RecipeIngredientsField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val ingredients by bloc.ingredients.collectAsState()

    ResizableMultilineField(
        value = ingredients,
        onValueChange = bloc::onIngredientsChanged,
        label = stringResource(Res.string.edit_recipe_field_ingredients),
        placeholder = stringResource(Res.string.edit_recipe_field_ingredients_placeholder),
        modifier = modifier,
    )
}

@Composable
private fun RecipeDirectionsField(bloc: EditRecipeBloc, modifier: Modifier = Modifier) {
    val directions by bloc.directions.collectAsState()

    ResizableMultilineField(
        value = directions,
        onValueChange = bloc::onDirectionsChanged,
        label = stringResource(Res.string.edit_recipe_field_directions),
        placeholder = stringResource(Res.string.edit_recipe_field_directions_placeholder),
        modifier = modifier,
    )
}

/**
 * A tall multiline text field with a drag handle in the bottom-end corner. Dragging the handle
 * grows or shrinks the field between [minHeight] and [maxHeight]; text scrolls internally once it
 * exceeds the current height. The chosen height survives configuration changes via
 * [rememberSaveable].
 */
@Composable
private fun ResizableMultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 160.dp,
    maxHeight: Dp = 480.dp,
    initialHeight: Dp = 200.dp,
) {
    var heightDp by rememberSaveable { mutableStateOf(initialHeight.value) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth().height(heightDp.dp),
        )
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription =
                stringResource(Res.string.edit_recipe_field_resize_handle_a11y, label),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.align(Alignment.BottomEnd)
                    .padding(ChefMateTheme.dimens.paddingSmall)
                    .size(20.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            heightDp =
                                (heightDp + dragAmount.y.toDp().value).coerceIn(
                                    minHeight.value,
                                    maxHeight.value,
                                )
                        }
                    },
        )
    }
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

private val previewBloc =
    object : EditRecipeBloc {
        override val state: StateFlow<EditRecipeBloc.Model> =
            MutableStateFlow(
                EditRecipeBloc.Model(
                    title = FixedString("Edit Recipe"),
                    isLoading = false,
                    isSaving = false,
                    showDiscardChangesDialog = false,
                )
            )
        override val title: StateFlow<String> = MutableStateFlow("Spaghetti Carbonara")
        override val description: StateFlow<String> =
            MutableStateFlow(
                "A classic Italian pasta dish with eggs, cheese, pancetta, and black pepper"
            )
        override val imageUrl: StateFlow<String> =
            MutableStateFlow("https://example.com/carbonara.jpg")
        override val ingredients: StateFlow<String> =
            MutableStateFlow(
                """400g spaghetti
200g pancetta
4 large eggs
100g Pecorino Romano cheese
Black pepper to taste
Salt for pasta water"""
            )
        override val directions: StateFlow<String> =
            MutableStateFlow(
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
        override val sourceUrl: StateFlow<String> =
            MutableStateFlow("https://example.com/recipe/carbonara")
        override val servings: StateFlow<String> = MutableStateFlow("4")
        override val prepTime: StateFlow<String> = MutableStateFlow("10 minutes")
        override val cookTime: StateFlow<String> = MutableStateFlow("15 minutes")
        override val totalTime: StateFlow<String> = MutableStateFlow("25 minutes")
        override val calories: StateFlow<String> = MutableStateFlow("550")
        override val starRating: StateFlow<Int?> = MutableStateFlow(4)
        override val categories: StateFlow<Set<com.plusmobileapps.chefmate.recipe.data.Category>> =
            MutableStateFlow(
                setOf(
                    com.plusmobileapps.chefmate.recipe.data.Category(
                        id = 1L,
                        name = "Dinner",
                        builtinId = BuiltinCategory.DINNER.id,
                    )
                )
            )
        override val availableUserCategories:
            StateFlow<List<com.plusmobileapps.chefmate.recipe.data.Category>> =
            MutableStateFlow(emptyList())
        override val recipeBooks:
            StateFlow<List<com.plusmobileapps.chefmate.recipebook.data.RecipeBook>> =
            MutableStateFlow(com.plusmobileapps.chefmate.recipebook.data.RecipeBook.Samples)
        override val selectedBookIds: StateFlow<Set<Long>> = MutableStateFlow(setOf(1L))
        override val pendingPhotoBytes: StateFlow<ByteArray?> = MutableStateFlow(null)

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

        override fun onCategoriesChanged(
            categories: Set<com.plusmobileapps.chefmate.recipe.data.Category>
        ) {}

        override fun onAttachBuiltin(builtin: BuiltinCategory) {}

        override fun onAttachCategory(category: com.plusmobileapps.chefmate.recipe.data.Category) {}

        override fun onDetachCategory(category: com.plusmobileapps.chefmate.recipe.data.Category) {}

        override fun onCreateUserCategory(name: String) {}

        override fun onRenameCategory(id: Long, newName: String) {}

        override fun onDeleteCategory(id: Long) {}

        override fun onToggleBook(bookId: Long) {}

        override fun onDiscardChangesConfirmed() {}

        override fun onDiscardChangesCancelled() {}

        override fun onSaveClicked() {}

        override fun onPhotoPicked(bytes: ByteArray, fileExtension: String) {}

        override fun onUploadErrorDismissed() {}

        override fun onBackClicked() {}

        @Composable override fun Content(modifier: Modifier) = EditRecipeScreen(this, modifier)
    }

@Preview
@Composable
private fun EditRecipeScreenLightPreview() {
    ChefMateTheme(darkTheme = false) { EditRecipeScreen(bloc = previewBloc) }
}

@Preview
@Composable
private fun EditRecipeScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) { EditRecipeScreen(bloc = previewBloc) }
}
