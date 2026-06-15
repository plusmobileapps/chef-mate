package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/** Which side of the anchor the tooltip bubble sits on. The caret always points at the anchor. */
enum class PlusTooltipPlacement {
    ABOVE,
    BELOW,
}

/**
 * An onboarding "coach mark": a small pop-up bubble that floats next to the composable it wraps and
 * points at it with a caret. Use it to draw a first-time user's attention to a specific control.
 *
 * The bubble is rendered in a [Popup] so it escapes the bounds of the anchor's parent (e.g. a
 * floating toolbar) and can overlay other content. The popup is non-focusable, so taps still reach
 * the underlying control — a user can tap the highlighted control directly.
 *
 * Visibility is fully caller-controlled. Pass [visible] = true to show it and handle [onDismiss]
 * (called when the bubble itself is tapped) to hide it. Callers that want it shown only once should
 * persist that decision themselves.
 *
 * @param text the message shown in the bubble.
 * @param visible whether the bubble is currently shown.
 * @param onDismiss invoked when the user taps the bubble to dismiss it.
 * @param placement which side of the anchor the bubble sits on. Defaults to [ABOVE].
 * @param anchor the control the tooltip points at; always laid out, tooltip or not.
 */
@Composable
fun PlusOnboardingTooltip(
    text: TextData,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placement: PlusTooltipPlacement = PlusTooltipPlacement.ABOVE,
    anchor: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        anchor()

        if (visible) {
            val density = LocalDensity.current
            val gapPx = with(density) { ChefMateTheme.dimens.paddingSmall.roundToPx() }
            val edgeMarginPx = with(density) { ChefMateTheme.dimens.paddingNormal.roundToPx() }

            // -1 = "not positioned yet" → the caret renders centered. The position provider
            // updates this to the anchor's center relative to the bubble's left edge once the
            // popup is laid out, keeping the caret pointing at the anchor even when the bubble is
            // clamped to a screen edge.
            var caretOffsetPx by remember { mutableIntStateOf(-1) }
            val positionProvider =
                remember(placement, gapPx, edgeMarginPx) {
                    TooltipPositionProvider(
                        placement = placement,
                        anchorGapPx = gapPx,
                        edgeMarginPx = edgeMarginPx,
                        onCaretOffset = { caretOffsetPx = it },
                    )
                }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismiss,
                properties = PopupProperties(focusable = false, clippingEnabled = false),
            ) {
                PlusTooltipBubble(
                    text = text,
                    placement = placement,
                    caretOffsetPx = caretOffsetPx,
                    onClick = onDismiss,
                )
            }
        }
    }
}

private val CaretWidth: Dp = 16.dp
private val CaretHeight: Dp = 8.dp
private val MaxBubbleWidth: Dp = 260.dp

/**
 * The bare tooltip bubble visual — a rounded message surface with a caret on one edge. Exposed
 * separately from [PlusOnboardingTooltip] so it can be rendered directly (e.g. in snapshot tests)
 * without the surrounding [Popup].
 *
 * @param caretOffsetPx horizontal position of the caret tip, in pixels from the bubble's left edge.
 *   A negative value (the default) centers the caret.
 */
@Composable
fun PlusTooltipBubble(
    text: TextData,
    placement: PlusTooltipPlacement,
    modifier: Modifier = Modifier,
    caretOffsetPx: Int = -1,
    onClick: () -> Unit = {},
) {
    val color = MaterialTheme.colorScheme.primaryContainer
    val caret: @Composable () -> Unit = {
        Caret(
            color = color,
            pointingUp = placement == PlusTooltipPlacement.BELOW,
            caretOffsetPx = caretOffsetPx,
        )
    }

    // IntrinsicSize.Max pins the column to the text surface's width so the full-width caret canvas
    // doesn't stretch the bubble to the popup window's width.
    Column(modifier = modifier.width(IntrinsicSize.Max).clickable(onClick = onClick)) {
        if (placement == PlusTooltipPlacement.BELOW) caret()
        Surface(
            color = color,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = ChefMateTheme.shapes.medium,
            shadowElevation = ChefMateTheme.dimens.paddingExtraSmall,
        ) {
            Text(
                text = text.localized(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.widthIn(max = MaxBubbleWidth)
                        .padding(
                            horizontal = ChefMateTheme.dimens.paddingNormal,
                            vertical = ChefMateTheme.dimens.paddingSmall,
                        ),
            )
        }
        if (placement == PlusTooltipPlacement.ABOVE) caret()
    }
}

@Composable
private fun Caret(
    color: Color,
    pointingUp: Boolean,
    caretOffsetPx: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(CaretHeight)) {
        val halfWidth = CaretWidth.toPx() / 2f
        // Centre the caret when the popup hasn't been positioned yet (or in preview), otherwise
        // align it with the anchor while keeping it fully inside the bubble.
        val centerX =
            if (caretOffsetPx < 0) size.width / 2f
            else caretOffsetPx.toFloat().coerceIn(halfWidth, size.width - halfWidth)
        val path =
            Path().apply {
                if (pointingUp) {
                    moveTo(centerX, 0f)
                    lineTo(centerX - halfWidth, size.height)
                    lineTo(centerX + halfWidth, size.height)
                } else {
                    moveTo(centerX, size.height)
                    lineTo(centerX - halfWidth, 0f)
                    lineTo(centerX + halfWidth, 0f)
                }
                close()
            }
        drawPath(path = path, color = color)
    }
}

private class TooltipPositionProvider(
    private val placement: PlusTooltipPlacement,
    private val anchorGapPx: Int,
    private val edgeMarginPx: Int,
    private val onCaretOffset: (Int) -> Unit,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
        val maxX =
            (windowSize.width - popupContentSize.width - edgeMarginPx).coerceAtLeast(edgeMarginPx)
        val x = (anchorCenterX - popupContentSize.width / 2).coerceIn(edgeMarginPx, maxX)
        val y =
            when (placement) {
                PlusTooltipPlacement.ABOVE ->
                    anchorBounds.top - popupContentSize.height - anchorGapPx
                PlusTooltipPlacement.BELOW -> anchorBounds.bottom + anchorGapPx
            }
        onCaretOffset(anchorCenterX - x)
        return IntOffset(x, y)
    }
}
