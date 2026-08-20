package com.plusmobileapps.chefmate.family.core.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.family.core.FamilyBloc
import com.plusmobileapps.chefmate.family.core.FamilyBloc.Model
import com.plusmobileapps.chefmate.family.core.FamilyBloc.Output
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = FamilyBloc.Factory::class)
class FamilyBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<FamilyViewModel>,
) : FamilyBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<Model> = viewModel.state

    override fun onBack() {
        output.onNext(Output.Back)
    }

    override fun onSignInClicked() {
        output.onNext(Output.OpenSignIn)
    }

    override fun onSignUpClicked() {
        output.onNext(Output.OpenSignUp)
    }

    override fun onNewFamilyNameChanged(name: String) {
        viewModel.onNewFamilyNameChanged(name)
    }

    override fun onCreateFamilyClicked() {
        viewModel.createFamily()
    }

    override fun onRenameClicked() {
        viewModel.startRename()
    }

    override fun onEditingNameChanged(name: String) {
        viewModel.onEditingNameChanged(name)
    }

    override fun onRenameConfirmed() {
        viewModel.confirmRename()
    }

    override fun onRenameCancelled() {
        viewModel.cancelRename()
    }

    override fun onInviteEmailChanged(email: String) {
        viewModel.onInviteEmailChanged(email)
    }

    override fun onInviteClicked() {
        viewModel.invite()
    }

    override fun onRemoveMemberClicked(memberId: String) {
        viewModel.startRemoveMember(memberId)
    }

    override fun onConfirmRemoveMember() {
        viewModel.confirmRemoveMember()
    }

    override fun onDismissRemoveMember() {
        viewModel.dismissRemoveMember()
    }

    override fun onLeaveFamilyClicked() {
        viewModel.startFamilyAction(FamilyBloc.FamilyAction.LEAVE)
    }

    override fun onDeleteFamilyClicked() {
        viewModel.startFamilyAction(FamilyBloc.FamilyAction.DELETE)
    }

    override fun onConfirmFamilyAction() {
        viewModel.confirmFamilyAction()
    }

    override fun onDismissFamilyAction() {
        viewModel.dismissFamilyAction()
    }
}
