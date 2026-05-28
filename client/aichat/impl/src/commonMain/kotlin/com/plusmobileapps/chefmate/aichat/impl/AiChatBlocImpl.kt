package com.plusmobileapps.chefmate.aichat.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.impl.ui.AiChatScreen
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = AiChatBloc.Factory::class)
class AiChatBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<AiChatBloc.Output>,
    viewModelFactory: Provider<AiChatViewModel>,
) : AiChatBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }
    private val scope = createScope()

    init {
        viewModel.extractedRecipe
            .onEach { output.onNext(AiChatBloc.Output.AddAsRecipe(it)) }
            .launchIn(scope)
    }

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

    override fun onAddRecipeClick() {
        viewModel.extractRecipe()
    }

    override fun onBackClicked() {
        output.onNext(AiChatBloc.Output.Back)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        AiChatScreen(bloc = this, modifier = modifier)
    }
}
