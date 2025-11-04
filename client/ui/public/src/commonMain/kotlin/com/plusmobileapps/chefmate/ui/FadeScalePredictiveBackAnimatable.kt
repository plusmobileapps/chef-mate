@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.essenty.backhandler.BackEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun fadeScalePredictiveBackAnimatable(initialEvent: BackEvent): PredictiveBackAnimatable = FadeScaleAnimation(initialEvent)

class FadeScaleAnimation(
    initialEvent: BackEvent,
) : PredictiveBackAnimatable {
    private val exitProgressAnimatable = Animatable(initialValue = initialEvent.progress)
    private val enterProgressAnimatable = Animatable(initialValue = initialEvent.progress)
    private val finishProgressAnimatable = Animatable(initialValue = 0F)
    private val exitProgress: Float by derivedStateOf { exitProgressAnimatable.value }
    private val enterProgress: Float by derivedStateOf { enterProgressAnimatable.value }
    private val finishProgress: Float by derivedStateOf { finishProgressAnimatable.value }

    override val exitModifier: Modifier
        get() =
            Modifier.graphicsLayer {
                alpha = 1F - exitProgress
                scaleX = 1F - exitProgress * 0.1F
                scaleY = scaleX
            }

    override val enterModifier: Modifier
        get() =
            Modifier.graphicsLayer {
                alpha = enterProgress
                scaleX = 0.9F + enterProgress * 0.1F
                scaleY = scaleX
            }

    override suspend fun animate(event: BackEvent) {
        coroutineScope {
            launch { exitProgressAnimatable.animateTo(targetValue = event.progress) }
            launch { enterProgressAnimatable.animateTo(targetValue = event.progress) }
        }
    }

    override suspend fun finish() {
        coroutineScope {
            launch { exitProgressAnimatable.animateTo(targetValue = 1F) }
            launch { finishProgressAnimatable.animateTo(targetValue = 1F) }
        }
    }

    override suspend fun cancel() {
        coroutineScope {
            launch { exitProgressAnimatable.animateTo(targetValue = 0F) }
            launch { enterProgressAnimatable.animateTo(targetValue = 0F) }
        }
    }
}
