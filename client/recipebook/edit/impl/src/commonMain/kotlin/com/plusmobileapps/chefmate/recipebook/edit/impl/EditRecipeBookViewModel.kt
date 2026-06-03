package com.plusmobileapps.chefmate.recipebook.edit.impl

import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_create_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_edit_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_error
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class EditRecipeBookViewModel(
    @Assisted private val props: EditRecipeBookBloc.Props,
    @Assisted private val onSaved: () -> Unit,
    @Main mainContext: CoroutineContext,
    @IO private val ioContext: CoroutineContext,
    private val repository: RecipeBookRepository,
) : ViewModel(mainContext) {

    private val isCreate = props is EditRecipeBookBloc.Props.Create

    private val _state =
        MutableStateFlow(
            State(
                title =
                    ResourceString(
                        if (isCreate) Res.string.edit_recipe_book_create_title
                        else Res.string.edit_recipe_book_edit_title
                    ),
                isCreate = isCreate,
            )
        )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        if (props is EditRecipeBookBloc.Props.Edit) {
            scope.launch {
                val book = withContext(ioContext) { repository.getRecipeBook(props.bookId).first() }
                if (book != null) {
                    _state.update { it.copy(name = book.name) }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name, nameError = null) }
    }

    fun onSaveClicked() {
        val current = _state.value
        if (current.isSaving) return
        val name = current.name.trim()
        if (name.isEmpty()) {
            _state.update {
                it.copy(nameError = ResourceString(Res.string.edit_recipe_book_name_error))
            }
            return
        }
        _state.update { it.copy(isSaving = true, nameError = null) }
        scope.launch {
            withContext(ioContext) {
                when (val p = props) {
                    is EditRecipeBookBloc.Props.Create -> repository.createBook(name)
                    is EditRecipeBookBloc.Props.Edit -> repository.renameBook(p.bookId, name)
                }
            }
            onSaved()
        }
    }

    data class State(
        val title: TextData,
        val name: String = "",
        val isCreate: Boolean = true,
        val isSaving: Boolean = false,
        val nameError: TextData? = null,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(props: EditRecipeBookBloc.Props, onSaved: () -> Unit): EditRecipeBookViewModel
    }
}
