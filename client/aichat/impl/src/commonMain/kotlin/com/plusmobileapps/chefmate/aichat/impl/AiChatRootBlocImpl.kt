@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.aichat.impl

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.aichat.AiChatBloc
import com.plusmobileapps.chefmate.aichat.AiChatHistoryBloc
import com.plusmobileapps.chefmate.aichat.AiChatRootBloc
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AiChatRootBloc.Factory::class,
)
class AiChatRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeContextId: Long?,
    @Assisted private val output: Consumer<AiChatRootBloc.Output>,
    private val chatFactory: AiChatBloc.Factory,
    private val historyFactory: AiChatHistoryBloc.Factory,
    private val repository: AiChatRepository,
) : AiChatRootBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = {
                // Reopen the recipe's most recent conversation if one exists, so returning to the
                // chat picks up where it left off (still grounded in the recipe); otherwise start a
                // fresh one. The lookup is a single indexed local read.
                val existingConversationId =
                    recipeContextId?.let(repository::latestConversationIdForRecipe)
                listOf(
                    Configuration.Chat(
                        conversationId = existingConversationId,
                        recipeContextId = recipeContextId,
                    )
                )
            },
            handleBackButton = true,
            key = "AiChatRootRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, AiChatRootBloc.Child>> = stack

    override fun onBackClicked() {
        // Default back behaviour is delegated to childStack via handleBackButton, but the public
        // BackClickBloc API still needs an implementation for e.g. the back arrow.
        if (stack.value.backStack.isNotEmpty()) {
            navigation.pop()
        } else {
            output.onNext(AiChatRootBloc.Output.Finished)
        }
    }

    private fun createChild(config: Configuration, context: BlocContext): AiChatRootBloc.Child =
        when (config) {
            is Configuration.Chat ->
                AiChatRootBloc.Child.Chat(
                    bloc =
                        chatFactory.create(
                            context = context,
                            props =
                                config.conversationId?.let {
                                    AiChatBloc.Props.ExistingConversation(it)
                                }
                                    ?: AiChatBloc.Props.NewConversation(
                                        recipeContextId = config.recipeContextId
                                    ),
                            output = ::handleChatOutput,
                        )
                )
            Configuration.History ->
                AiChatRootBloc.Child.History(
                    bloc = historyFactory.create(context = context, output = ::handleHistoryOutput)
                )
        }

    private fun handleChatOutput(out: AiChatBloc.Output) {
        when (out) {
            AiChatBloc.Output.Back -> output.onNext(AiChatRootBloc.Output.Finished)
            AiChatBloc.Output.OpenHistory -> navigation.bringToFront(Configuration.History)
            AiChatBloc.Output.NewConversation ->
                navigation.navigate { listOf(freshChatForRecipe()) }
            is AiChatBloc.Output.AddAsRecipe ->
                output.onNext(
                    AiChatRootBloc.Output.AddAsRecipe(
                        extracted = out.extracted,
                        consumePendingPhoto = out.consumePendingPhoto,
                    )
                )
        }
    }

    private fun handleHistoryOutput(out: AiChatHistoryBloc.Output) {
        when (out) {
            AiChatHistoryBloc.Output.Back -> navigation.pop()
            is AiChatHistoryBloc.Output.OpenConversation ->
                navigation.navigate { listOf(Configuration.Chat(out.conversationId)) }
            AiChatHistoryBloc.Output.NewConversation ->
                navigation.navigate { listOf(freshChatForRecipe()) }
        }
    }

    // A brand-new conversation that keeps this sheet's recipe grounding, so "new chat" from the
    // recipe stays about that recipe rather than dropping to an ungrounded chat.
    private fun freshChatForRecipe() =
        Configuration.Chat(conversationId = null, recipeContextId = recipeContextId)

    @Serializable
    private sealed class Configuration {
        @Serializable
        data class Chat(val conversationId: Long?, val recipeContextId: Long? = null) :
            Configuration()

        @Serializable data object History : Configuration()
    }
}
