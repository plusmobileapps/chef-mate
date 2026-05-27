package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = AiChatBloc.Factory::class)
class AiChatBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<AiChatBloc.Output>,
    viewModelFactory: Provider<AiChatViewModel>,
) : AiChatBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<AiChatBloc.Model> = viewModel.state

    override val inputText: StateFlow<String> = viewModel.inputText

    override fun onInputChange(text: String) {
        viewModel.onInputChange(text)
    }

    override fun onSendClick() {
        viewModel.send()
    }

    override fun onClearClick() {
        viewModel.clear()
    }

    override fun onBackClicked() {
        output.onNext(AiChatBloc.Output.Back)
    }
}
