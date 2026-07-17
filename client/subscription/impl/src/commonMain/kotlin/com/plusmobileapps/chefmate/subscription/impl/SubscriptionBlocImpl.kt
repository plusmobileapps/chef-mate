package com.plusmobileapps.chefmate.subscription.impl

import chefmate.client.subscription.public.generated.resources.Res
import chefmate.client.subscription.public.generated.resources.subscription_error_message
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.subscription.SubscriptionBloc
import com.plusmobileapps.chefmate.subscription.SubscriptionBloc.Output
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = SubscriptionBloc.Factory::class,
)
class SubscriptionBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<SubscriptionViewModel>,
) : SubscriptionBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<SubscriptionBloc.Model> =
        viewModel.state.mapState {
            SubscriptionBloc.Model(
                isPremium = it.isPremium,
                isLoading = it.isLoading,
                packages = it.packages,
                selectedPackageId = it.selectedPackageId,
                isProcessing = it.isProcessing,
                error =
                    if (it.showError) ResourceString(Res.string.subscription_error_message)
                    else null,
            )
        }

    override fun onCloseClicked() {
        output.onNext(Output.Finished)
    }

    override fun onPackageSelected(packageId: String) {
        viewModel.selectPackage(packageId)
    }

    override fun onPurchaseClicked() {
        viewModel.purchase()
    }

    override fun onRestoreClicked() {
        viewModel.restore()
    }

    override fun onErrorDismissed() {
        viewModel.dismissError()
    }
}
