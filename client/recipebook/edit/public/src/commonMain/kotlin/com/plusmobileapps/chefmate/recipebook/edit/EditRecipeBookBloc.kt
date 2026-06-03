package com.plusmobileapps.chefmate.recipebook.edit

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface EditRecipeBookBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onNameChanged(name: String)

    fun onSaveClicked()

    fun onCloseClicked()

    data class Model(
        val title: TextData,
        val name: String = "",
        val isCreate: Boolean = true,
        val isSaving: Boolean = false,
        val nameError: TextData? = null,
    ) {
        val canSave: Boolean
            get() = name.isNotBlank() && !isSaving
    }

    sealed class Output {
        /** The book was created or renamed (or the user closed the modal). Pop the modal. */
        data object Finished : Output()
    }

    /**
     * Drives create vs. edit. [Create] opens a blank form; [Edit] loads the book's current name.
     */
    @Serializable
    sealed class Props {
        @Serializable data object Create : Props()

        @Serializable data class Edit(val bookId: Long) : Props()
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): EditRecipeBookBloc
    }
}
