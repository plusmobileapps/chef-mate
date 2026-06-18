package com.plusmobileapps.chefmate.recipebook.edit.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc.Model
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc.Output
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc.Props
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = EditRecipeBookBloc.Factory::class,
)
class EditRecipeBookBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val props: Props,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: EditRecipeBookViewModel.Factory,
) : EditRecipeBookBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(props) { output.onNext(Output.Finished) }
    }

    override val state: StateFlow<Model> = viewModel.state.mapState { it.toBlocModel() }

    override fun onNameChanged(name: String) = viewModel.onNameChanged(name)

    override fun onSaveClicked() = viewModel.onSaveClicked()

    override fun onCloseClicked() {
        output.onNext(Output.Finished)
    }

    override fun onInviteEmailChanged(email: String) = viewModel.onInviteEmailChanged(email)

    override fun onInviteRoleChanged(role: RecipeBookRole) = viewModel.onInviteRoleChanged(role)

    override fun onInviteClicked() = viewModel.onInviteClicked()

    override fun onRemoveMemberClicked(memberId: String) = viewModel.onRemoveMemberClicked(memberId)

    override fun onConfirmRemoveMember() = viewModel.onConfirmRemoveMember()

    override fun onDismissRemoveMember() = viewModel.onDismissRemoveMember()

    private fun EditRecipeBookViewModel.State.toBlocModel(): Model =
        Model(
            title = title,
            name = name,
            isCreate = isCreate,
            isSaving = isSaving,
            nameError = nameError,
            canManageCollaborators = canManageCollaborators,
            members = members,
            inviteEmail = inviteEmail,
            inviteRole = inviteRole,
            isInviting = isInviting,
            inviteError = inviteError,
            removingMember = removingMember,
        )
}
