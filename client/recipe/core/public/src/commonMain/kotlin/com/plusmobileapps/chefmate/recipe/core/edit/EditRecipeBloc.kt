package com.plusmobileapps.chefmate.recipe.core.edit

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface EditRecipeBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    val title: StateFlow<String>

    val description: StateFlow<String>

    val imageUrl: StateFlow<String>

    val ingredients: StateFlow<String>

    val directions: StateFlow<String>

    val sourceUrl: StateFlow<String>

    val servings: StateFlow<String>

    val prepTime: StateFlow<String>

    val cookTime: StateFlow<String>

    val totalTime: StateFlow<String>

    val calories: StateFlow<String>

    val starRating: StateFlow<Int?>

    val categories: StateFlow<Set<Category>>

    /** User-created categories from the local DB. Built-in presets are not included. */
    val availableUserCategories: StateFlow<List<Category>>

    /** All of the user's recipe books, so the editor can offer them as a multi-select. */
    val recipeBooks: StateFlow<List<RecipeBook>>

    /** Local ids of the books this recipe belongs to. A recipe can be filed under several. */
    val selectedBookIds: StateFlow<Set<Long>>

    val pendingPhotoBytes: StateFlow<ByteArray?>

    /**
     * App-wide preference: true edits ingredients/directions/description in the WYSIWYG rich-text
     * editor, false uses the raw markdown editor with a preview toggle. Persisted across launches.
     */
    val richTextEditorMode: StateFlow<Boolean>

    fun onTitleChanged(title: String)

    fun onDescriptionChanged(description: String)

    fun onImageUrlChanged(imageUrl: String)

    fun onIngredientsChanged(ingredients: String)

    fun onDirectionsChanged(directions: String)

    fun onSourceUrlChanged(sourceUrl: String)

    fun onServingsChanged(servings: String)

    fun onPrepTimeChanged(prepTime: String)

    fun onCookTimeChanged(cookTime: String)

    fun onTotalTimeChanged(totalTime: String)

    fun onCaloriesChanged(calories: String)

    fun onStarRatingChanged(starRating: Int?)

    /**
     * Bulk replacement — used by the picker sheet when applying changes (currently unused but kept
     * for parity).
     */
    fun onCategoriesChanged(categories: Set<Category>)

    /** Materializes [builtin] if needed and adds it to the recipe's category set. */
    fun onAttachBuiltin(builtin: BuiltinCategory)

    /** Adds a user-created [category] to the recipe's category set. */
    fun onAttachCategory(category: Category)

    /** Removes [category] from the recipe's category set. */
    fun onDetachCategory(category: Category)

    /**
     * Creates a new user category named [name] and attaches it to the current recipe. The local DB
     * insert is offline-first; the new row appears in [availableUserCategories] as soon as it
     * lands, and remote sync happens in the background.
     */
    fun onCreateUserCategory(name: String)

    /** Renames a user-created category. Offline-first; remote sync happens in the background. */
    fun onRenameCategory(id: Long, newName: String)

    /**
     * Deletes a user-created category. Detaches it from the current recipe in-memory; other recipes
     * lose the attachment via the join table's cascade.
     */
    fun onDeleteCategory(id: Long)

    /** Toggles whether this recipe belongs to the book with local id [bookId]. */
    fun onToggleBook(bookId: Long)

    fun onDiscardChangesConfirmed()

    fun onDiscardChangesCancelled()

    fun onSaveClicked()

    fun onPhotoPicked(bytes: ByteArray, fileExtension: String)

    fun onUploadErrorDismissed()

    /** Persists the editor-mode preference (true = rich text, false = markdown). */
    fun onRichTextEditorModeChanged(richTextMode: Boolean)

    data class Model(
        val title: TextData,
        val isLoading: Boolean,
        val isSaving: Boolean,
        val showDiscardChangesDialog: Boolean,
        val uploadError: TextData? = null,
    )

    sealed class Output {
        data class Finished(val recipeId: Long) : Output()

        data object Cancelled : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long?,
            extractedRecipe: ExtractedRecipeData?,
            fromAi: Boolean,
            consumePendingPhoto: Boolean,
            output: Consumer<Output>,
        ): EditRecipeBloc
    }
}
