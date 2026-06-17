package com.plusmobileapps.chefmate.grocery.core.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListInvite
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface GroceryListBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        GroceryListScreen(bloc = this, modifier = modifier)
    }

    val newGroceryItemName: StateFlow<String>

    val childSlot: Value<ChildSlot<*, Sheet>>

    fun onGroceryItemCheckedChange(item: GroceryItem, isChecked: Boolean)

    fun onGroceryItemDelete(item: GroceryItem)

    fun onNewGroceryItemNameChange(name: String)

    fun saveGroceryItem()

    fun onGroceryItemClicked(item: GroceryItem)

    fun onDismissSheet()

    fun onSyncClicked()

    fun onSyncTooltipDismissed()

    fun onListSelected(list: GroceryListModel)

    fun onCreateListClicked()

    fun onCreateListDismissed()

    fun onCreateListConfirmed(name: String)

    fun onDeleteListClicked(list: GroceryListModel)

    fun onApplySortAndFilter(sort: GrocerySort, filter: GroceryFilter, recipeFilter: String? = null)

    fun onClearFiltersClicked()

    fun onDeleteClicked()

    fun onDeleteDismissed()

    fun onDeletePurchasedConfirmed()

    fun onDeleteAllConfirmed()

    fun onListSelectorClicked()

    fun onListSelectorDismissed()

    fun onBrowseRecipesClicked()

    fun onEditListClicked(list: GroceryListModel)

    fun onAcceptInvitation(invite: GroceryListInvite)

    fun onRejectInvitation(invite: GroceryListInvite)

    data class GroceryGroup(val category: GroceryCategory, val items: ImmutableList<GroceryItem>)

    enum class GrocerySort {
        AISLE,
        ALPHABETICAL,
    }

    enum class GroceryFilter {
        ALL,
        UNPURCHASED,
        PURCHASED,
    }

    data class Model(
        val groupedItems: ImmutableList<GroceryGroup> = persistentListOf(),
        val sort: GrocerySort = GrocerySort.AISLE,
        val filter: GroceryFilter = GroceryFilter.ALL,
        val recipeFilter: String? = null,
        val availableRecipes: ImmutableList<String> = persistentListOf(),
        val autocompleteSuggestions: ImmutableList<String> = persistentListOf(),
        val hasNoRecipeItems: Boolean = false,
        val isSyncing: Boolean = false,
        val lists: ImmutableList<GroceryListModel> = persistentListOf(),
        val selectedList: GroceryListModel? = null,
        val showCreateListDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showListSelector: Boolean = false,
        val pendingInvitations: List<GroceryListInvite> = emptyList(),
        val currentUserRole: ListRole = ListRole.OWNER,
        val showSyncTooltip: Boolean = false,
    )

    sealed class Output {
        data object OpenRecipes : Output()

        data class OpenEditList(val listId: Long) : Output()
    }

    sealed class Sheet {

        abstract val bloc: ComposeScreen

        data class GroceryDetail(override val bloc: GroceryDetailBloc) : Sheet()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): GroceryListBloc
    }

    companion object {
        const val NO_RECIPE_FILTER = ""
    }
}
