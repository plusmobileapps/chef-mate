package com.plusmobileapps.chefmate.ui

import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.essenty.backhandler.BackHandler

expect fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
    animator: StackAnimator? = null,
): StackAnimation<C, T>
