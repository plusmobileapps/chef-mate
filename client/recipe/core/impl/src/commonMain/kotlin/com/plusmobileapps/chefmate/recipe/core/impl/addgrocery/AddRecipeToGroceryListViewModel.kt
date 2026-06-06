package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.IngredientParser
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.GroceryListItem
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.IngredientGroup
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.ListItem
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class AddRecipeToGroceryListViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository,
    private val settings: Settings,
) : ViewModel(mainContext) {
    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadGroceryLists()
        loadRecipe()
    }

    override fun onCleared() {
        super.onCleared()
        _output.close()
    }

    fun toggleIngredient(ingredientId: Int) {
        _state.update {
            it.copy(
                groupedIngredients =
                    it.groupedIngredients.map { group ->
                        group.copy(
                            items =
                                group.items
                                    .map { ingredient ->
                                        if (ingredient.id == ingredientId) {
                                            ingredient.copy(isSelected = !ingredient.isSelected)
                                        } else {
                                            ingredient
                                        }
                                    }
                                    .toImmutableList()
                        )
                    }
            )
        }
    }

    fun onGroceryListSelected(listId: Long) {
        val list = _state.value.groceryLists.firstOrNull { it.id == listId } ?: return
        _state.update { it.copy(selectedGroceryList = list) }
        settings.putLong(KEY_LAST_GROCERY_LIST_ID, listId)
    }

    fun save() {
        val ingredients =
            state.value.groupedIngredients
                .flatMap { it.items }
                .filter { it.isSelected }
                .map { it.name }
        val selectedListId = state.value.selectedGroceryList?.id
        val recipeName = state.value.recipe.title.takeIf { it.isNotBlank() }
        _state.update { it.copy(isAdding = true) }
        scope.launch {
            if (ingredients.isNotEmpty()) {
                if (selectedListId != null) {
                    groceryRepository.addGroceries(selectedListId, ingredients, recipeName)
                } else {
                    groceryRepository.addGroceries(ingredients)
                }
            }
            _output.send(Output.Added)
        }
    }

    private fun loadGroceryLists() {
        scope.launch {
            groceryRepository.ensureDefaultList()
            groceryRepository.getGroceryLists().collect { lists ->
                val groceryListItems = lists.map { GroceryListItem(id = it.id, name = it.name) }
                val lastSelectedId = settings.getLongOrNull(KEY_LAST_GROCERY_LIST_ID)
                val selected =
                    groceryListItems.firstOrNull { it.id == lastSelectedId }
                        ?: groceryListItems.firstOrNull()
                _state.update {
                    it.copy(
                        groceryLists = groceryListItems,
                        selectedGroceryList = it.selectedGroceryList ?: selected,
                    )
                }
            }
        }
    }

    private fun loadRecipe() {
        scope.launch {
            val recipe =
                recipeRepository.getRecipe(recipeId).first()
                    ?: run {
                        _output.send(Output.Finished)
                        return@launch
                    }
            val grouped =
                recipe.ingredients
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .map { raw ->
                        val parsed = IngredientParser.parse(raw)
                        ListItem(
                            id = raw.hashCode(),
                            name = raw,
                            displayName = parsed.name,
                            quantity = parsed.quantity,
                            isSelected = true,
                        ) to parsed.category
                    }
                    .groupBy { it.second }
                    .entries
                    .sortedBy { it.key.ordinal }
                    .map { (category, pairs) ->
                        IngredientGroup(
                            category = category,
                            items = pairs.map { it.first }.toImmutableList(),
                        )
                    }
            _state.update {
                it.copy(isLoading = false, recipe = recipe, groupedIngredients = grouped)
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isAdding: Boolean = false,
        val recipe: Recipe = Recipe.Empty,
        val groupedIngredients: List<IngredientGroup> = emptyList(),
        val groceryLists: List<GroceryListItem> = emptyList(),
        val selectedGroceryList: GroceryListItem? = null,
    )

    sealed class Output {
        data object Finished : Output()

        data object Added : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long): AddRecipeToGroceryListViewModel
    }

    companion object {
        private const val KEY_LAST_GROCERY_LIST_ID = "last_grocery_list_id"
    }
}
