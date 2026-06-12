package com.plusmobileapps.chefmate.grocery.core.impl.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc.Output
import com.plusmobileapps.chefmate.grocery.core.impl.edit.ui.EditGroceryListScreen
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = EditGroceryListBloc.Factory::class,
)
class EditGroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted listId: Long,
    @Assisted private val output: Consumer<Output>,
    repository: GroceryRepository,
    authRepository: AuthenticationRepository,
) : EditGroceryListBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel {
        EditGroceryListViewModel(
            listId = listId,
            mainContext = mainContext,
            repository = repository,
            authRepository = authRepository,
        )
    }

    override val state: StateFlow<EditGroceryListBloc.Model> =
        viewModel.state.mapState {
            EditGroceryListBloc.Model(
                name = it.name,
                isAuthenticated = it.isAuthenticated,
                isOwner = it.isOwner,
                collaborators = it.collaborators,
                showDeleteConfirm = it.showDeleteConfirm,
                collaboratorPendingRemoval = it.collaboratorPendingRemoval,
                inviteEmail = it.inviteEmail,
                inviteRole = it.inviteRole,
                isInviting = it.isInviting,
                inviteError = it.inviteError,
            )
        }

    init {
        scope.launch {
            viewModel.outputs.collect {
                when (it) {
                    EditGroceryListViewModel.Output.Finished -> output.onNext(Output.Finished)
                }
            }
        }
    }

    override fun onNameChanged(name: String) {
        viewModel.onNameChanged(name)
    }

    override fun onRenameSubmitted() {
        viewModel.onRenameSubmitted()
    }

    override fun onDeleteClicked() {
        viewModel.onDeleteClicked()
    }

    override fun onDeleteConfirmed() {
        viewModel.onDeleteConfirmed()
    }

    override fun onDeleteDismissed() {
        viewModel.onDeleteDismissed()
    }

    override fun onInviteEmailChanged(email: String) {
        viewModel.onInviteEmailChanged(email)
    }

    override fun onInviteRoleChanged(role: ListRole) {
        viewModel.onInviteRoleChanged(role)
    }

    override fun onInviteClicked() {
        viewModel.onInviteClicked()
    }

    override fun onRemoveCollaboratorClicked(collaborator: ListCollaborator) {
        viewModel.onRemoveCollaboratorClicked(collaborator)
    }

    override fun onConfirmRemoveCollaborator() {
        viewModel.onConfirmRemoveCollaborator()
    }

    override fun onDismissRemoveCollaborator() {
        viewModel.onDismissRemoveCollaborator()
    }

    override fun onSignInClicked() {
        output.onNext(Output.OpenSignIn)
    }

    override fun onSignUpClicked() {
        output.onNext(Output.OpenSignUp)
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        EditGroceryListScreen(bloc = this, modifier = modifier)
    }
}
