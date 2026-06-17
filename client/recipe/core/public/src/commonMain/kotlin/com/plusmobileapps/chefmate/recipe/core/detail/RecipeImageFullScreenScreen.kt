package com.plusmobileapps.chefmate.recipe.core.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_full_image_close
import coil3.compose.AsyncImage
import com.plusmobileapps.chefmate.ui.sharedElementBy
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RecipeImageFullScreenScreen(
    imageUrl: String,
    recipeId: Long,
    contentDescription: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 150.dp.toPx() }

    var scale by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    val animatedDragY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val dismissProgress = (abs(animatedDragY.value) / dismissThresholdPx).coerceIn(0f, 1f)

    Box(
        modifier =
            modifier.fillMaxSize().background(Color.Black.copy(alpha = 1f - dismissProgress * 0.6f))
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier.fillMaxSize()
                    .sharedElementBy("recipe-image-fullscreen-$recipeId")
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = pan.x
                        translationY = pan.y + animatedDragY.value
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, gPan, gZoom, _ ->
                            val newScale = (scale * gZoom).coerceIn(1f, 5f)
                            scale = newScale
                            pan = if (newScale > 1f) pan + gPan else Offset.Zero
                        }
                    }
                    .pointerInput(scale) {
                        if (scale <= 1.01f) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (abs(animatedDragY.value) >= dismissThresholdPx) {
                                        onDismiss()
                                    } else {
                                        scope.launch { animatedDragY.animateTo(0f, tween(200)) }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch { animatedDragY.animateTo(0f, tween(200)) }
                                },
                            ) { _, delta ->
                                scope.launch { animatedDragY.snapTo(animatedDragY.value + delta) }
                            }
                        }
                    },
        )

        IconButton(
            onClick = onDismiss,
            modifier =
                Modifier.align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .alpha(1f - dismissProgress),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.recipe_full_image_close),
                tint = Color.White,
            )
        }
    }

    LaunchedEffect(scale) {
        if (scale <= 1.01f && pan != Offset.Zero) {
            pan = Offset.Zero
        }
    }
}
