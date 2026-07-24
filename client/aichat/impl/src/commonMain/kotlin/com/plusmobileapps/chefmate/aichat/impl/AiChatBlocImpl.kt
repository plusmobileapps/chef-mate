package com.plusmobileapps.chefmate.aichat.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = AiChatBloc.Factory::class)
class AiChatBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: AiChatBloc.Props,
    @Assisted private val output: Consumer<AiChatBloc.Output>,
    viewModelFactory: AiChatViewModel.Factory,
) : AiChatBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory.create(props) }
    private val scope = createScope()

    init {
        viewModel.extractedRecipe.onEach { output.onNext(it) }.launchIn(scope)
    }

    override val state: StateFlow<AiChatBloc.Model> = viewModel.state

    override val inputText: StateFlow<String> = viewModel.inputText

    override fun onInputChange(text: String) {
        viewModel.onInputChange(text)
    }

    override fun onSendClick() {
        viewModel.send()
    }

    override fun onHistoryClick() {
        output.onNext(AiChatBloc.Output.OpenHistory)
    }

    override fun onNewChatClick() {
        output.onNext(AiChatBloc.Output.NewConversation)
    }

    override fun onAddRecipeClick() {
        viewModel.extractRecipe()
    }

    override fun onPhotoPicked(bytes: ByteArray, fileExtension: String) {
        viewModel.extractFromImage(bytes = bytes, fileExtension = fileExtension)
    }

    override fun onBackClicked() {
        output.onNext(AiChatBloc.Output.Back)
    }
}
