package com.plusmobileapps.chefmate.grocerylist.detail

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.grocerylist.GroceryItem
import com.plusmobileapps.chefmate.grocerylist.GroceryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class GroceryDetailViewModel(
    id: Long,
    mainContext: CoroutineContext,
    private val repository: GroceryRepository,
): ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    private val output = Channel<Output>(Channel.BUFFERED)

    val state: StateFlow<State> = _state.asStateFlow()

    val outputs: Flow<Output> = output.receiveAsFlow()

    init {
        loadGrocery(id)
    }

    override fun onCleared() {
        super.onCleared()
        output.close()
    }

    fun onGroceryNameChanged(name: String) {
        _state.value = _state.value.copy(
            groceryItem = _state.value.groceryItem.copy(name = name)
        )
    }

    fun onGroceryCheckedChanged(isChecked: Boolean) {
        _state.value = _state.value.copy(
            groceryItem = _state.value.groceryItem.copy(isChecked = isChecked)
        )
    }

    fun save() {
        val groceryItem = _state.value.groceryItem
        if (groceryItem.name.isBlank()) return
        scope.launch {
            repository.updateGrocery(groceryItem)
            output.send(Output.Finished)
        }
    }

    private fun loadGrocery(id: Long) {
        scope.launch {
            val grocery = repository.getGrocery(id)
            _state.value = State(isLoading = false, groceryItem = grocery ?: GroceryItem.empty)
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val groceryItem: GroceryItem = GroceryItem.empty,
    )

    sealed class Output {
        data object Finished : Output()
    }

}