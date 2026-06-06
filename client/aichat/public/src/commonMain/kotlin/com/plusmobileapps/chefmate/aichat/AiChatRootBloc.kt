package com.plusmobileapps.chefmate.aichat

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.ui.BlocScreen

interface AiChatRootBloc : BackHandlerOwner, BackClickBloc, BlocScreen {
    val routerState: Value<ChildStack<*, Child>>

    sealed class Child : BlocScreen {
        data class Chat(val bloc: AiChatBloc) : Child(), BlocScreen by bloc

        data class History(val bloc: AiChatHistoryBloc) : Child(), BlocScreen by bloc
    }

    sealed class Output {
        data object Finished : Output()

        data class AddAsRecipe(
            val extracted: ExtractedRecipeData,
            /** True when extraction came from a photo whose bytes should seed the recipe image. */
            val consumePendingPhoto: Boolean = false,
        ) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AiChatRootBloc
    }
}
