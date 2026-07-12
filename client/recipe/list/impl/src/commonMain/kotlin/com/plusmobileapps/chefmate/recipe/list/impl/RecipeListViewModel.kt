package com.plusmobileapps.chefmate.recipe.list.impl

import chefmate.client.recipe.list.public.generated.resources.Res
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scan_failed
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scan_no_api_key
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.combineStates
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.isEnabled
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.PendingRecipePhotoStore
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionError
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import com.plusmobileapps.chefmate.recipe.data.RecipeImageExtractor
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookCollaborationRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookInvite
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.util.mimeTypeForImageExtension
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.string
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class RecipeListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
    private val recipeBookRepository: RecipeBookRepository,
    private val collaborationRepository: RecipeBookCollaborationRepository,
    private val categoryRepository: CategoryRepository,
    private val cookingSessionRepository: CookingSessionRepository,
    private val imageExtractor: RecipeImageExtractor,
    private val pendingPhotoStore: PendingRecipePhotoStore,
    featureFlags: FeatureFlags,
    settings: Settings,
    private val coachMarkController: CoachMarkController,
) : ViewModel(mainContext) {
    private val scanFromPhotoEnabled =
        featureFlags.isEnabled(FeatureFlagRegistry.ScanRecipeFromPhoto)
    private var isGridViewPref by settings.boolean(KEY_IS_GRID_VIEW, false)
    private var sortOptionPref by
        settings.string(KEY_SORT_OPTION, RecipeSortOption.RECENTLY_ADDED.name)
    private var activeFiltersPref by settings.string(KEY_ACTIVE_FILTERS, "")
    private var activeCategoriesPref by settings.string(KEY_ACTIVE_CATEGORIES, "")
    private var activeUserCategoriesPref by settings.string(KEY_ACTIVE_USER_CATEGORIES, "")

    private val _state =
        MutableStateFlow(
            State(
                isGridView = isGridViewPref,
                currentSort =
                    RecipeSortOption.entries.find { it.name == sortOptionPref }
                        ?: RecipeSortOption.RECENTLY_ADDED,
                activeFilters =
                    activeFiltersPref
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { name -> RecipeFilterOption.entries.find { it.name == name } }
                        .toSet(),
                activeCategories =
                    activeCategoriesPref
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { id -> BuiltinCategory.fromId(id) }
                        .toSet(),
                activeUserCategoryIds =
                    activeUserCategoriesPref
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.toLongOrNull() }
                        .toSet(),
            )
        )

    // Fold the shared controller's active mark into state so the screen shows at most one coach
    // mark at a time across the whole app.
    val state: StateFlow<State> =
        combineStates(_state, coachMarkController.activeCoachMark) { state, activeCoachMark ->
            state.copy(activeCoachMark = activeCoachMark)
        }

    private val _scannedRecipe =
        MutableSharedFlow<ExtractedRecipeData>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * Emits each time a photo scan succeeds. Collected by the BlocImpl, which forwards it as an
     * [com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output.OpenScannedRecipe].
     */
    val scannedRecipe: SharedFlow<ExtractedRecipeData> = _scannedRecipe.asSharedFlow()

    init {
        // Queue every recipe-list coach mark in order; the controller shows them one at a time.
        CoachMarkId.recipeListSequence.forEach(coachMarkController::request)
        scope.launch { observeRecipes() }
        scope.launch { observeCookingSession() }
        scope.launch { observeUserCategories() }
        scanFromPhotoEnabled
            .onEach { enabled -> _state.update { it.copy(isScanFromPhotoEnabled = enabled) } }
            .launchIn(scope)
        scope.launch { observeRecipeBooks() }
        scope.launch { loadPendingInvites() }
    }

    private suspend fun loadPendingInvites() {
        val invites =
            try {
                collaborationRepository.pendingInvites()
            } catch (_: Throwable) {
                emptyList()
            }
        _state.update { it.copy(pendingInvites = invites) }
    }

    fun acceptInvite(memberId: String) {
        scope.launch {
            try {
                collaborationRepository.acceptInvite(memberId)
            } catch (_: Throwable) {}
            loadPendingInvites()
        }
    }

    fun declineInvite(memberId: String) {
        scope.launch {
            try {
                collaborationRepository.declineInvite(memberId)
            } catch (_: Throwable) {}
            loadPendingInvites()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeRecipes() {
        recipeBookRepository.activeBookId
            .flatMapLatest { bookId ->
                if (bookId == null) repository.getRecipes() else repository.getRecipes(bookId)
            }
            .collect { recipes -> _state.update { it.copy(isLoading = false, recipes = recipes) } }
    }

    private suspend fun observeRecipeBooks() {
        combine(recipeBookRepository.getRecipeBooks(), recipeBookRepository.activeBookId) {
                books,
                activeId ->
                books to activeId
            }
            .collect { (books, activeId) ->
                _state.update { state ->
                    state.copy(
                        recipeBooks = books,
                        // A null active id is the explicit "All recipes" selection — span every
                        // book rather than falling back to the first one.
                        activeBook = books.firstOrNull { it.id == activeId },
                        isAllRecipesSelected = activeId == null,
                    )
                }
            }
    }

    private suspend fun observeCookingSession() {
        cookingSessionRepository.observeRecipeIds().collect { ids ->
            _state.update { it.copy(cookingRecipeIds = ids) }
        }
    }

    private suspend fun observeUserCategories() {
        categoryRepository.observeUserCategories().collect { categories ->
            _state.update { it.copy(availableUserCategories = categories) }
        }
    }

    fun showDoneCookingDialog() {
        _state.update { it.copy(showDoneCookingDialog = true) }
    }

    fun dismissDoneCookingDialog() {
        _state.update { it.copy(showDoneCookingDialog = false) }
    }

    fun confirmDoneCooking() {
        _state.update { it.copy(showDoneCookingDialog = false) }
        scope.launch { cookingSessionRepository.stopAll() }
    }

    fun deleteRecipe(recipeId: Long) {
        scope.launch { repository.deleteRecipe(recipeId) }
    }

    fun toggleFavorite(recipeId: Long) {
        scope.launch {
            val recipe = repository.getRecipe(recipeId).first() ?: return@launch
            repository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    fun updateSort(option: RecipeSortOption) {
        _state.update { it.copy(currentSort = option) }
        sortOptionPref = option.name
    }

    fun toggleFilter(filter: RecipeFilterOption) {
        _state.update { state ->
            val newFilters =
                if (filter in state.activeFilters) {
                    state.activeFilters - filter
                } else {
                    state.activeFilters + filter
                }
            state.copy(activeFilters = newFilters)
        }
        persistFilters()
    }

    fun clearFilters() {
        _state.update {
            it.copy(
                activeFilters = emptySet(),
                activeCategories = emptySet(),
                activeUserCategoryIds = emptySet(),
            )
        }
        persistFilters()
        persistCategories()
        persistUserCategories()
    }

    fun applySortAndFilters(
        sort: RecipeSortOption,
        filters: Set<RecipeFilterOption>,
        categories: Set<BuiltinCategory>,
        userCategoryIds: Set<Long>,
    ) {
        _state.update {
            it.copy(
                currentSort = sort,
                activeFilters = filters,
                activeCategories = categories,
                activeUserCategoryIds = userCategoryIds,
            )
        }
        sortOptionPref = sort.name
        persistFilters()
        persistCategories()
        persistUserCategories()
    }

    private fun persistFilters() {
        activeFiltersPref = _state.value.activeFilters.joinToString(",") { it.name }
    }

    private fun persistCategories() {
        activeCategoriesPref = _state.value.activeCategories.joinToString(",") { it.id }
    }

    private fun persistUserCategories() {
        activeUserCategoriesPref =
            _state.value.activeUserCategoryIds.joinToString(",") { it.toString() }
    }

    fun toggleViewMode() {
        _state.update { it.copy(isGridView = !it.isGridView) }
        isGridViewPref = _state.value.isGridView
    }

    /**
     * Mark a coach mark seen, but only if it's the one currently showing, so tapping a control
     * dismisses its own tip without consuming tips further down the queue.
     */
    fun dismissCoachMark(id: String) {
        if (coachMarkController.activeCoachMark.value == id) {
            coachMarkController.dismiss(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Leaving the screen without dismissing frees the queue so other coach marks can show.
        CoachMarkId.recipeListSequence.forEach(coachMarkController::release)
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSyncClicked() {
        scope.launch {
            _state.update { it.copy(isSyncing = true) }
            try {
                repository.syncAllUnsynced()
                // Refresh invites too so a pull-to-refresh surfaces a newly-arrived invitation
                // even if the initial load raced a cold sign-in.
                loadPendingInvites()
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    /**
     * Scans a recipe out of a picked photo via Gemini vision. On success the picked bytes are
     * stashed in [pendingPhotoStore] (so the editor can seed them as the recipe image) and the
     * recipe is emitted on [scannedRecipe]; on failure [State.scanError] is set.
     */
    fun scanRecipeFromPhoto(bytes: ByteArray, fileExtension: String) {
        if (_state.value.isScanning) return
        scope.launch {
            _state.update { it.copy(isScanning = true, scanError = null) }
            try {
                val recipe =
                    imageExtractor.extractFromImage(
                        bytes = bytes,
                        mimeType = mimeTypeForImageExtension(fileExtension),
                    )
                pendingPhotoStore.put(bytes = bytes, fileExtension = fileExtension)
                _scannedRecipe.tryEmit(recipe)
            } catch (e: RecipeExtractionException) {
                _state.update {
                    it.copy(
                        scanError =
                            if (e.error == RecipeExtractionError.MISSING_API_KEY)
                                ResourceString(Res.string.recipe_list_scan_no_api_key)
                            else ResourceString(Res.string.recipe_list_scan_failed)
                    )
                }
            } catch (_: Throwable) {
                _state.update {
                    it.copy(scanError = ResourceString(Res.string.recipe_list_scan_failed))
                }
            } finally {
                _state.update { it.copy(isScanning = false) }
            }
        }
    }

    fun dismissScanError() {
        _state.update { it.copy(scanError = null) }
    }

    fun openBookPicker() {
        _state.update { it.copy(isBookPickerOpen = true) }
    }

    fun dismissBookPicker() {
        _state.update { it.copy(isBookPickerOpen = false) }
    }

    fun selectBook(bookId: Long) {
        _state.update { it.copy(isBookPickerOpen = false) }
        scope.launch { recipeBookRepository.setActiveBook(bookId) }
    }

    fun selectAllRecipes() {
        _state.update { it.copy(isBookPickerOpen = false) }
        scope.launch { recipeBookRepository.selectAllRecipes() }
    }

    fun enterSelectionMode() {
        _state.update { it.copy(isSelectionMode = true, selectedRecipeIds = emptySet()) }
    }

    fun exitSelectionMode() {
        _state.update { it.copy(isSelectionMode = false, selectedRecipeIds = emptySet()) }
    }

    fun toggleRecipeSelected(recipeId: Long) {
        _state.update {
            val next =
                if (recipeId in it.selectedRecipeIds) it.selectedRecipeIds - recipeId
                else it.selectedRecipeIds + recipeId
            it.copy(selectedRecipeIds = next)
        }
    }

    /**
     * Selects every recipe currently visible; if all are already selected, clears the selection.
     */
    fun toggleSelectAllVisible() {
        _state.update { state ->
            val visibleIds = state.displayRecipes.map { it.id }.toSet()
            val allVisibleSelected =
                visibleIds.isNotEmpty() && state.selectedRecipeIds.containsAll(visibleIds)
            val next =
                if (allVisibleSelected) state.selectedRecipeIds - visibleIds
                else state.selectedRecipeIds + visibleIds
            state.copy(selectedRecipeIds = next)
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isSyncing: Boolean = false,
        val recipes: List<Recipe> = emptyList(),
        val currentSort: RecipeSortOption = RecipeSortOption.RECENTLY_ADDED,
        val activeFilters: Set<RecipeFilterOption> = emptySet(),
        val activeCategories: Set<BuiltinCategory> = emptySet(),
        val activeUserCategoryIds: Set<Long> = emptySet(),
        val availableUserCategories: List<Category> = emptyList(),
        val isGridView: Boolean = false,
        val searchQuery: String = "",
        val cookingRecipeIds: List<Long> = emptyList(),
        val showDoneCookingDialog: Boolean = false,
        val isSelectionMode: Boolean = false,
        val selectedRecipeIds: Set<Long> = emptySet(),
        val isScanning: Boolean = false,
        val scanError: TextData? = null,
        val isScanFromPhotoEnabled: Boolean = false,
        val recipeBooks: List<RecipeBook> = emptyList(),
        val activeBook: RecipeBook? = null,
        /** True when the cross-book "All recipes" view is active (no single book selected). */
        val isAllRecipesSelected: Boolean = false,
        val isBookPickerOpen: Boolean = false,
        val pendingInvites: List<RecipeBookInvite> = emptyList(),
        val activeCoachMark: String? = null,
    ) {
        val isSearchActive: Boolean
            get() = searchQuery.isNotBlank()

        val displayRecipes: List<Recipe>
            get() =
                recipes
                    .let { applySearch(it, searchQuery) }
                    .let { applyFilters(it, activeFilters) }
                    .let { applyCategoryFilter(it, activeCategories, activeUserCategoryIds) }
                    .let { applySort(it, currentSort) }
    }
}

private const val KEY_IS_GRID_VIEW = "recipe_list_is_grid_view"
private const val KEY_SORT_OPTION = "recipe_list_sort_option"
private const val KEY_ACTIVE_FILTERS = "recipe_list_active_filters"
private const val KEY_ACTIVE_CATEGORIES = "recipe_list_active_categories"
private const val KEY_ACTIVE_USER_CATEGORIES = "recipe_list_active_user_categories"

private fun applySearch(recipes: List<Recipe>, query: String): List<Recipe> {
    if (query.isBlank()) return recipes
    val lowerQuery = query.lowercase()
    return recipes.filter { recipe ->
        recipe.title.lowercase().contains(lowerQuery) ||
            recipe.description?.lowercase()?.contains(lowerQuery) == true
    }
}

private fun applyFilters(recipes: List<Recipe>, filters: Set<RecipeFilterOption>): List<Recipe> {
    if (filters.isEmpty()) return recipes
    return recipes.filter { recipe ->
        filters.all { filter ->
            when (filter) {
                RecipeFilterOption.FAVORITES -> recipe.isFavorite
                RecipeFilterOption.RATED -> recipe.starRating != null
                RecipeFilterOption.QUICK_RECIPES -> (recipe.totalTime ?: Int.MAX_VALUE) <= 30
            }
        }
    }
}

private fun applyCategoryFilter(
    recipes: List<Recipe>,
    presets: Set<BuiltinCategory>,
    userCategoryIds: Set<Long>,
): List<Recipe> {
    if (presets.isEmpty() && userCategoryIds.isEmpty()) return recipes
    return recipes.filter { recipe ->
        // OTHER preset acts as the "uncategorized" bucket — preserves the legacy behavior.
        val noCategories = recipe.categories.isEmpty()
        if (noCategories && BuiltinCategory.OTHER in presets) return@filter true

        val recipeBuiltins =
            recipe.categories.mapNotNull { BuiltinCategory.fromId(it.builtinId) }.toSet()
        val recipeIds = recipe.categories.map { it.id }.toSet()
        recipeBuiltins.any { it in presets } || recipeIds.any { it in userCategoryIds }
    }
}

private fun applySort(recipes: List<Recipe>, sort: RecipeSortOption): List<Recipe> =
    when (sort) {
        RecipeSortOption.RECENTLY_ADDED -> recipes.sortedByDescending { it.createdAt }
        RecipeSortOption.OLDEST_FIRST -> recipes.sortedBy { it.createdAt }
        RecipeSortOption.ALPHABETICAL_ASC -> recipes.sortedBy { it.title.lowercase() }
        RecipeSortOption.ALPHABETICAL_DESC -> recipes.sortedByDescending { it.title.lowercase() }
        RecipeSortOption.TOP_RATED ->
            // Highest rating first, descending by stars, then unrated recipes at the bottom.
            // Alphabetical title breaks ties, so unrated recipes are ordered alphabetically.
            recipes.sortedWith(
                compareByDescending<Recipe> { it.starRating ?: Int.MIN_VALUE }
                    .thenBy { it.title.lowercase() }
            )
    }
