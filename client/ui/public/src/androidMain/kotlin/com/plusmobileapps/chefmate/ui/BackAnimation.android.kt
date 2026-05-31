@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatableV2
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler

actual fun <C : Any, T : Any> backAnimation(
    backHandler: BackHandler,
    onBack: () -> Unit,
    fallbackAnimation: StackAnimation<C, T>?,
    isModal: (T) -> Boolean,
): StackAnimation<C, T> =
    predictiveBackAnimation(
        backHandler = backHandler,
        fallbackAnimation = fallbackAnimation ?: stackAnimation(slide()),
        selector = { backEvent, exitChild, enterChild ->
            if (isModal(exitChild.instance) || isModal(enterChild.instance)) {
                verticalPredictiveBackAnimatable(backEvent)
            } else {
                androidPredictiveBackAnimatableV2(backEvent)
            }
        },
        onBack = onBack,
    )

private fun verticalPredictiveBackAnimatable(
    initialBackEvent: BackEvent
): PredictiveBackAnimatable =
    predictiveBackAnimatable(
        initialBackEvent = initialBackEvent,
        exitModifier = { progress, _ -> Modifier.offsetYFactor(progress) },
        enterModifier = { _, _ -> Modifier },
    )

private fun Modifier.offsetYFactor(factor: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x = 0, y = (placeable.height.toFloat() * factor).toInt())
    }
}
