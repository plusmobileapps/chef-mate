package com.plusmobileapps.chefmate.ui

import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.essenty.backhandler.BackHandler

expect fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
    fallbackAnimation: StackAnimation<C, T>? = null,
    isModal: (T) -> Boolean = { false },
): StackAnimation<C, T>
