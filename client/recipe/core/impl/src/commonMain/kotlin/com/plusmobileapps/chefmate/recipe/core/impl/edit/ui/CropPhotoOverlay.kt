package com.plusmobileapps.chefmate.recipe.core.impl.edit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_crop_cancel
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_crop_confirm
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_crop_title
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.compose.resources.stringResource

private const val MAX_USER_ZOOM = 4f

@Composable
fun CropPhotoOverlay(
    bitmap: ImageBitmap,
    isProcessing: Boolean,
    onCancel: () -> Unit,
    onConfirm: (srcX: Int, srcY: Int, srcSize: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imgW = bitmap.width
    val imgH = bitmap.height
    var userScale by remember(bitmap) { mutableStateOf(1f) }
    var userOffset by remember(bitmap) { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onCancel() },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        BoxWithConstraints(
            modifier = modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            val frameSidePx = (min(constraints.maxWidth, constraints.maxHeight) * 0.85f).toInt()
            val baseScale =
                max(frameSidePx.toFloat() / imgW.toFloat(), frameSidePx.toFloat() / imgH.toFloat())
            val effectiveScale = baseScale * userScale

            Canvas(
                modifier =
                    Modifier.fillMaxSize().pointerInput(bitmap) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (userScale * zoom).coerceIn(1f, MAX_USER_ZOOM)
                            val nextEffective = baseScale * nextScale
                            val nextRenderedW = imgW * nextEffective
                            val nextRenderedH = imgH * nextEffective
                            val nextMaxX = max(0f, (nextRenderedW - frameSidePx) / 2f)
                            val nextMaxY = max(0f, (nextRenderedH - frameSidePx) / 2f)
                            userScale = nextScale
                            userOffset =
                                Offset(
                                    (userOffset.x + pan.x).coerceIn(-nextMaxX, nextMaxX),
                                    (userOffset.y + pan.y).coerceIn(-nextMaxY, nextMaxY),
                                )
                        }
                    }
            ) {
                val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                val renderedW = imgW * effectiveScale
                val renderedH = imgH * effectiveScale
                val imageTopLeft =
                    canvasCenter + userOffset - Offset(renderedW / 2f, renderedH / 2f)
                drawImage(
                    image = bitmap,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(imgW, imgH),
                    dstOffset = IntOffset(imageTopLeft.x.toInt(), imageTopLeft.y.toInt()),
                    dstSize = IntSize(renderedW.toInt(), renderedH.toInt()),
                )

                val frameSizeF = frameSidePx.toFloat()
                val frameTopLeft = canvasCenter - Offset(frameSizeF / 2f, frameSizeF / 2f)
                val dim = Color.Black.copy(alpha = 0.55f)
                drawRect(dim, Offset.Zero, Size(size.width, frameTopLeft.y))
                drawRect(
                    dim,
                    Offset(0f, frameTopLeft.y + frameSizeF),
                    Size(size.width, size.height - (frameTopLeft.y + frameSizeF)),
                )
                drawRect(dim, Offset(0f, frameTopLeft.y), Size(frameTopLeft.x, frameSizeF))
                drawRect(
                    dim,
                    Offset(frameTopLeft.x + frameSizeF, frameTopLeft.y),
                    Size(size.width - (frameTopLeft.x + frameSizeF), frameSizeF),
                )
                drawRect(
                    color = Color.White,
                    topLeft = frameTopLeft,
                    size = Size(frameSizeF, frameSizeF),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            Text(
                text = stringResource(Res.string.edit_recipe_crop_title),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(ChefMateTheme.dimens.paddingNormal),
            )

            Row(
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(ChefMateTheme.dimens.paddingNormal),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onCancel, enabled = !isProcessing) {
                    Text(
                        text = stringResource(Res.string.edit_recipe_crop_cancel),
                        color = Color.White,
                    )
                }
                Button(
                    enabled = !isProcessing,
                    onClick = {
                        val srcSizeF = frameSidePx.toFloat() / effectiveScale
                        val srcSizeInt = srcSizeF.toInt().coerceIn(1, min(imgW, imgH))
                        val srcCenterX = imgW / 2f - userOffset.x / effectiveScale
                        val srcCenterY = imgH / 2f - userOffset.y / effectiveScale
                        val srcX =
                            (srcCenterX - srcSizeInt / 2f).toInt().coerceIn(0, imgW - srcSizeInt)
                        val srcY =
                            (srcCenterY - srcSizeInt / 2f).toInt().coerceIn(0, imgH - srcSizeInt)
                        onConfirm(srcX, srcY, srcSizeInt)
                    },
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(ChefMateTheme.dimens.paddingSmall))
                    }
                    Text(stringResource(Res.string.edit_recipe_crop_confirm))
                }
            }
        }
    }
}
