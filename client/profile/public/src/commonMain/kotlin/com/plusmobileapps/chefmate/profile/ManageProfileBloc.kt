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

    fun onDeleteAccountClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    data class Model(
        val displayName: String = "",
        val email: String = "",
        val isSaving: Boolean = false,
        val saveError: TextData? = null,
        val showDeleteDialog: Boolean = false,
        val isDeleting: Boolean = false,
        val deleteError: TextData? = null,
    ) {
        val canSave: Boolean
            get() = displayName.isNotBlank() && !isSaving && !isDeleting
    }

    sealed class Output {
        data object Back : Output()

        data object AccountDeleted : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ManageProfileBloc
    }
}
