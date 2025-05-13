package com.singularityuniverse.lib.userguide

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

fun Modifier.userGuide(state: UserGuideState): Modifier = composed {
    val targetRect = state.getCurrentTargetRect()

    LaunchedEffect(state.animationState, targetRect) {
        when (state.animationState) {
            GuideAnimationState.ENTERING -> {
                if (state.animatedRect == Rect.Zero) {
                    state.setAnimatedRect(state.fullScreenRect)
                }

                val anim = Animatable(
                    initialValue = state.animatedRect,
                    typeConverter = Rect.VectorConverter
                )

                anim.animateTo(
                    targetValue = targetRect,
                    animationSpec = state.transitionAnimationSpec
                ) {
                    state.setAnimatedRect(value)
                }

                state.setAnimationState(GuideAnimationState.NAVIGATING)
            }

            GuideAnimationState.NAVIGATING -> {
                val anim = Animatable(
                    initialValue = state.animatedRect,
                    typeConverter = Rect.VectorConverter
                )

                anim.animateTo(
                    targetValue = targetRect,
                    animationSpec = state.navigationAnimationSpec
                ) {
                    state.setAnimatedRect(value)
                }
            }

            GuideAnimationState.EXITING -> {
                val anim = Animatable(
                    initialValue = state.animatedRect,
                    typeConverter = Rect.VectorConverter
                )

                anim.animateTo(
                    targetValue = state.fullScreenRect,
                    animationSpec = state.transitionAnimationSpec
                ) {
                    state.setAnimatedRect(value)
                }

                state.finishExit()
            }

            GuideAnimationState.IDLE -> {
            }
        }
    }

    drawWithContent {
        drawContent()

        if (state.isShowing) {
            drawTargetHighlight(state.animatedRect)
        }
    }
}.onSizeChanged {
    state.setFullScreenRect(
        Rect(offset = Offset(0f, 0f), size = it.toSize())
    )
}

private fun DrawScope.drawTargetHighlight(
    targetRect: Rect,
    overlayColor: Color = Color.Black.copy(alpha = 0.7f),
    strokeColor: Color = Color.White,
    strokeWidth: Float = 2.dp.toPx()
) {
    val highlightPath = Path().apply {
        addRect(Rect(Offset.Zero, Size(size.width, size.height)))

        addRect(targetRect)

        fillType = PathFillType.EvenOdd
    }

    drawPath(
        path = highlightPath,
        color = overlayColor
    )

    drawRect(
        color = strokeColor,
        topLeft = targetRect.topLeft,
        size = targetRect.size,
        style = Stroke(width = strokeWidth)
    )
}

fun Modifier.addTargetToTargetGuide(userGuideState: UserGuideState, id: String, order: Int): Modifier = composed {
    onGloballyPositioned { layoutCoordinates ->
        val boundsInWindow = layoutCoordinates.boundsInRoot()

        userGuideState.registerTarget(
            id = id,
            order = order,
            position = Offset(boundsInWindow.left, boundsInWindow.top),
            size = Size(boundsInWindow.width, boundsInWindow.height)
        )
    }
}