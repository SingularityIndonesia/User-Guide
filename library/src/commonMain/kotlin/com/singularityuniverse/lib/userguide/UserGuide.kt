package com.singularityuniverse.lib.userguide

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex

fun Modifier.userGuide(state: UserGuideState): Modifier = composed {
    val targetRect = state.getCurrentTargetRect()

    LaunchedEffect(state.animationState, targetRect) {
        when (state.animationState) {
            GuideAnimationState.ENTERING -> {
                // For the first step, initialize with full screen rect
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

                // Once animation completes, change state to NAVIGATING
                // This will trigger the balloon to show
                state.setAnimationState(GuideAnimationState.NAVIGATING)
            }

            GuideAnimationState.NAVIGATING -> {
                // This is for any adjustments needed while in the NAVIGATING state
                // For example, if the target position changes without a step change
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
                // No animation in idle state
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

@Composable
fun TooltipBalloon(
    targetState: UserGuideState,
    tooltip: String,
    tooltipColor: Color = Color.White,
    tooltipTextColor: Color = Color.Black,
    tooltipWidth: Int = 250
) {
    // We'll check the animation state here as well for extra security
    if (targetState.isShowing && targetState.animationState == GuideAnimationState.NAVIGATING) {
        val targetInfo = targetState.getCurrentTarget() ?: return
        val targetRect = targetInfo.rect
        
        // Calculate available space above and below the target
        val spaceAbove = targetRect.top
        val spaceBelow = targetState.fullScreenRect.height - targetRect.bottom
        
        // Determine if we should show the tooltip above or below the target
        val showAbove = spaceAbove > spaceBelow
        
        // Calculate the position for the tooltip
        val tooltipX = targetRect.left + targetRect.width / 2 - tooltipWidth / 2
        val tooltipY = if (showAbove) {
            targetRect.top - 10.dp.value
        } else {
            targetRect.bottom + 10.dp.value
        }
        
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(tooltipX.toInt(), tooltipY.toInt()),
            properties = PopupProperties(focusable = false),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tooltipColor,
                modifier = Modifier
                    .width(tooltipWidth.dp)
                    .zIndex(1000f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = tooltip,
                        color = tooltipTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun UserGuideOverlay(state: UserGuideState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Only show tooltip when animation state is NAVIGATING (animation completed)
        if (state.isShowing && state.animationState == GuideAnimationState.NAVIGATING) {
            // Display tooltip if content is provided for the current target
            state.getCurrentTarget()?.let { targetInfo ->
                targetInfo.tooltipContent?.let { tooltip ->
                    TooltipBalloon(
                        targetState = state,
                        tooltip = tooltip
                    )
                }
            }
        }
    }
}

fun Modifier.addTargetToTargetGuide(
    userGuideState: UserGuideState, 
    id: String, 
    order: Int,
    tooltipContent: String? = null
): Modifier = composed {
    onGloballyPositioned { layoutCoordinates ->
        val boundsInWindow = layoutCoordinates.boundsInRoot()

        userGuideState.registerTarget(
            id = id,
            order = order,
            position = Offset(boundsInWindow.left, boundsInWindow.top),
            size = Size(boundsInWindow.width, boundsInWindow.height),
            tooltipContent = tooltipContent
        )
    }
}