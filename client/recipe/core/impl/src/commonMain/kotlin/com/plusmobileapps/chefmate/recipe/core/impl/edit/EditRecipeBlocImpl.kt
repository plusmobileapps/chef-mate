package com.plusmobileapps.chefmate.recipe.core.impl.edit

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc.Output
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = EditRecipeBloc.Factory::class,
)
class EditRecipeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted recipeId: Long?,
    @Assisted private val output: Consumer<Output>
) : EditRecipeBloc, BlocContext by context {
    override val state: StateFlow<EditRecipeBloc.Model> = MutableStateFlow(
        EditRecipeBloc.Model(
            isLoading = false,
            isSaving = false,
            showDiscardChangesDialog = false,
        )
    )
    override val title: StateFlow<String> = MutableStateFlow("")
    override val description: StateFlow<String> = MutableStateFlow("")
    override val imageUrl: StateFlow<String> = MutableStateFlow("")
    override val ingredients: StateFlow<String> = MutableStateFlow("")
    override val directions: StateFlow<String> = MutableStateFlow("")

    override fun onTitleChanged(title: String) {
        TODO("Not yet implemented")
    }

    override fun onDescriptionChanged(description: String) {
        TODO("Not yet implemented")
    }

    override fun onImageUrlChanged(imageUrl: String) {
        TODO("Not yet implemented")
    }

    override fun onIngredientsChanged(ingredients: String) {
        TODO("Not yet implemented")
    }

    override fun onDirectionsChanged(directions: String) {
        TODO("Not yet implemented")
    }

    override fun onDiscardChangesConfirmed() {
        TODO("Not yet implemented")
    }

    override fun onDiscardChangesCancelled() {
        TODO("Not yet implemented")
    }

    override fun onSaveClicked() {
        TODO("Not yet implemented")
    }

    override fun onBackClicked() {
        output.onNext(Output.Cancelled)
    }
}