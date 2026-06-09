package com.plusmobileapps.chefmate.profile

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface ManageProfileBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onBack()

    fun onDisplayNameChanged(displayName: String)

    fun onSaveClicked()

    data class Model(
        val displayName: String = "",
        val email: String = "",
        val isSaving: Boolean = false,
        val saveError: TextData? = null,
    ) {
        val canSave: Boolean
            get() = displayName.isNotBlank() && !isSaving
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ManageProfileBloc
    }
}
