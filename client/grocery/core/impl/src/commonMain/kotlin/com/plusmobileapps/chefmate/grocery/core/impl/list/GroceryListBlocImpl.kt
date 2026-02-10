package com.plusmobileapps.chefmate.grocery.core.impl.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class GroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<GroceryListBloc.Output>,
    viewModelFactory: Provider<GroceryListViewModel>,
) : GroceryListBloc,
    BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<GroceryListBloc.Output>): GroceryListBlocImpl
    }

    private val viewModel =
        instanceKeeper.getViewModel {
            viewModelFactory()
        }

    override val state: StateFlow<GroceryListBloc.Model> =
        viewModel.state.mapState {
            GroceryListBloc.Model(
                items = it.items,
                isSyncing = it.isSyncing,
                lists = it.lists,
                selectedList = it.selectedList,
                showCreateListDialog = it.showCreateListDialog,
            )
        }

    override val newGroceryItemName: StateFlow<String> = viewModel.newGroceryItemName

    override fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean,
    ) {
        viewModel.onGroceryItemCheckedChange(item, isChecked)
    }

    override fun onGroceryItemDelete(item: GroceryItem) {
        viewModel.onGroceryItemDelete(item)
    }

    override fun onNewGroceryItemNameChange(name: String) {
        if (name.contains("\n")) {
            viewModel.saveGroceryItem()
        } else {
            viewModel.onNewGroceryItemNameChange(name)
        }
    }

    override fun saveGroceryItem() {
        viewModel.saveGroceryItem()
    }

    override fun onGroceryItemClicked(item: GroceryItem) {
        output.onNext(GroceryListBloc.Output.OpenDetail(item.id))
    }

    override fun onSyncClicked() {
        viewModel.onSyncClicked()
    }

    override fun onListSelected(list: GroceryListModel) {
        viewModel.onListSelected(list)
    }

    override fun onCreateListClicked() {
        viewModel.onCreateListClicked()
    }

    override fun onCreateListDismissed() {
        viewModel.onCreateListDismissed()
    }

    override fun onCreateListConfirmed(name: String) {
        viewModel.onCreateListConfirmed(name)
    }

    override fun onDeleteListClicked(list: GroceryListModel) {
        viewModel.onDeleteListClicked(list)
    }
}

@ContributesTo(AppScope::class)
interface GroceryListBlocBindingModule {
    @Provides
    fun provideGroceryListBlocFactory(factory: GroceryListBlocImpl.ManagedFactory): GroceryListBloc.Factory =
        GroceryListBloc.Factory { context, output -> factory.create(context, output) }
}
