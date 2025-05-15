package com.singularityuniverse.lib.userguide

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.singularityuniverse.lib.userguide.tools.LocalIsDebugModeEnabled
import com.singularityuniverse.lib.userguide.tools.borderStroke1Dp
import kotlin.math.absoluteValue

@Composable
fun UserGuideOverlay(
    state: UserGuideState,
    tooltipBuilder: (UserGuideOverlayScope.(targetInfo: TargetInfo) -> Unit)
) {
    val density = LocalDensity.current
    val isDebugMode = LocalIsDebugModeEnabled.current
    val currentTarget = state.getCurrentTarget()
    val scope = UserGuideOverlayScope(state, density)
        .apply { tooltipBuilder(this, currentTarget ?: return) }

    if (state.isShowing && state.animationState == GuideAnimationState.NAVIGATING) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((scope.targetRect.top / density.density).dp)
                    .border(borderStroke1Dp(if (isDebugMode) Color.Red else Color.Transparent)),
                contentAlignment = when {
                    scope.isTargetHorizontallyCentered -> Alignment.BottomCenter
                    scope.isPreferLeft -> Alignment.BottomEnd
                    else -> Alignment.BottomStart
                }
            ) {
                scope.aboveContent?.invoke(this)
            }

            Box(
                modifier = Modifier
                    .padding(top = (scope.targetRect.height / density.density).dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .border(borderStroke1Dp(if (isDebugMode) Color.Red else Color.Transparent)),
                contentAlignment =
                    when {
                        scope.isTargetHorizontallyCentered -> Alignment.TopCenter
                        scope.isPreferLeft -> Alignment.TopStart
                        else -> Alignment.TopEnd
                    }
            ) {

                scope.bellowContent?.invoke(this)

                if (isDebugMode)
                    Text(
                        """
                        preferOnTop = ${scope.isPreferTop}
                        preferLeft = ${scope.isPreferLeft}
                        centerVertically = ${scope.isTargetVerticallyCentered}
                        centerHorizontally = ${scope.isTargetHorizontallyCentered}
                    """.trimIndent(),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .zIndex(1f)
                    )
            }
        }
    }
}

class UserGuideOverlayScope(
    private val state: UserGuideState,
    private val density: Density
) {
    companion object {
        private val FLOAT_ROUNDING_PIXEL_TOLERANCE = 10
    }

    val rootRect: Rect get() = state.fullScreenRect
    val targetRect: Rect get() = state.animatedRect

    val isPreferTop: Boolean get() = targetRect.center.y > rootRect.center.y
    val isPreferBottom: Boolean get() = !isPreferTop
    val isPreferLeft: Boolean get() = targetRect.center.x < rootRect.center.x
    val isPreferRight: Boolean get() = !isPreferLeft

    val isTargetHorizontallyCentered: Boolean
        get() = (targetRect.center.x - rootRect.center.x).absoluteValue < FLOAT_ROUNDING_PIXEL_TOLERANCE
    val isTargetVerticallyCentered: Boolean = targetRect.center.y == rootRect.center.y

    val targetLeftSpace: Dp = (targetRect.left / density.density).dp
    val targetRightSpace: Dp = ((rootRect.width - targetRect.right) / density.density).dp

    internal var bellowContent: (@Composable BoxScope.() -> Unit)? = null
        private set

    fun drawBellow(content: @Composable BoxScope.() -> Unit) {
        bellowContent = content
    }

    internal var aboveContent: (@Composable BoxScope.() -> Unit)? = null
        private set

    fun drawAbove(content: @Composable BoxScope.() -> Unit) {
        aboveContent = content
    }
}