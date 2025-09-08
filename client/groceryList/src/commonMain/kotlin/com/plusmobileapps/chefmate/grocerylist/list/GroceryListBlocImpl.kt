package com.plusmobileapps.chefmate.grocerylist.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocerylist.GroceryItem
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = GroceryListBloc.Factory::class
)
class GroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<GroceryListBloc.Output>,
    private val repository: GroceryRepository
) : GroceryListBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel {
        GroceryListViewModel(mainContext, repository)
    }

    override val state: StateFlow<GroceryListBloc.Model> = viewModel.state.mapState {
        GroceryListBloc.Model(
            items = it.items
        )
    }

    override val newGroceryItemName: StateFlow<String> = viewModel.newGroceryItemName

    override fun onGroceryItemCheckedChange(
        item: GroceryItem,
        isChecked: Boolean
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
}